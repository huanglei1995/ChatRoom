package com.chatroom.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

/**
 * 接受到的数据组合到包中
 */
public abstract class ReceivePacket<Stream extends OutputStream,Entity> extends Packet<Stream> {

    private Entity entity;

    public ReceivePacket(long len) {
        this.length = len;
    }

    public Entity entity () {
        return entity;
    }

    protected abstract Entity buildEntity (Stream stream);

    @Override
    protected void closeStream(Stream steam) throws IOException {
        super.closeStream(steam);
        entity = buildEntity(steam);
    }
}
