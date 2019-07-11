package com.chatroom.core;

import java.io.Closeable;

/**
 * 接收的数据调度封装
 * 把一份或者多份IoArgs组合成一个Packet
 */
public interface ReceiveDispatcher extends Closeable {

    void start();

    void stop();

    interface ReceivePacketCallback{
        void onReceivePacketComplated(ReceivePacket packet);
    }
}
