package com.chatroom.box;

import com.chatroom.core.SendPacket;
import com.chatroom.utils.CloseUtils;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class StringSendPacket extends SendPacket<ByteArrayInputStream> {

    private final byte[] bytes;

    public StringSendPacket (String msg) {
        this.bytes = msg.getBytes();
        this.lenth = bytes.length;
    }

    @Override
    protected ByteArrayInputStream createStream() {
        return new ByteArrayInputStream(bytes);
    }


}
