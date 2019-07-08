package com.chatroom.client;

import com.chatroom.client.bean.ServerInfo;

import java.io.*;

public class Client {

    public static void main(String[] args) {
        ServerInfo serverInfo = UDPSearcher.searchServer(20000);

        System.out.println("Server:" + serverInfo);

        if (serverInfo != null) {
            TCPClient tcpClient = null;
           try {
               tcpClient = TCPClient.startWith(serverInfo);
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
    }

    private static void write(TCPClient tcpClient) throws IOException {

        // 构建键盘输入流
        InputStream in  = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        do {
            // 键盘读取一行
            String str = input.readLine();
            // 发送到服务器
            tcpClient.send(str);

            if ("00bye00".equalsIgnoreCase(str)) {
                break;
            }
        }while (true);
    }
}
