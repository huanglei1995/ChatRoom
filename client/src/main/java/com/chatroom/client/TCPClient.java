package com.chatroom.client;

import com.chatroom.Foo;
import com.chatroom.client.bean.ServerInfo;
import com.chatroom.core.Connector;
import com.chatroom.core.Packet;
import com.chatroom.core.ReceivePacket;
import com.chatroom.utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class TCPClient extends Connector {

    private final File cachePath;

    public TCPClient(SocketChannel channel, File cachePath) throws IOException {
        this.cachePath = cachePath;
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

    @Override
    protected File createNewReceiveFile() {
        return Foo.createRandomTemp(cachePath);
    }

    @Override
    protected void onReceivedPacket(ReceivePacket packet) {
        super.onReceivedPacket(packet);
        if (packet.type() == Packet.TYPE_MEMORY_STRING) {
            String string = (String) packet.entity();
            System.out.println(key.toString() + ":" + string);
        }
    }

    public static TCPClient startWith(ServerInfo info, File cachePath) throws IOException {

        SocketChannel socket = SocketChannel.open();

        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()));

        System.out.println("已发起服务器连接，并进入后续流程~");
        System.out.println("客户端信息：" + socket.getLocalAddress().toString());
        System.out.println("服务器信息：" + socket.getRemoteAddress().toString());
        try {
            return new TCPClient(socket, cachePath);
        } catch (Exception e) {
            System.out.println("连接异常");
            CloseUtils.close(socket);
        }

        return null;
    }

}
