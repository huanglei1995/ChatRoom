package com.chatroom.core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class IoArgs {
    private int limit = 256;
    private byte[] byteBuffer = new byte[256];

    private ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);

    public int  readFrom(byte[] bytes, int offset){
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.put(bytes, offset, size);
        return size;
    }

    public int  writeTo(byte[] bytes, int offset){
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.get(bytes, offset, size);
        return size;
    }

    public int readFrom(SocketChannel channel) throws IOException {
        startWriting();
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int lenth = channel.read(buffer);
            if (lenth < 0) {
                throw new EOFException();
            }
            bytesProduced += lenth;
        }
        finishWriting();
        return bytesProduced;
    }

    public int writeTo(SocketChannel channel) throws IOException {
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int lenth = channel.write(buffer);
            if (lenth < 0) {
                throw new EOFException();
            }
            bytesProduced += lenth;
        }
        return bytesProduced;
    }

    public void startWriting() {
        buffer.clear();
        buffer.limit(limit);
    }

    public void finishWriting() {
        buffer.flip();
    }

    public String bufferString () {
        return new String(byteBuffer, 0 , buffer.position() - 1);
    }

    public void writeLength(int total) {
        buffer.putInt(total);
    }

    public int readLength () {
        return buffer.getInt();
    }

    public int capacity() {
        return buffer.capacity();
    }

    public interface IoArgsEventListener {
        void onStarted(IoArgs args);

        void onCompleted(IoArgs args);
    }

    public void limit(int limit) {
        this.limit = limit;
    }
}
