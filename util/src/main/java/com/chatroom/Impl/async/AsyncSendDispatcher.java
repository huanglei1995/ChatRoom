package com.chatroom.Impl.async;

import com.chatroom.core.*;
import com.chatroom.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncSendDispatcher implements SendDispatcher, IoArgs.IoArgsEventProcess {

    private final Sender sender;

    private final Queue<SendPacket> queue = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean isSending = new AtomicBoolean();

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private IoArgs ioArgs = new IoArgs();
    private SendPacket<?> packetTemp;

    private ReadableByteChannel packetChannel;
    private long total;
    private long position;

    public AsyncSendDispatcher(Sender sender) {
        this.sender = sender;
        try {
            sender.setSendListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(SendPacket packet) {
        queue.offer(packet);
        if (isSending.compareAndSet(false, true)) {
            sendNextPacket();
        }
    }

    private void sendNextPacket() {
        SendPacket temp = packetTemp;
        if (temp != null) {
            CloseUtils.close(temp);
        }
        SendPacket packet = packetTemp = tackPacket();
        if (packet == null) {
            isSending.set(false);
            return;
        }
        this.total = packet.lenth();
        position = 0;

        sendCurrentPacket();

    }

    private void sendCurrentPacket() {

        if (position >= total) {
            completePacket(position == total);
            sendNextPacket();
            return;
        }

        try {
            sender.postSendAsync();
        } catch (IOException e) {
            closeAndNotify();
        }


    }

    private void closeAndNotify() {

        CloseUtils.close(this);
    }

    private SendPacket tackPacket() {
        SendPacket packet = queue.poll();
        if (packet != null && packet.isCanceled()) {
            // 已取消，不用发送
            return tackPacket();
        }
        return packet;
    }

    @Override
    public void cancel(SendPacket packet) {

    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            isSending.set(false);
            // 异常关闭导致的完成操作
            completePacket(false);
        }
    }

    /**
     * 完成packet发送
     * @param isSuccess 是否成功
     */
    private void completePacket (boolean isSuccess) {
        SendPacket packet = this.packetTemp;
        if (packet== null) {
            return;
        }
        CloseUtils.close(packet);
        CloseUtils.close(packetChannel);
        packetChannel = null;
        packetTemp = null;
        total = 0;
        position = 0;
    }

    @Override
    public IoArgs provideIoArgs() {
        IoArgs args = ioArgs;
        if (packetChannel == null) {
            packetChannel = Channels.newChannel(packetTemp.open());
            args.limit(4);
            args.writeLength((int) packetTemp.lenth());
        } else {
            args.limit((int) Math.min(args.capacity(), total - position));
            try {
                int count = args.readFrom(packetChannel);
                position += count;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return args;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        // 继续发送当前包
        sendCurrentPacket();
    }
}
