package com.chatroom.core;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

public interface IoProvider extends Closeable {

    boolean registerInput(SocketChannel socketChannel, HandleInputCallback handlerInputCallback);

    boolean registerOutput(SocketChannel socketChannel, HandleOutputCallback handleOutputCallback);

    void unRegisterInput(SocketChannel socketChannel);

    void unRegisterOutput(SocketChannel socketChannel);

    abstract class HandleInputCallback implements Runnable {
        @Override
        public void run() {
            canProviderInput();
        }

        protected abstract void canProviderInput();
    }

    abstract class HandleOutputCallback implements Runnable {

        @Override
        public void run() {
            canProviderOutput();
        }

        protected abstract void canProviderOutput();
    }
}
