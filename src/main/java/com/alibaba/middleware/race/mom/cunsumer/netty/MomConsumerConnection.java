package com.alibaba.middleware.race.mom.cunsumer.netty;

import io.netty.channel.ChannelInboundHandlerAdapter;

import com.alibaba.middleware.race.mom.MessageListener;
import com.alibaba.middleware.race.mom.SendCallback;
import com.alibaba.middleware.race.mom.model.InvokeFuture;
import com.alibaba.middleware.race.mom.model.MomRequest;

/**
 * 描述Consumer与broker服务器的连接 主要是被动接受消息，主动发消息
 * @author sei.zz
 *
 */
public interface MomConsumerConnection {
	void init();
	void connect();
	void connect(String host,int port);
	void setHandle(ChannelInboundHandlerAdapter handle);
	Object Send(MomRequest request);
	void SendSync(MomRequest request);
	void close();
	boolean isConnected();
	boolean isClosed();
	public boolean containsFuture(String key);
	public InvokeFuture<Object> removeFuture(String key);
	public void setTimeOut(long timeout);
}
