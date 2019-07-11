package com.chatroom.Impl.async;

import com.chatroom.core.*;
import com.chatroom.utils.CloseUtils;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncSendDispatcher implements SendDispatcher {

    private final Sender sender;

    private final Queue<SendPacket> queue = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean isSending = new AtomicBoolean();

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private IoArgs ioArgs = new IoArgs();
    private SendPacket packetTemp;
    private int total;
    private int position;

    public AsyncSendDispatcher(Sender sender) {
        this.sender = sender;
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
        IoArgs args = ioArgs;

        args.startWriting();
        if (position >= total) {
            sendNextPacket();
            return;
        } else if (position == 0) {
            // 首包，需要携带长度信息
            args.writeLength(total);
        }

        byte[] bytes = packetTemp.bytes();

        int count = args.readFrom(bytes, position);
        position += count;

        // 完成封装
        args.finishWriting();

        try {
            sender.sendAsync(args, ioArgsEventListener);
        } catch (IOException e) {
            closeAndNotify();
        }


    }

    private void closeAndNotify() {

        CloseUtils.close(this);
    }

    private final IoArgs.IoArgsEventListener ioArgsEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {

        }

        @Override
        public void onCompleted(IoArgs args) {
            // 继续发送当前包
            sendCurrentPacket();
        }
    };

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
        if (isClosed.compareAndSet(false, true)) {
            isSending.set(false);
            SendPacket packetTemp = this.packetTemp;
            if (this.packetTemp != null) {
                this.packetTemp = null;
                CloseUtils.close(this.packetTemp);
            }
        }
    }

    @Override
    public void close() throws IOException {

    }
}
