package com.chatroom.server;

import com.chatroom.constants.UDPConstants;
import com.chatroom.utils.ByteUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

class UDPProvider {

    // 单例
    private static Provider PROVIDER_INSTANCE;


    static void start(int port) {
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, port);
        provider.start();
        PROVIDER_INSTANCE = provider;
    }

     static void stop() {

        if (PROVIDER_INSTANCE != null) {
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    private static class Provider extends Thread {

        private final byte[] sn;
        private final int port;
        private boolean done = false;
        private DatagramSocket ds = null;

        // 存储消息的buffer
        final byte[] buffer = new byte[128];

        private Provider(String sn, int port) {
            super();
            this.sn = sn.getBytes();
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("UDPProvider started");

            try {

                // 监听端口
                ds = new DatagramSocket(UDPConstants.PORT_SERVER);

                // 接受消息的package
                DatagramPacket receivePack = new DatagramPacket(buffer,buffer.length);

                while (!done) {
                    // 接受
                    ds.receive(receivePack);

                    // 打印接受到的信息与发送者 的信息
                    // 发送者的ip
                    String clientIp  = receivePack.getAddress().getHostAddress();
                    int clientPort = receivePack.getPort();
                    int clientDataLen = receivePack.getLength();

                    byte[] clientData = receivePack.getData();

                    boolean isValid = clientDataLen >= (UDPConstants.HEADER.length + 2 + 4) && ByteUtils.startsWith(clientData, UDPConstants.HEADER);

                    System.out.println("UDPProvider receive from ip:" + clientIp + "\tport:" + clientPort + "\tdata:" + new String(clientData) + "\tvalidData:" + isValid);

                    if (!isValid) {
                        continue;
                    }

                    // 解析命令与回送终端
                    int index = UDPConstants.HEADER.length;
                    short cmd = (short) ((clientData[index++] << 8) | clientData[index++] & 0xff);
                    int responsePort = (((clientData[index++]) << 24) |
                            ((clientData[index++] & 0xff) << 16) |
                            ((clientData[index++] & 0xff) << 8) |
                            ((clientData[index] & 0xff)));

                    // 判断合法性
                    if (cmd == 1 && responsePort > 0) {
                        // 构建一份回送数据
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short)2);
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);

                        int len = byteBuffer.position();

                        // 直接构建发送者构建一份回送数据
                        DatagramPacket responsePack = new DatagramPacket(buffer, len, receivePack.getAddress(), responsePort);
                        ds.send(responsePack);
                        System.out.println("UDPProvider response to:" + clientIp + "\tport:" + responsePort + "\ttdataLen:" + len);
                    } else {
                        System.out.println("UDPProvider receive cmd nosupport; cmd:" +cmd + "\tport" + port);
                    }
                }
            } catch (Exception e) {
            } finally {
                close();
            }

            // 完成
            System.out.println("UDPProvider Finished.");
        }

        private void close () {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        void exit () {
            done = true;
            close();
        }
    }
}
