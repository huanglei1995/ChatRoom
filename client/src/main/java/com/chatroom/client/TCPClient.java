package com.chatroom.client;

import com.chatroom.client.bean.ServerInfo;
import com.chatroom.core.Connector;
import com.chatroom.utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class TCPClient extends Connector {

    public TCPClient(SocketChannel channel) throws IOException {
        setup(channel);
    }

    public void exit () {
        CloseUtils.close(this);
    }
    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        System.out.println("链接已关闭，无法读取数据");
    }


    public static TCPClient startWith(ServerInfo info) throws IOException {

        SocketChannel socket = SocketChannel.open();

        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()));

        System.out.println("已发起服务器连接，并进入后续流程~");
        System.out.println("客户端信息：" + socket.getLocalAddress().toString());
        System.out.println("服务器信息：" + socket.getRemoteAddress().toString());
        try {
            return new TCPClient(socket);
        } catch (Exception e) {
            System.out.println("连接异常");
            CloseUtils.close(socket);
        }

        return null;
    }

}
