package com.alibaba.middleware.race.mom.broker;

import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.alibaba.middleware.race.mom.SendResult;

public class AckManager {
	
	private static ConcurrentLinkedQueue<SendResult> ackQueue=new ConcurrentLinkedQueue<SendResult>();
	
	private static ConcurrentHashMap<String/* requestId */, Channel> producerMap =
            new ConcurrentHashMap<String, Channel>();
	
	
	public static void pushRequest(String requestId,Channel channel)
	{
		producerMap.put(requestId, channel);
	}
	//查找到这个message发送来的channel,并从这里面删除它
	public static Channel findChannel(String requestId)
	{
		Channel channel= producerMap.remove(requestId);
		return channel;
	}
	
	public static boolean pushAck(SendResult ack)
	{
		return ackQueue.offer(ack);
	}
	public static boolean pushAck(List<SendResult> acks)
	{
		boolean flag=false;
		for (SendResult ack : acks) {
			flag=ackQueue.offer(ack);
		}
		return flag;
	}
	public static SendResult getAck()
	{
		return ackQueue.poll();
	}
}
