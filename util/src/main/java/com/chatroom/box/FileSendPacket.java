package com.chatroom.box;

import com.chatroom.core.SendPacket;
import com.chatroom.utils.CloseUtils;

import java.io.*;

public class FileSendPacket extends SendPacket<FileInputStream> {

    public FileSendPacket(File file) {
        this.lenth = file.length();
    }

    @Override
    protected FileInputStream createStream() {
        return null;
    }
}
