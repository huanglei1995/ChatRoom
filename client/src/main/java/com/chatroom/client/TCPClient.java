package com.chatroom.client;

import com.chatroom.client.bean.ServerInfo;
import com.chatroom.utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TCPClient {

    private final Socket socket;

    private final ServerReadHandler readHandler;

    private final PrintStream printStream;

    public TCPClient(Socket socket, ServerReadHandler readHandler) throws IOException {
        this.socket = socket;
        this.readHandler = readHandler;
        this.printStream = new PrintStream(socket.getOutputStream());
    }

    public void exit () {
        readHandler.exit();
        CloseUtils.close(socket);
        CloseUtils.close(printStream);
    }

    public void send(String msg) {
        printStream.println(msg);
    }

    public static TCPClient startWith(ServerInfo info) throws IOException {

        Socket socket = new Socket();
        // 超时时间
        socket.setSoTimeout(30000);

        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()), 3000);

        System.out.println("已发起服务器连接，并进入后续流程~");
        System.out.println("客户端信息：" + socket.getLocalAddress() + "\t Port:" + socket.getLocalPort());
        System.out.println("服务器信息：" + socket.getInetAddress() + "\t Port:" + socket.getPort());
        try {
            // 接受数据
            ServerReadHandler readHandler = new ServerReadHandler(socket.getInputStream());
            readHandler.start();

            return new TCPClient(socket, readHandler);
        } catch (Exception e) {
            System.out.println("连接异常");
            CloseUtils.close(socket);
        }

        return null;
    }

    private static void writer(Socket client) throws IOException {

    }

    static class ServerReadHandler extends Thread {

        private boolean done = false;

        private final InputStream inputStream;

        public ServerReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            super.run();

            try {
                // 用于输入了，用于接受数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));
                do {
                    String str;
                    try {
                        // 客户端拿到一条数据
                        str = socketInput.readLine();
                    } catch (SocketTimeoutException e) {
                        continue;
                    }
                    if (str == null) {
                        System.out.println("连接已关闭！无法读取数据");
                        // 退出当前客户端，并breal
                        break;
                    }
                    // 打印到屏幕上
                    System.out.println(str);
                }while (!done);
            } catch (IOException e) {
                if (!done) {
                    System.out.println("链接异常断开：" + e.getMessage());
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
