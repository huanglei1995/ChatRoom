package com.chatroom.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

public class IoArgs {
    private int limit = 256;

    private ByteBuffer buffer = ByteBuffer.allocate(256);

    /**
     * 从byte中读取数据
     * @param channel
     * @return
     */
    public int  readFrom(ReadableByteChannel channel) throws IOException {
        startWriting();
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.read(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
        finishWriting();
        return bytesProduced;
    }

    /**
     * 写数据到bytes中
     * @param channel
     * @return
     */
    public int  writeTo(WritableByteChannel channel) throws IOException {
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

    /**
     * 从SocketChannel中读取数据
     * @param channel
     * @return
     * @throws IOException
     */
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

    /**
     * 写数据到SocketChannel中
     * @param channel
     * @return
     * @throws IOException
     */
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

  /*  public String bufferString () {
        return new String(byteBuffer, 0 , buffer.position() - 1);
    }*/

    public void writeLength(int total) {
        startWriting();
        buffer.putInt(total);
        finishWriting();
    }

    public int readLength () {
        return buffer.getInt();
    }

    public int capacity() {
        return buffer.capacity();
    }

    /**
     * IoArgs 提供者，处理者；数据的生产或消费者
     */
    public interface IoArgsEventProcess {
        /**
         * 提供可供消费的IoARgs
         * @return
         */
        IoArgs provideIoArgs();

        /**
         * 消费失败时返回
         * @param args
         * @param e
         */
        void onConsumeFailed(IoArgs args, Exception e);

        /**
         * 消费成功
         * @param args
         */
        void onConsumeCompleted(IoArgs args);
    }

    public void limit(int limit) {
        this.limit = limit;
    }
}
