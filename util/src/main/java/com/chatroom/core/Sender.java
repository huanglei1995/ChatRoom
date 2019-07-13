package com.chatroom.core;

import java.io.Closeable;
import java.io.IOException;

public interface Sender extends Closeable {

    void setSendListener(IoArgs.IoArgsEventProcess process) throws IOException;

    boolean postSendAsync() throws IOException;
}
