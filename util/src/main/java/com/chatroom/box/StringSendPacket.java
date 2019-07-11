package com.chatroom.box;

import com.chatroom.core.SendPacket;

import java.io.IOException;

public class StringSendPacket extends SendPacket {

    private final byte[] bytes;

    public StringSendPacket (String msg) {
        this.bytes = msg.getBytes();
        this.lenth = bytes.length;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public byte[] bytes() {
        return bytes;
    }
}
