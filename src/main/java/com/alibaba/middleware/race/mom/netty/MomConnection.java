package com.alibaba.middleware.race.mom.netty;

import com.alibaba.middleware.race.mom.SendCallback;
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
	Object Send(MomRequest request);
	Object Send(MomRequest request,final SendCallback listener);
	void close();
	boolean isConnected();
	boolean isClosed();
	public boolean containsFuture(String key);
	public InvokeFuture<Object> removeFuture(String key);
	public void setTimeOut(long timeout);
}
