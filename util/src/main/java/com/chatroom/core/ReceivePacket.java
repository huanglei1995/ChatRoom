package com.chatroom.core;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 接受到的数据组合到包中
 */
public abstract class ReceivePacket<T extends OutputStream> extends Packet<T> {
}
