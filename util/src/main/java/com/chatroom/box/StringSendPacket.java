package com.chatroom.box;

import com.chatroom.core.SendPacket;
import com.chatroom.utils.CloseUtils;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class StringSendPacket extends BytesSendPacket {

    public StringSendPacket (String msg) {
        super(msg.getBytes());
    }


    @Override
    public byte type() {
        return TYPE_MEMORY_STRING;
    }
}
