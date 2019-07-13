package com.chatroom.server;

import com.chatroom.Foo;
import com.chatroom.Impl.IoSelectorProvider;
import com.chatroom.constants.TCPConstants;
import com.chatroom.core.IoContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {

    public static void main(String[] args) throws IOException {
        File cachePath = Foo.getCacheDir("server");
        IoContext.setup().ioProvider(new IoSelectorProvider()).start();
        // 初始化服务器ServerSocket,并搜索客户端连接，并作为一个数组对象
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER, cachePath);
        boolean isSuccessed = tcpServer.start();
        if (!isSuccessed) {
            System.out.println("Start TCP server failed");
            return;
        }
        //
        UDPProvider.start(TCPConstants.PORT_SERVER);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str;
        do {
            str = bufferedReader.readLine();

            if ("00bye00".equalsIgnoreCase(str)) {
                break;
            }
            tcpServer.broadCast(str);
        } while (true);


        UDPProvider.stop();
        tcpServer.stop();

        IoContext.close();
    }
}
