package com.chatroom.core;

import com.chatroom.utils.CloseUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class SendPacket<T extends InputStream> extends Packet<T> {
    private boolean isCanceled;

    public boolean isCanceled () {
        return isCanceled;
    }

    /**
     * 设置取消发送标记
     */
    public void cancel() {
        isCanceled = true;
    }

}
