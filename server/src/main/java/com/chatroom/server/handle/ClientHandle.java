package com.chatroom.server.handle;

import com.chatroom.Foo;
import com.chatroom.core.Connector;
import com.chatroom.core.Packet;
import com.chatroom.core.ReceivePacket;
import com.chatroom.utils.CloseUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandle extends Connector {

    private final File cachePath;

    private final ClientHandleCallback clientHandleCallback;

    private final String clientInfo;

    public ClientHandle(SocketChannel socketChannel, ClientHandleCallback clientHandleCallback, File cachePath) throws IOException {
        this.clientHandleCallback = clientHandleCallback;
        this.clientInfo =socketChannel.getRemoteAddress().toString();
        this.cachePath = cachePath;
        System.out.println("新客户端链接：" + clientInfo);
        setup(socketChannel);

    }

    public void exit() {
        CloseUtils.close(this);
        System.out.println("客户端已退出：" + clientInfo);
    }

    @Override
    protected File createNewReceiveFile() {
        return Foo.createRandomTemp(cachePath);
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        exitByself();
    }

    @Override
    protected void onReceivedPacket(ReceivePacket packet) {
        super.onReceivedPacket(packet);
        if (packet.type() == Packet.TYPE_MEMORY_STRING) {
            String string = (String) packet.entity();
            System.out.println(key.toString() + ":" + string);
            clientHandleCallback.onNewMessageArrived(this, string);
        }
    }

    public void exitByself () {
        exit();
        clientHandleCallback.onSelfClosed(this);
    }


}
