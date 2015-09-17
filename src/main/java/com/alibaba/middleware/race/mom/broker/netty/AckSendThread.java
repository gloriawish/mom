package com.alibaba.middleware.race.mom.broker.netty;

import io.netty.channel.Channel;

import com.alibaba.middleware.race.mom.SendResult;
import com.alibaba.middleware.race.mom.broker.AckManager;
import com.alibaba.middleware.race.mom.broker.SemaphoreManager;
import com.alibaba.middleware.race.mom.model.MomResponse;
import com.alibaba.middleware.race.mom.model.RequestResponseFromType;
import com.alibaba.middleware.race.mom.model.ResponseType;


/***
 * ack消息发送线程,从ack队列取出来的消息都是已经落盘了的消息
 * @author zz
 *
 */
public class AckSendThread implements Runnable{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while(true)
		{
			SemaphoreManager.decrease("Ack");//获取一个ack信号量
			
			//获取ack消息
			SendResult ack=AckManager.getAck();
			//获取requestId
			String requestId=ack.getInfo();
			ack.setInfo(null);
			
			//更具requestID找到那个对应的channel来发送数据
			Channel channel=AckManager.findChannel(requestId);
			if(channel!=null&&channel.isActive()&&channel.isOpen()&&channel.isWritable())
			{
				MomResponse response=new MomResponse();
				response.setRequestId(requestId);
				response.setFromType(RequestResponseFromType.Broker);
				response.setResponseType(ResponseType.SendResult);
				response.setResponse(ack);
				
				channel.writeAndFlush(response);//发送ack到生产者
			}
		}
	}

}
