package com.alibaba.middleware.race.mom.broker;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.alibaba.middleware.race.mom.ConsumeResult;
import com.alibaba.middleware.race.mom.ConsumeStatus;
import com.alibaba.middleware.race.mom.Message;
import com.alibaba.middleware.race.mom.file.MessageLog;
import com.alibaba.middleware.race.mom.model.InvokeFuture;
import com.alibaba.middleware.race.mom.model.MomRequest;
import com.alibaba.middleware.race.mom.model.MomResponse;
import com.alibaba.middleware.race.mom.model.RequestResponseFromType;
import com.alibaba.middleware.race.mom.model.ResponseType;
import com.alibaba.middleware.race.mom.tool.QueueFile;
import com.alibaba.middleware.race.mom.tool.Tool;

public class SendHelper {

	//用于发送队列的信息到对应的consumer
	public static void sendMessagegByKey(String key)
	{
		QueueFile queue=QueueManager.findQueue(key);
		try
		{
			//取出下一个发送的消息
			if(queue.size()>0)
			{
				Message pullmsg=(Message)Tool.deserialize(queue.peek(),Message.class);
				
				MomResponse response=new MomResponse();
				response.setFromType(RequestResponseFromType.Broker);
				response.setResponseType(ResponseType.Message);
				response.setResponse(pullmsg);
				
				//找到对应的组,取得下一个要发送的channel
				if(QueueManager.getGroup(key)!=null)
				{
					ClientChannelInfo channel=ConsumerManager.findGroupByGroupID(QueueManager.getGroup(key)).getNextChannelInfo();
					//发送消息
					channel.getChannel().writeAndFlush(response);
					
					//记录下这个队列的发送时间
					QueueManager.recordTime(key, System.currentTimeMillis());
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private long timeout=3000;//默认超时
	
	public static volatile Map<String, InvokeFuture<Object>> futrues=new ConcurrentHashMap<String, InvokeFuture<Object>>();
	/**
	 * broker给consumer发送消息
	 * @param channel
	 * @param message
	 * @return
	 */
	public Object brokerSend(Channel channel,MomResponse response)
	{
		if(channel!=null)
		{	
			final InvokeFuture<Object> future=new InvokeFuture<Object>();
			futrues.put(response.getRequestId(), future);
			//设置这次请求的ID
			future.setRequestId(response.getRequestId());
			ChannelFuture cfuture=channel.writeAndFlush(response);
			cfuture.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture rfuture) throws Exception {
					if(!rfuture.isSuccess()){
						future.setCause(rfuture.cause());
					}
				}
			});
			try
			{
				Object result=future.getResult(timeout, TimeUnit.MILLISECONDS);
				return result;
			}
			catch(RuntimeException e)
			{
				throw e;
			}
			finally
			{
				//这个结果已经收到
				futrues.remove(response.getRequestId());
			}
		}
		else
		{
			return null;
		}
	}
	
	public static boolean containsFuture(String key) {
		// TODO Auto-generated method stub
		return futrues.containsKey(key);
	}

	public static InvokeFuture<Object> removeFuture(String key) {
		// TODO Auto-generated method stub
		if(futrues.containsKey(key))
		{
			return futrues.remove(key);
		}
		else
			return null;
	}
	
	
	

}
