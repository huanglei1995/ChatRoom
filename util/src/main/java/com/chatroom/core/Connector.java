package com.chatroom.core;

import com.chatroom.Impl.SocketChannelAdapter;
import com.chatroom.Impl.async.AsyncReceiveDispatcher;
import com.chatroom.Impl.async.AsyncSendDispatcher;
import com.chatroom.box.StringReceivePacket;
import com.chatroom.box.StringSendPacket;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

public class Connector implements Closeable,SocketChannelAdapter.OnChangeStatusChangedListener {

    // 设置当前连接的唯一性
    private UUID key = UUID.randomUUID();

    private SocketChannel channel;

    private Sender sender;

    private Receiver receiver;

    private SendDispatcher sendDispatcher;

    private ReceiveDispatcher receiveDispatcher;

    public void setup (SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;

        IoContext ioContext = IoContext.get();
        SocketChannelAdapter adapter = new SocketChannelAdapter(channel, ioContext.getIoProvider(), this);

        this.sender = adapter;
        this.receiver = adapter;
        sendDispatcher = new AsyncSendDispatcher(sender);
        receiveDispatcher = new AsyncReceiveDispatcher(receiver, receivePacketCallback);
        // 启动接受
        receiveDispatcher.start();
    }

    public void send(String msg) {
        SendPacket packet = new StringSendPacket(msg);
        sendDispatcher.send(packet);
    }


    private ReceiveDispatcher.ReceivePacketCallback receivePacketCallback = new ReceiveDispatcher.ReceivePacketCallback(){
        @Override
        public void onReceivePacketComplated(ReceivePacket packet) {
            if (packet instanceof StringReceivePacket) {
                String msg = ((StringReceivePacket) packet).string();
                onReceivedNewMessage(msg);
            }
        }
    };

    @Override
    public void close() throws IOException {
        receiveDispatcher.close();
        sendDispatcher.close();
        sender.close();
        receiver.close();
        channel.close();
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {

    }

    protected void onReceivedNewMessage (String msg) {
        System.out.println(key + ":" + msg);
    }
}
