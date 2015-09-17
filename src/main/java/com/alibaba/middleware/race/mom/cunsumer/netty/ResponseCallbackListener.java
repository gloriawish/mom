package com.alibaba.middleware.race.mom.cunsumer.netty;

public interface ResponseCallbackListener {
    Object onResponse(Object response);
    void onTimeout();
    void onException(Throwable e);
    void onDisconnect(String msg);
}