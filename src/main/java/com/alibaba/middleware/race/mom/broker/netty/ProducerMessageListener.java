package com.alibaba.middleware.race.mom.broker.netty;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.alibaba.middleware.race.mom.Message;
import com.alibaba.middleware.race.mom.SendResult;
import com.alibaba.middleware.race.mom.SendStatus;
import com.alibaba.middleware.race.mom.broker.AckManager;
import com.alibaba.middleware.race.mom.broker.ConsumerGroupInfo;
import com.alibaba.middleware.race.mom.broker.ConsumerManager;
import com.alibaba.middleware.race.mom.broker.SemaphoreManager;
import com.alibaba.middleware.race.mom.broker.SendHelper;
import com.alibaba.middleware.race.mom.broker.TaskManager;
import com.alibaba.middleware.race.mom.file.LogTask;
import com.alibaba.middleware.race.mom.model.SendTask;
import com.alibaba.middleware.race.mom.tool.MemoryTool;
import com.alibaba.middleware.race.mom.tool.Tool;


//broker收到producer的消息的时候监听类
public class ProducerMessageListener  extends MessageListener{

	@Override
	void onProducerMessageReceived(Message msg,String requestId,Channel channel) {
		
		//放入一个requestId对应的channel 在后面发送ack后删除
		AckManager.pushRequest(requestId, channel);
		
		// TODO Auto-generated method stub
		//标识是否序列化成功
		boolean isError=false;
		String mapstr="";
		for(Map.Entry<String, String> entry:msg.getProperties().entrySet()){    
			mapstr+=entry.getKey()+"="+entry.getValue();
		}   
		//System.out.println("receive producer message msgid:"+msg.getMsgId()+" topic:"+msg.getTopic()+" filter:"+mapstr);
		String topic=msg.getTopic();
		//找到订阅这个消息的组信息   最好有订阅过滤条件
		List<ConsumerGroupInfo> allgroups= ConsumerManager.findGroupByTopic(topic);
		//符合这个消息过滤消息的组
		List<ConsumerGroupInfo> groups=new ArrayList<ConsumerGroupInfo>();
		for (ConsumerGroupInfo groupinfo : allgroups) 
		{
			//groupinfo.findSubscriptionData(topic);
			String filterName=groupinfo.findSubscriptionData(topic).getFitlerName();
			String filterValue=groupinfo.findSubscriptionData(topic).getFitlerValue();
			if(filterName==null)
			{
				groups.add(groupinfo);
			}
			else
			{
				//判断消息是否有组需要的字段，且字段的值和消息一致
				if(msg.getProperty(filterName)!=null&&msg.getProperty(filterName).equals(filterValue))
				{
					groups.add(groupinfo);
				}
			}
		}
		if(groups.size()==0)//没有订阅这个消息的组，需要把消息存储在默认队列里面？
		{
			//TODO 丢失信息？
			//查找有没有专属于存储这个topic的队列
			//QueueFile queue=QueueManager.findQueue(topic);
			System.out.println("don't have match group");
			//返回这个ack
			SendResult ack=new SendResult();
			ack.setMsgId(msg.getMsgId());//message id
			ack.setInfo(requestId);//request id
			ack.setStatus(SendStatus.SUCCESS);
			AckManager.pushAck(ack);
			SemaphoreManager.increase("Ack");
		}
		//遍历所有的订阅该消息的组
		List<byte[]> logList=new ArrayList<byte[]>();
		List<SendTask> taskList=new ArrayList<SendTask>();
		for (ConsumerGroupInfo consumerGroupInfo : groups) {
			SendTask task=new SendTask();
			task.setGroupId(consumerGroupInfo.getGroupId());
			task.setTopic(topic);
			task.setMessage(msg);
			
			taskList.add(task);
			LogTask log=new LogTask(task, 0);
			byte[] data=Tool.serialize(log);
			logList.add(data);
		}
		try 
		{
			//生成一个requestID和messageID组成的键值
			String key=requestId+"@"+msg.getMsgId();
			//先把这些任务写到缓冲区，这时候ack消息还没有生成，producer还在等待ack消息
			//等到一定时机生成ACK消息加入ack消息队列等待ack发送线程发送ack消息
			FlushTool.writeToCache(logList,key);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//添加一个发送任务
		if(MemoryTool.moreThan(1024*1024*100*8))//100MB可用内存
		{
			TaskManager.pushTask(taskList);//把这些任务放到内存中的任务队列
			
			for (int i=0;i<taskList.size();i++) {
				SemaphoreManager.increase("SendTask");
			}
		}
		else
		{
			//不添加到发送队列了
		}
		
	}

}
