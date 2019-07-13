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

    private IoArgs.IoArgsEventProcess receiveIoEventProcess;
    private IoArgs.IoArgsEventProcess sendIoEventProcess;

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
    public void setReceiveListener(IoArgs.IoArgsEventProcess process) throws IOException {
        receiveIoEventProcess = process;
    }

    @Override
    public boolean postReceiveAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("当前channel已经关闭");
        }
        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public void setSendListener(IoArgs.IoArgsEventProcess process) throws IOException {
        sendIoEventProcess = process;
    }

    @Override
    public boolean postSendAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("当前channel已经关闭");
        }
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
            IoArgs.IoArgsEventProcess process = receiveIoEventProcess;
            IoArgs args = process.provideIoArgs();
            try {
                // 具体的读取操作
                if (args.readFrom(channel)>0) {
                    process.onConsumeCompleted(args);
                } else {
                    process.onConsumeFailed(args, new IOException("当前信息不能被读取"));
                }
            } catch (IOException e) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };

    private final IoProvider.HandleOutputCallback outputCallback = new IoProvider.HandleOutputCallback() {
        @Override
        protected void canProviderOutput() {
            if (isClosed.get()) {
                return;
            }

            IoArgs.IoArgsEventProcess process = sendIoEventProcess;
            IoArgs args = process.provideIoArgs();
            try {
                // 具体的读取操作
                if (args.writeTo(channel)>0) {
                    process.onConsumeCompleted(args);
                } else {
                    process.onConsumeFailed(args, new IOException("当前信息不能被读取"));
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
