package com.chatroom.server;

import com.chatroom.server.handle.ClientHandle;
import com.chatroom.server.handle.ClientHandleCallback;
import com.chatroom.utils.CloseUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements ClientHandleCallback{

    private final int port;

    private ClientListener listener;

    private List<ClientHandle> clientHandleList = new ArrayList<>();

    private final ExecutorService forwardingThreadPoolExecutor;

    private Selector selector;

    private ServerSocketChannel server;

    public TCPServer(int port) {
        this.port = port;
        // 转发线程池
        this.forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
    }

    public boolean start () {
        try {
            selector = Selector.open();
            ServerSocketChannel server = ServerSocketChannel.open();

            // 设置为非阻塞
            server.configureBlocking(false);
            // 绑定本地端口
            server.socket().bind(new InetSocketAddress(port));

            // 注册客户端连接监听
            server.register(selector, SelectionKey.OP_ACCEPT);

            this.server = server;

            System.out.println("服务器信息：" + server.getLocalAddress().toString());
            // 启动客户端的监听
            ClientListener listener = this.listener = new ClientListener();
            listener.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop () {
        if (listener != null) {
            listener.exit();
        }
        CloseUtils.close(server);
        CloseUtils.close(selector);
        synchronized (TCPServer.this) {
            for (ClientHandle clientHandle : clientHandleList) {
                clientHandle.exit();
            }
            clientHandleList.clear();
        }
        // 停止线程池
        forwardingThreadPoolExecutor.shutdown();
    }

    public synchronized void broadCast(String str) {

        for (ClientHandle clientHandle : clientHandleList) {
            clientHandle.send(str);
        }
    }

    public class ClientListener extends Thread {
        private boolean done = false;

        @Override
        public void run() {
            super.run();
            Selector selector = TCPServer.this.selector;
            System.out.println("服务器准备就绪");
            do {
                // 得到客户端
                Socket client;
                try {
                    if (selector.select() == 0) {
                        if (done) {
                            return;
                        }
                        continue;
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        if (done) {
                            return;
                        }

                        SelectionKey key = iterator.next();
                        iterator.remove();
                        // 检查当前key的状态是否是我们关注的
                        // 客户端到大状态
                        if (key.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            // 非阻塞状态拿客户端
                            SocketChannel socketChannel = serverSocketChannel.accept();

                            try {
                                // 客户端构建异步线程
                                ClientHandle clientHandle = new ClientHandle(socketChannel, TCPServer.this);

                                synchronized (TCPServer.this) {
                                    clientHandleList.add(clientHandle);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("客户端连接异常：" + e.getMessage());
                            }

                        }
                   }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }while (!done);

            System.out.println("服务器关闭");
        }

        void exit() {
            done = true;
            // 唤醒当前阻塞
            selector.wakeup();
        }
    }


    @Override
    public synchronized void onSelfClosed(ClientHandle clientHandle) {
        clientHandleList.remove(clientHandle);
    }

    @Override
    public void onNewMessageArrived(final ClientHandle clientHandle, final String msg) {
        // 异步转发
        forwardingThreadPoolExecutor.execute(() -> {
            synchronized (TCPServer.this) {
                for (ClientHandle handle : clientHandleList) {
                    if (handle.equals(clientHandle)) {
                        // 跳过自己
                        continue;
                    }
                    // 对其他客户端发送消息
                    handle.send(msg);
                }
            }
        });
    }
}
