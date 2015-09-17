package com.alibaba.middleware.race.mom.broker.netty;

import io.netty.channel.Channel;

import com.alibaba.middleware.race.mom.ConsumeResult;
import com.alibaba.middleware.race.mom.Message;
import com.alibaba.middleware.race.mom.SendResult;
import com.alibaba.middleware.race.mom.broker.ClientChannelInfo;
import com.alibaba.middleware.race.mom.model.MomRequest;
import com.alibaba.middleware.race.mom.model.SubscriptRequestInfo;

public abstract class MessageListener {
	
	//当收到来自producer的消息时触发的事件,处理完成后需要返回一个ACK确认信息
	void onProducerMessageReceived(Message msg,String requestId,Channel channel)
	{
		
	}
	
	
	//当收到consumer发送的消费信息的事件
	void onConsumeResultReceived(ConsumeResult msg)
	{
		
	}
	//当收到客户端的订阅信息触发的事件
	void onConsumeSubcriptReceived(SubscriptRequestInfo msg,ClientChannelInfo channel)
	{
		
	}
	//当收到请求触发的事件
	void onRequest(MomRequest request)
	{
		
	}
	//当异常时触发的事件
	void onError(Throwable t)
	{
		
	}

}
