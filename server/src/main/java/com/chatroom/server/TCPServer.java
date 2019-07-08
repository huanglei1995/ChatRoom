package com.chatroom.server;

import com.chatroom.server.handle.ClientHandle;
import com.chatroom.server.handle.ClientHandleCallback;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements ClientHandleCallback{

    private final int port;

    private ClientListener mListener;

    private List<ClientHandle> clientHandleList = new ArrayList<>();

    private final ExecutorService forwardingThreadPoolExecutor;

    public TCPServer(int port) {
        this.port = port;
        // 转发线程池
        this.forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
    }

    public boolean start () {
        try {
            ClientListener listener = new ClientListener(port);
            mListener = listener;
            listener.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop () {
        if (mListener != null) {
            mListener.exit();
        }
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
        private ServerSocket server;

        private boolean done = false;

        public ClientListener(int port) throws IOException {
            server = new ServerSocket(port);
            System.out.println("服务器信息：" + server.getInetAddress()+ "\t Port:" + server.getLocalPort());
        }

        @Override
        public void run() {
            super.run();
            System.out.println("服务器准备就绪");
            do {
                Socket client;
                try {
                    client = server.accept();
                } catch (IOException e) {
                    continue;
                }
                try {
                    // 客户端构建异步线程
                    ClientHandle clientHandle = new ClientHandle(client, TCPServer.this);
                    // 读取数据并打印
                    clientHandle.readToPrint();
                    synchronized (TCPServer.this) {
                        clientHandleList.add(clientHandle);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("客户端连接异常：" + e.getMessage());
                }
            }while (!done);

            System.out.println("服务器关闭");
        }

        void exit() {
            done = true;
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public synchronized void onSelfClosed(ClientHandle clientHandle) {
        clientHandleList.remove(clientHandle);
    }

    @Override
    public void onNewMessageArrived(final ClientHandle clientHandle, final String msg) {
        // 打印到屏幕上
        System.out.println("Received-"  + clientHandle.getClientInfo() + ":" + msg);

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
