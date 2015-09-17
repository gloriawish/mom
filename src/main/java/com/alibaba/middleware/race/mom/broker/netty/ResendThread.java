package com.alibaba.middleware.race.mom.broker.netty;

import java.util.UUID;

import com.alibaba.middleware.race.mom.ConsumeResult;
import com.alibaba.middleware.race.mom.ConsumeStatus;
import com.alibaba.middleware.race.mom.Message;
import com.alibaba.middleware.race.mom.broker.ClientChannelInfo;
import com.alibaba.middleware.race.mom.broker.ConsumerManager;
import com.alibaba.middleware.race.mom.broker.SendHelper;
import com.alibaba.middleware.race.mom.broker.TaskManager;
import com.alibaba.middleware.race.mom.model.MomRequest;
import com.alibaba.middleware.race.mom.model.MomResponse;
import com.alibaba.middleware.race.mom.model.RequestResponseFromType;
import com.alibaba.middleware.race.mom.model.ResponseType;
import com.alibaba.middleware.race.mom.model.SendTask;

/**
 * 什么时候出发重新发送线程呢？ 当发送的tps下降的时候，启动多少个线程呢？
 * 
 * 
 * @author zz
 *
 */
public class ResendThread  implements Runnable{

	//发送的一个帮助类
	private SendHelper helper=new SendHelper();
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		//TODO 需要知道消费方是否恢复消费，触发条件就是重发队列不增加？
		SendTask task=null;
		while((task=TaskManager.getResendTask())!=null)
		{
			Message msg=task.getMessage();
			//负载均衡
			if(ConsumerManager.findGroupByGroupID(task.getGroupId())==null)
			{
				TaskManager.pushResendTask(task);//放到重发线程去处理
				continue;
			}
			ClientChannelInfo channel=ConsumerManager.findGroupByGroupID(task.getGroupId()).getNextChannelInfo();
			//发送消息
			MomResponse response=new MomResponse();
			response.setFromType(RequestResponseFromType.Broker);
			response.setResponseType(ResponseType.Message);
			response.setResponse(msg);
			response.setRequestId(UUID.randomUUID().toString());
			//发送给consumer
			try
			{
				MomRequest request=(MomRequest)helper.brokerSend(channel.getChannel(), response);
				ConsumeResult result=(ConsumeResult) request.getParameters();
				if(result.getStatus()!=ConsumeStatus.SUCCESS)
				{
					//TODO 消费失败 本身消费失败
					TaskManager.pushResendTask(task);
				}
				else
				{
					ConsumerManager.putConsumerGroupStat(task.getGroupId(), 0);
					System.out.println("resendThread:"+Thread.currentThread().getId()+" send msg id="+msg.getMsgId());
				}
			}
			catch (Exception e) 
			{
				// TODO: handle exception
				//发送超时，添加进入任务队尾
				TaskManager.pushResendTask(task);
				
			}
		}
		//减少当前重发线程数量
		ResendManager.resendThreadNumber--;
		
		
	}
	
}
