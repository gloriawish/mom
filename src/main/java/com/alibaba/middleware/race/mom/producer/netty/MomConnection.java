package com.alibaba.middleware.race.mom.producer.netty;

import io.netty.channel.ChannelInboundHandlerAdapter;

import com.alibaba.middleware.race.mom.SendCallback;
import com.alibaba.middleware.race.mom.model.InvokeFuture;
import com.alibaba.middleware.race.mom.model.MomRequest;

/**
 * 描述与服务器的连接
 * @author sei.zz
 *
 */
public interface MomConnection {
	void init();
	void connect();
	void connect(String host,int port);
	void setHandle(ChannelInboundHandlerAdapter handle);
	Object Send(MomRequest request);
	void Send(MomRequest request,final SendCallback listener);
	void close();
	boolean isConnected();
	boolean isClosed();
	public boolean containsFuture(String key);
	public InvokeFuture<Object> removeFuture(String key);
	public void setTimeOut(long timeout);
}
