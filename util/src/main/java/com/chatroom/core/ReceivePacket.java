package com.chatroom.core;

/**
 * 接受到的数据组合到包中
 */
public abstract class ReceivePacket extends Packet {

    public abstract void save(byte[] bytes, int count);
}
