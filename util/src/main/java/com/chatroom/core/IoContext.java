package com.chatroom.core;

import java.io.Closeable;
import java.io.IOException;

public class IoContext {

    private static IoContext INSTANCE;

    private final IoProvider ioProvider;

    public IoContext(IoProvider ioProvider) {
        this.ioProvider = ioProvider;
    }

    public IoProvider getIoProvider() {
        return ioProvider;
    }

    public static IoContext get() {
        return INSTANCE;
    }

    public static StartBoot setup() {
        return new StartBoot();
    }

    public static void close() {
        if (INSTANCE != null) {
            try {
                INSTANCE.callclose();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // @Override
    private void callclose() throws IOException {
        ioProvider.close();
    }

    public static class StartBoot {
        private IoProvider ioProvider;

        public StartBoot() {
        }

        public StartBoot ioProvider(IoProvider ioProvider) {
            this.ioProvider = ioProvider;
            return this;
        }

        public IoContext start () {
            INSTANCE = new IoContext(ioProvider);
            return INSTANCE;
        }
    }

}
