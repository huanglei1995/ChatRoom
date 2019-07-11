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

        private Object  attach;

        public final <T> T getAttach() {
            T attach = (T) this.attach;
            return attach;
        }

        public final void setAttach(Object attach) {
            this.attach = attach;
        }

        @Override
        public void run() {
            canProviderInput(attach);
        }

        protected abstract void canProviderInput(Object attach);
    }
}
