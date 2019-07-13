package com.chatroom.client;

import com.chatroom.Foo;
import com.chatroom.Impl.IoSelectorProvider;
import com.chatroom.box.FileSendPacket;
import com.chatroom.client.bean.ServerInfo;
import com.chatroom.core.IoContext;

import java.io.*;

public class Client {

    public static void main(String[] args) throws IOException {
        File cachePath = Foo.getCacheDir("client");
        IoContext.setup().ioProvider(new IoSelectorProvider()).start();

        ServerInfo serverInfo = UDPSearcher.searchServer(20000);

        System.out.println("Server:" + serverInfo);

        if (serverInfo != null) {
            TCPClient tcpClient = null;
           try {
               tcpClient = TCPClient.startWith(serverInfo, cachePath);
               if (tcpClient == null) {
                   return;
               }
                write(tcpClient);

           } catch (Exception e) {
               e.printStackTrace();
           } finally {
               if (tcpClient != null) {
                   tcpClient.exit();
               }
           }
        }

        IoContext.close();
    }

    private static void write(TCPClient tcpClient) throws IOException {

        // 构建键盘输入流
        InputStream in  = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        do {
            // 键盘读取一行
            String str = input.readLine();
            if ("00bye00".equalsIgnoreCase(str)) {
                break;
            }

            // --f url
            if (str.startsWith("--f")) {
                String[] array = str.split(" ");
                if (array.length >= 2) {
                    String filePath = array[1];
                    File file = new File(filePath);
                    if (file.exists() && file.isFile()) {
                        FileSendPacket packet = new FileSendPacket(file);
                        tcpClient.send(packet);
                        continue;
                    }
                }
            }

            // 发送字符串
            tcpClient.send(str);
        }while (true);
    }
}
