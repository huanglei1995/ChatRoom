package com.chatroom.box;

import com.chatroom.core.ReceivePacket;
import com.chatroom.core.SendPacket;

import java.io.*;

public class StringReceivePacket extends AbsByteArrayReceivePacket<String> {
    public StringReceivePacket(long len) {
        super(len);
    }

    @Override
    protected String buildEntity(ByteArrayOutputStream stream) {
        return new String(stream.toByteArray());
    }

    @Override
    public byte type() {
        return TYPE_MEMORY_STRING;
    }
}
