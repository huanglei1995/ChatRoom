package com.chatroom.Impl;

import com.chatroom.core.IoArgs;
import com.chatroom.core.IoProvider;
import com.chatroom.core.Receiver;
import com.chatroom.core.Sender;
import com.chatroom.utils.CloseUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketChannelAdapter implements Sender, Receiver, Closeable {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private final SocketChannel channel;

    private final IoProvider ioProvider;

    private final OnChangeStatusChangedListener listener;

    private IoArgs.IoArgsEventListener receiveIoEventListener;
    private IoArgs.IoArgsEventListener sendIoEventListener;

    private IoArgs receiveTemp;

    public SocketChannelAdapter(SocketChannel channel, IoProvider ioProvider, OnChangeStatusChangedListener listener) {
        this.channel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;
        // 设置是否阻塞，
        try {
            channel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setReceiveListener(IoArgs.IoArgsEventListener listener) throws IOException {
        receiveIoEventListener = listener;
    }

    @Override
    public boolean receiveAsync(IoArgs args) throws IOException {
        if (isClosed.get()) {
            throw new IOException("当前channel已经关闭");
        }
        receiveTemp = args;
        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public boolean sendAsync(IoArgs args, IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()) {
            throw new IOException("当前channel已经关闭");
        }
        sendIoEventListener = listener;
        // 设置当前发送的数据，添加到回调中
        outputCallback.setAttach(args);
        return ioProvider.registerOutput(channel, outputCallback);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            ioProvider.unRegisterInput(channel);
            ioProvider.unRegisterOutput(channel);
            CloseUtils.close(channel);
            listener.onChannelClosed(channel);
        }
    }

    private final IoProvider.HandleInputCallback inputCallback = new IoProvider.HandleInputCallback() {
        @Override
        protected void canProviderInput() {
            if (isClosed.get()) {
                return;
            }
            IoArgs args = receiveTemp;
            IoArgs.IoArgsEventListener listener = SocketChannelAdapter.this.receiveIoEventListener;
            listener.onStarted(args);

            try {
                // 具体的读取操作
                if (args.readFrom(channel)>0) {
                    listener.onCompleted(args);
                } else {
                    throw new IOException("当前信息不能被读取");
                }
            } catch (IOException e) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };

    private final IoProvider.HandleOutputCallback outputCallback = new IoProvider.HandleOutputCallback() {
        @Override
        protected void canProviderInput(Object attach) {
            if (isClosed.get()) {
                return;
            }

            IoArgs args = getAttach();
            IoArgs.IoArgsEventListener listener = sendIoEventListener;
            listener.onStarted(args);

            try {
                // 具体的读取操作
                if (args.writeTo(channel)>0) {
                    listener.onCompleted(args);
                } else {
                    throw new IOException("当前信息不能被读取");
                }
            } catch (IOException e) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };

    public interface OnChangeStatusChangedListener {

        void onChannelClosed(SocketChannel channel);
    }
}
