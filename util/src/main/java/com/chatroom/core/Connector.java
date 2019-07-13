package com.chatroom.core;

import com.chatroom.Impl.SocketChannelAdapter;
import com.chatroom.Impl.async.AsyncReceiveDispatcher;
import com.chatroom.Impl.async.AsyncSendDispatcher;
import com.chatroom.box.BytesReceivePacket;
import com.chatroom.box.FileReceivePacket;
import com.chatroom.box.StringReceivePacket;
import com.chatroom.box.StringSendPacket;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

public abstract class  Connector implements Closeable,SocketChannelAdapter.OnChangeStatusChangedListener {

    // 设置当前连接的唯一性
    protected UUID key = UUID.randomUUID();
    private SocketChannel channel;
    private Sender sender;
    private Receiver receiver;
    private SendDispatcher sendDispatcher;
    private ReceiveDispatcher receiveDispatcher;

    public void setup (SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;

        IoContext context = IoContext.get();
        SocketChannelAdapter adapter = new SocketChannelAdapter(channel, context.getIoProvider(), this);

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

    public void send(SendPacket packet) {
        sendDispatcher.send(packet);
    }

    @Override
    public void close() throws IOException {
        receiveDispatcher.close();
        sendDispatcher.close();
        sender.close();
        receiver.close();
        channel.close();
    }

    protected abstract File createNewReceiveFile();

    private ReceiveDispatcher.ReceivePacketCallback receivePacketCallback = new ReceiveDispatcher.ReceivePacketCallback(){

        @Override
        public ReceivePacket<?, ?> onArrivedNewPacket(byte type, long length) {
            switch (type) {
                case Packet.TYPE_MEMORY_BYTES:
                    return new BytesReceivePacket(length);
                case Packet.TYPE_MEMORY_STRING:
                    return new StringReceivePacket(length);
                case Packet.TYPE_STREAM_FILE:
                    return new FileReceivePacket(length, createNewReceiveFile());
                case Packet.TYPE_STREAM_DIRECT:
                    return new BytesReceivePacket(length);
                default:
                    throw new UnsupportedOperationException("Unsupported packet type:" + type);
            }
        }

        @Override
        public void onReceivePacketComplated(ReceivePacket packet) {
            onReceivedPacket(packet);
        }
    };

    @Override
    public void onChannelClosed(SocketChannel channel) {

    }
    protected void onReceivedPacket (ReceivePacket packet) {
        System.out.println(key.toString() + ":[New Packet]-Type:" + packet.type() + ", Length:" + packet.length);
    }


    protected void onReceivedNewMessage (String msg) {
        System.out.println(key + ":" + msg);
    }
}
