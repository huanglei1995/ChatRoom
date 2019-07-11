package com.chatroom.core;

import java.io.Closeable;

/**
 * 发送的调度
 * 缓存所有需要发送的数据，通过队列对数据进行发送
 * 对数据进行前后包装
 */
public interface SendDispatcher extends Closeable {

    /**
     * 发送一份数据
     * @param packet
     */
    void send(SendPacket packet);

    /**
     * 取消发送数据
     * @param packet
     */
    void cancel(SendPacket packet);
}
