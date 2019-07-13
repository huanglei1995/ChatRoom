package com.chatroom.box;

import com.chatroom.core.ReceivePacket;
import com.chatroom.core.SendPacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class StringReceivePacket extends ReceivePacket<ByteArrayOutputStream> {
    private String string;
    public StringReceivePacket(int len) {
        lenth = len;
    }

    public String string () {
        return string;
    }

    @Override
    protected void closeStream(ByteArrayOutputStream stream) throws IOException {
        super.closeStream(stream);
        string = new String(stream.toByteArray());
    }

    @Override
    protected ByteArrayOutputStream createStream() {
        return new ByteArrayOutputStream((int)lenth);
    }
}
