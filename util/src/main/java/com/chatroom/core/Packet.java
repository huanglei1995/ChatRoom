package com.chatroom.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public abstract class Packet<T extends Closeable> implements Closeable {

    private T stream;

    protected byte type;

    protected long lenth;

    public byte type() {
        return type;
    }

    public long lenth () {
        return lenth;
    }

    public final T open(){
        if (stream == null) {
            stream = createStream();
        }
        return stream;
    }

    protected abstract T createStream ();

    protected  void closeStream (T steam) throws IOException {
        stream.close();
    }
    @Override
    public void close() throws IOException {
        if (stream != null) {
            closeStream(stream);
            stream = null;
        }
    }

}
