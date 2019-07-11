package com.chatroom.core;

import java.io.Closeable;
import java.io.IOException;

public abstract class Packet implements Closeable {

    protected byte type;

    protected int lenth;

    public byte type() {
        return type;
    }

    public int lenth () {
        return lenth;
    }

}
