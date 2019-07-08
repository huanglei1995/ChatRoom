package com.chatroom.server.handle;

public interface ClientHandleCallback {

    // 关闭自身通知
    void onSelfClosed(ClientHandle clientHandle);

    // 收到消息时的通知
    void onNewMessageArrived(ClientHandle clientHandle, String msg);
}
