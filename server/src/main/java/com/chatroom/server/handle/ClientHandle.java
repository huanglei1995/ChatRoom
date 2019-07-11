package com.chatroom.server.handle;

import com.chatroom.core.Connector;
import com.chatroom.utils.CloseUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandle extends Connector {

    private final ClientHandleCallback clientHandleCallback;

    private final String clientInfo;

    public ClientHandle(SocketChannel socketChannel, ClientHandleCallback clientHandleCallback) throws IOException {
        this.clientHandleCallback = clientHandleCallback;
        this.clientInfo =socketChannel.getRemoteAddress().toString();
        System.out.println("新客户端链接：" + clientInfo);
        setup(socketChannel);

    }

    public void exit() {
        CloseUtils.close(this);
        System.out.println("客户端已退出：" + clientInfo);
    }


    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        exitByself();
    }

    @Override
    protected void onReceivedNewMessage(String msg) {
        super.onReceivedNewMessage(msg);
        clientHandleCallback.onNewMessageArrived(this, msg);
    }

    public void exitByself () {
        exit();
        clientHandleCallback.onSelfClosed(this);
    }


}
