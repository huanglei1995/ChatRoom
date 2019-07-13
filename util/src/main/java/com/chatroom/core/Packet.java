package com.chatroom.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public abstract class Packet<Stream extends Closeable> implements Closeable {

    // BYTES类型
    public static final byte TYPE_MEMORY_BYTES = 1;
    public static final byte TYPE_MEMORY_STRING = 2;
    public static final byte TYPE_STREAM_FILE = 3;
    public static final byte TYPE_STREAM_DIRECT = 4;

    private Stream stream;

    protected long length;

    public long lenth () {
        return length;
    }

    public final Stream open(){
        if (stream == null) {
            stream = createStream();
        }
        return stream;
    }

    public abstract byte type();

    protected abstract Stream createStream ();

    protected  void closeStream (Stream steam) throws IOException {
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
