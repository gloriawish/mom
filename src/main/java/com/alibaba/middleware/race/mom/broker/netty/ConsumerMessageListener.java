package com.alibaba.middleware.race.mom.broker.netty;


import com.alibaba.middleware.race.mom.ConsumeResult;
import com.alibaba.middleware.race.mom.ConsumeStatus;
import com.alibaba.middleware.race.mom.Message;
import com.alibaba.middleware.race.mom.broker.ClientChannelInfo;
import com.alibaba.middleware.race.mom.broker.ConsumerManager;
import com.alibaba.middleware.race.mom.broker.SemaphoreManager;
import com.alibaba.middleware.race.mom.broker.SendHelper;
import com.alibaba.middleware.race.mom.broker.SubscriptionInfo;
import com.alibaba.middleware.race.mom.broker.TaskManager;
import com.alibaba.middleware.race.mom.file.LogTask;
import com.alibaba.middleware.race.mom.model.SendTask;
import com.alibaba.middleware.race.mom.model.SubscriptRequestInfo;
import com.alibaba.middleware.race.mom.tool.Tool;

public class ConsumerMessageListener extends MessageListener {

	@Override
	void onConsumeResultReceived(ConsumeResult msg) {
		// TODO Auto-generated method stub
		if(msg.getStatus()==ConsumeStatus.SUCCESS)//消费成功的话，找到对应的队列删除那个消息，然后继续发送下个消息
		{
			//String key=msg.getGroupID()+msg.getTopic()+msg.getMsgId();
			//System.out.println("consume ok");
			
			if(TaskManager.findInResend(msg.getGroupID(), msg.getTopic(), msg.getMsgId()))
			{
				//表示这个消息时已经超时的 这时候发过来的消费消息无效
				return;
			}
			//TODO 记录一个发送成功的日志
			SendTask task=new SendTask();
			task.setGroupId(msg.getGroupID());
			task.setTopic(msg.getTopic());
			Message message=new Message();
			message.setMsgId(msg.getMsgId());
			task.setMessage(message);
			LogTask logtask=new LogTask(task, 1);
			byte[] data=Tool.serialize(logtask);
			//消费情况直接写入文件
			FlushTool.writeConsumeResult(data);
		}
	}
	//收到订阅消息后的操作
	@Override
	void onConsumeSubcriptReceived(SubscriptRequestInfo msg,ClientChannelInfo channel) {
		// TODO Auto-generated method stub
		
		System.out.println("receive subcript info groupid:"+msg.getGroupId()+" topic:"+msg.getTopic()+" filterName:"+msg.getPropertieName()+" filterValue:"+msg.getPropertieValue()+" clientId:"+channel.getClientId());
		
		SubscriptionInfo subscript=new SubscriptionInfo();
		subscript.setTopic(msg.getTopic());
		subscript.setFitlerName(msg.getPropertieName());
		subscript.setFitlerValue(msg.getPropertieValue());
		channel.setSubcript(subscript);//设置订阅信息
		
		//加入某个组
		//相同的组和相同的topic,更新订阅条件就好
		ConsumerManager.addGroupInfo(msg.getGroupId(), channel);
		
		//TODO nothing todo
		
	}

}
