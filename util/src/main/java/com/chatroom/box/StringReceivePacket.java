package com.chatroom.box;

import com.chatroom.core.ReceivePacket;
import com.chatroom.core.SendPacket;

import java.io.IOException;

public class StringReceivePacket extends ReceivePacket {

    private byte[] buffer;

    private int position;

    public StringReceivePacket(int len) {
        this.buffer = new byte[len];
        lenth = len;
    }

    @Override
    public void save(byte[] bytes, int count) {
        System.arraycopy(bytes, 0, buffer, position, count);
        position += count;
    }



    public String string () {
        return new String(buffer);
    }

    @Override
    public void close() throws IOException {

    }
}
