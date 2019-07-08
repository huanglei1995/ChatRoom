package com.chatroom.client;

import com.chatroom.client.bean.ServerInfo;
import com.chatroom.constants.UDPConstants;
import com.chatroom.utils.ByteUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UDPSearcher {

    private static final int LISTEN_PORT = UDPConstants.PORT_CLIENT_RESPONSE;

    public static ServerInfo searchServer(int timeout) {
        System.out.println("UDPSearcher Started:");
        // 成功收到回送的栅栏
        CountDownLatch receiveLatch = new CountDownLatch(1);
        Listener listenner = null;
        try {
            listenner = listen(receiveLatch);
            sendBroadcast();
            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 完成
        if (listenner == null) {
            return null;
        }

        List<ServerInfo> devices = listenner.getServerAndClose();
        if (devices.size() > 0) {
            return devices.get(0);
        }
        return null;
    }

    // 监听的方法
    private static Listener listen (CountDownLatch receiveLatch) throws InterruptedException {

        System.out.println("UDPSearch started Listener");

        CountDownLatch startLatch = new CountDownLatch(1);

        Listener listener = new Listener(LISTEN_PORT, startLatch, receiveLatch);

        listener.start();
        startLatch.await();

        return listener;
    }

    // 发送广播的方法
    private static void sendBroadcast () throws IOException {
        System.out.println("UDPSearch sendBroadcast started");

        // 作为搜索方，让系统自动分配
        DatagramSocket ds = new DatagramSocket();


        // 构建一份会送数据
        ByteBuffer buffer = ByteBuffer.allocate(128);
        buffer.put(UDPConstants.HEADER);
        buffer.putShort((short) 1);
        buffer.putInt(LISTEN_PORT);

        // 直接根据发送者构建一份回送信息
        DatagramPacket requestPacket  = new DatagramPacket(buffer.array(), buffer.position() + 1);
        // 广播地址
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        // 设置服务器端口
        requestPacket.setPort(UDPConstants.PORT_SERVER);

        ds.send(requestPacket);
        ds.close();

        // 关闭
        System.out.println("UDPSearch sendBroadcast finished");
    }

    private static class Listener extends Thread {

        private final int listenPort;

        private final CountDownLatch startDownLatch;

        private final CountDownLatch receiveLatch;

        private final byte[] buffer = new byte[128];

        private final int minLen = UDPConstants.HEADER.length + 2 +4;

        private final List<ServerInfo> serverInfoList = new ArrayList<>();

        private boolean done = false;

        private DatagramSocket ds = null;

        public Listener(int listenPort, CountDownLatch startDownLatch, CountDownLatch receiveLatch) {
            super();
            this.listenPort = listenPort;
            this.startDownLatch = startDownLatch;
            this.receiveLatch = receiveLatch;
        }

        @Override
        public void run() {
            super.run();

            startDownLatch.countDown();

            try {

                //
                ds = new DatagramSocket(listenPort);
                //'构建接受实体
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                while (!done) {
                    // 接受
                    ds.receive(receivePacket);

                    // 打印接受到的信息与发送者的信息
                    // 发送者的端口和ip
                    String ip = receivePacket.getAddress().getHostAddress();
                    int port = receivePacket.getPort();
                    int datalen = receivePacket.getLength();
                    byte[] data = receivePacket.getData();

                    boolean isValid = datalen >= minLen && ByteUtils.startsWith(data, UDPConstants.HEADER);

                    System.out.println("UDPSearch receive from ip:" + ip + "\tport:" + port + "\tDataValid:" + isValid);

                    if (!isValid) {
                        continue;
                    }

                    // 解析命令与回送终端
                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, UDPConstants.HEADER.length, datalen);

                    final short cmd = byteBuffer.getShort();

                    final int serverPort = byteBuffer.getInt();
                    // 判断合法性
                    if (cmd != 2 || serverPort <= 0) {
                        System.out.println("UDPSearch receive cmd:" +cmd + "\tserverPort" + serverPort);

                    }
                    // 构建一份回送数据
                    String sn = new String(buffer, minLen, datalen - minLen);
                    ServerInfo serverInfo = new ServerInfo(sn, serverPort, ip);
                    serverInfoList.add(serverInfo);
                    // 成功接受一份
                    receiveLatch.countDown();
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                close();
            }

            System.out.println("UDPSearch listener finish");
        }

        private void close () {
            if (ds != null) {
                ds.close();;
                ds = null;
            }
        }

        List<ServerInfo> getServerAndClose() {
            done = true;
            close();
            return serverInfoList;
        }
    }
}
