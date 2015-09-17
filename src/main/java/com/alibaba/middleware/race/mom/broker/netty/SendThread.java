package com.alibaba.middleware.race.mom.broker.netty;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.middleware.race.mom.ConsumeResult;
import com.alibaba.middleware.race.mom.ConsumeStatus;
import com.alibaba.middleware.race.mom.Message;
import com.alibaba.middleware.race.mom.broker.ClientChannelInfo;
import com.alibaba.middleware.race.mom.broker.ConsumerManager;
import com.alibaba.middleware.race.mom.broker.SemaphoreManager;
import com.alibaba.middleware.race.mom.broker.SendHelper;
import com.alibaba.middleware.race.mom.broker.TaskManager;
import com.alibaba.middleware.race.mom.model.MomRequest;
import com.alibaba.middleware.race.mom.model.MomResponse;
import com.alibaba.middleware.race.mom.model.RequestResponseFromType;
import com.alibaba.middleware.race.mom.model.ResponseType;
import com.alibaba.middleware.race.mom.model.SendTask;

/***
 * broker给consumer发送数据的线程
 * 每次从任务队列获取一个任务，然后根据任务的信息 找到要发送的consumer 同步发送
 * 如果发送失败或超时则加入任务队尾，继续下面的数据发送
 * @author zz
 *
 */
public class SendThread implements Runnable {
	//总的发送数量
	public static AtomicLong sendToal=new AtomicLong(0L);
	//发送的一个帮助类
	private SendHelper helper=new SendHelper();
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true)
		{
			SemaphoreManager.decrease("SendTask");
			SendTask task=TaskManager.getTask();
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
				
				if(!channel.getChannel().isActive()||!channel.getChannel().isOpen()||!channel.getChannel().isWritable())
				{
					TaskManager.pushResendTask(task);//放到重发线程去处理
					//设置下状态为离线
					ConsumerManager.putConsumerGroupStat(task.getGroupId(), -1);
					continue;
				}
				//如果发送的对象是一个超时对象就跳过它，由重发线程试着来发送这些数据
				if(ConsumerManager.getConsumerGroupStat(task.getGroupId())!=null&&ConsumerManager.getConsumerGroupStat(task.getGroupId())==1)
				{
					TaskManager.pushResendTask(task);//放到重发线程去处理
					continue;
				}
				
				long start=System.currentTimeMillis();
				MomRequest request=(MomRequest)helper.brokerSend(channel.getChannel(), response);
				long end=System.currentTimeMillis();
				ConsumeResult result=(ConsumeResult) request.getParameters();
				if(result.getStatus()!=ConsumeStatus.SUCCESS)
				{
					//TODO 消费失败 本身消费失败
					TaskManager.pushResendTask(task);
				}
				else
				{
					//设置状态为正常
					ConsumerManager.putConsumerGroupStat(task.getGroupId(), 0);
					sendToal.incrementAndGet();//增加一个已发送
					//FlushTool.logWriter.log("sendThread:"+Thread.currentThread().getId()+" send msg id="+msg.getMsgId()+" groupid="+task.getGroupId()+" success! use time:"+(end-start));
				}
			}
			catch (Exception e) 
			{
				// TODO: handle exception
				//发送超时，添加进入任务队尾
				TaskManager.pushResendTask(task);
				//设置状态为
				ConsumerManager.putConsumerGroupStat(task.getGroupId(), 1);
				FlushTool.logWriter.log("send time out:"+task.getMessage().getMsgId());
				
			}
		}
		
		
		
//		while(true)
//		{
//			//获取一个发送任务
//			SendTask task=TaskManager.getTask();
//			
//			String topic=task.getTopic();
//			Message msg=task.getMessage();
//			
//			//找到对应的组,取得下一个要发送的channel
//			if(ConsumerManager.findGroupByGroupID(task.getGroupId(),topic)!=null)
//			{
//				boolean flag=false;
//				//根据任务找到对应的group  判断这个group是否真正的订阅了这个消息  判断属性是否满足要求
//				//TODO 其实可以减少这里的判断  在收到消息的时候就确定了这个组可以接收这个消息
//				ConsumerGroupInfo groupinfo=ConsumerManager.findGroupByGroupID(task.getGroupId(),topic);
//				String filterName=groupinfo.findSubscriptionData(topic).getFitlerName();
//				String filterValue=groupinfo.findSubscriptionData(topic).getFitlerValue();
//				if(filterName==null)
//				{
//					//TODO send
//					flag=true;
//				}
//				else
//				{
//					//判断消息是否有组需要的字段，且字段的值和消息一致
//					if(msg.getProperty(filterName)!=null&&msg.getProperty(filterName).equals(filterValue))
//					{
//						//TODO send
//						flag=true;
//					}
//				}
//				if(flag)
//				{
//					ClientChannelInfo channel=ConsumerManager.findGroupByGroupID(task.getGroupId(),topic).getNextChannelInfo();
//					//发送消息
//					MomResponse response=new MomResponse();
//					response.setFromType(RequestResponseFromType.Broker);
//					response.setResponseType(ResponseType.Message);
//					response.setResponse(msg);
//					
//					//发送给consumer
//					try
//					{
//						MomRequest request=(MomRequest)helper.brokerSend(channel.getChannel(), response);
//						
//						ConsumeResult result=(ConsumeResult) request.getParameters();
//						
//						if(result.getStatus()==ConsumeStatus.SUCCESS)
//						{
//							//TODO 继续发送下一个
//						}
//						else
//						{
//							TaskManager.pushTask(task);
//						}
//					}
//					catch (Exception e) 
//					{
//						// TODO: handle exception
//						//发送超时，添加进入任务队尾
//						TaskManager.pushTask(task);
//					}
//					
//				}
//			}
//		
//		}
	}
}
