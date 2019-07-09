package com.chatroom.server.handle;

import com.chatroom.utils.CloseUtils;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandle {

    private final SocketChannel socketChannel;

    private final ClientReadHandler readHandle;

    private final ClientWriterHandler writerHandle;

    private final ClientHandleCallback clientHandleCallback;

    private final String clientInfo;

    public ClientHandle(SocketChannel socketChannel, ClientHandleCallback clientHandleCallback) throws IOException {
        this.socketChannel = socketChannel;

        // 设置非阻塞
        socketChannel.configureBlocking(false);

        // 读取selector
        Selector readSelector = Selector.open();
        socketChannel.register(readSelector, SelectionKey.OP_READ);
        // 写selector
        Selector writeSelector = Selector.open();
        socketChannel.register(writeSelector, SelectionKey.OP_WRITE);

        this.readHandle = new ClientReadHandler(readSelector);
        this.writerHandle = new ClientWriterHandler(writeSelector);
        this.clientHandleCallback = clientHandleCallback;
        this.clientInfo =socketChannel.getRemoteAddress().toString();
        System.out.println("新客户端链接：" + clientInfo);
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void exit() {
        readHandle.exit();
        writerHandle.exit();
        CloseUtils.close(socketChannel);
        System.out.println("客户端已退出：" + clientInfo);
    }

    public void send(String str) {
        writerHandle.send(str);
    }

    public void readToPrint() {
        readHandle.start();
    }

    public void exitByself () {
        exit();
        clientHandleCallback.onSelfClosed(this);
    }

    public class ClientReadHandler extends Thread {

        private boolean done = false;

        private final Selector selector;

        private final ByteBuffer byteBuffer;

        public ClientReadHandler(Selector selector) {
            this.selector = selector;
            this.byteBuffer = ByteBuffer.allocate(256);
        }

        @Override
        public void run() {
            super.run();

            try {
                do {
                    // 客户端拿到一条数据
                    if (selector.select() == 0) {
                        if (done) {
                            return;
                        }
                        continue;
                    }
                    // 读取所有的selectorKey
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        // 判断是否可读，因为当前是读的selector
                        if (key.isReadable()) {
                            // 读取数据
                            SocketChannel client = (SocketChannel) key.channel();
                            // 确保读取之前，byteBuffer为空的
                            byteBuffer.clear();
                            int read = client.read(byteBuffer);
                            if (read > 0) {
                                // 去掉最后的换行符
                                String str = new String(byteBuffer.array(), 0, byteBuffer.position() - 1);
                                // 打印到屏幕上
                                clientHandleCallback.onNewMessageArrived(ClientHandle.this, str);
                            } else {
                                System.out.println("客户端已无法读取数据？");
                                // 退出当前客户端，并breal
                                ClientHandle.this.exitByself();
                                break;
                            }

                        }
                    }

                }while (!done);
            } catch (IOException e) {
                if (!done) {
                    System.out.println("链接异常断开");
                    // 退出当前客户端，并breal
                    ClientHandle.this.exitByself();
                }
            } finally {
                CloseUtils.close(selector);
            }

        }

        void exit() {
            done = true;
            CloseUtils.close(selector);
        }
    }

    public class ClientWriterHandler {

        private boolean done = false;

        private final Selector selector;

        private final ByteBuffer byteBuffer;

        private final ExecutorService executorService;

        public ClientWriterHandler(Selector selector) {
            this.selector = selector;
            this.byteBuffer = ByteBuffer.allocate(256);
            this.executorService = Executors.newSingleThreadExecutor();
        }

        void exit() {
            done = true;
            CloseUtils.close(selector);
        }

        public void send(String str) {
            if (done) {
                return;
            }
            executorService.execute(new WriterRunnable(str));
        }

        class WriterRunnable implements Runnable{

            private final String msg;

            public WriterRunnable(String msg) {
                this.msg = msg;
            }

            @Override
            public void run() {
                if (ClientWriterHandler.this.done) {
                    return;
                }

                byteBuffer.clear();
                byteBuffer.put(msg.getBytes());
                // 反转，让坐标归0
                byteBuffer.flip();
                while (!done && byteBuffer.hasRemaining()) {
                    try {
                        int write = socketChannel.write(byteBuffer);
                        if (write < 0) {
                            System.out.println("客户端已经无法发送数据");
                            ClientHandle.this.exitByself();
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

}
