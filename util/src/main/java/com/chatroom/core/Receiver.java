package com.chatroom.core;

import java.io.Closeable;
import java.io.IOException;

public interface Receiver extends Closeable {
    void setReceiveListener(IoArgs.IoArgsEventListener listener) throws IOException;

    boolean receiveAsync(IoArgs args) throws IOException;
}
