package com.chatroom.server.handle;


import com.chatroom.utils.CloseUtils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientWriterHandler {

    private boolean done = false;

    private final PrintStream printStream;

    private final ExecutorService executorService;

    public ClientWriterHandler(OutputStream outputStream) {
        this.printStream = new PrintStream(outputStream);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    void exit() {
        done = true;
        CloseUtils.close(printStream);
    }

    public void send(String str) {
        if (done) {
            return;
        }
        executorService.execute(new WriterRunnable(str));
    }

    class WriterRunnable implements Runnable{

        private final String msg;

        public WriterRunnable(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            if (ClientWriterHandler.this.done) {
                return;
            }
            try {
                ClientWriterHandler.this.printStream.println(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
