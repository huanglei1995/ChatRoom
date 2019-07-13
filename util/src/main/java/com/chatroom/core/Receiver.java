package com.chatroom.core;

import java.io.Closeable;
import java.io.IOException;

public interface Receiver extends Closeable {
    void setReceiveListener(IoArgs.IoArgsEventProcess process) throws IOException;

    boolean postReceiveAsync() throws IOException;
}
