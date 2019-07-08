package com.chatroom.server.handle;

import com.chatroom.utils.CloseUtils;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandle {

    private final Socket socket;

    private final ClientReadHandler readHandle;

    private final ClientWriterHandler writerHandle;

    private final ClientHandleCallback clientHandleCallback;

    private final String clientInfo;

    public ClientHandle(Socket socket, ClientHandleCallback clientHandleCallback) throws IOException {
        this.socket = socket;
        this.readHandle = new ClientReadHandler(socket.getInputStream());
        this.writerHandle = new ClientWriterHandler(socket.getOutputStream());
        this.clientHandleCallback = clientHandleCallback;
        this.clientInfo = String.format("A【%s】\t P【%s】", socket.getInetAddress(), socket.getPort());
        System.out.println("新客户端链接：" + clientInfo);
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void exit() {
        readHandle.exit();
        writerHandle.exit();
        CloseUtils.close(socket);
        System.out.println("客户端已退出：" + socket.getInetAddress() +
                " P:" + socket.getPort());
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

        private final InputStream inputStream;

        public ClientReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            super.run();

            try {
                // 用于输入了，用于接受数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));
                do {
                    // 客户端拿到一条数据
                    String str = socketInput.readLine();
                    if (str == null) {
                        System.out.println("客户端已无法读取数据？");
                        // 退出当前客户端，并breal
                        ClientHandle.this.exitByself();
                        break;
                    }
                    // 打印到屏幕上
                    // System.out.println(str);
                    clientHandleCallback.onNewMessageArrived(ClientHandle.this, str);
                }while (!done);
            } catch (IOException e) {
                if (!done) {
                    System.out.println("链接异常断开");
                    // 退出当前客户端，并breal
                    ClientHandle.this.exitByself();
                }
            } finally {
                CloseUtils.close(inputStream);
            }

        }

        void exit() {
            done = true;
            CloseUtils.close(inputStream);
        }
    }

}
