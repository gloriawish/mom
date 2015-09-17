package com.alibaba.middleware.race.mom.broker;

import java.util.List;

/**
 * 超时管理器，当broker发送消息给 consumer的时候 记录下这个发送时间，过一段时间遍历一下判断有没有超时的
 * @author zz
 * 未使用
 */
public class TimeOutManaer implements Runnable{
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true)
		{
			List<String> queues=QueueManager.getAllQueue();
			for (int i = 0; i < queues.size(); i++) {
				String key=queues.get(i);
				if(QueueManager.getRecordTime(key)!=null)
				{
					long now=System.currentTimeMillis();
					long start=QueueManager.getRecordTime(queues.get(i));
					int second =(int) ((now-start)/1000F);
					if(second>10)//秒数大于10，说明已经超时了，我需要重发一次
					{
						//判断是否为不为空，否则不能发送
						if(QueueManager.findQueue(key).size()>0)
						{
							//重发送消息
							SendHelper.sendMessagegByKey(key);
						}
					}
				}
			}
			try
			{
				//睡10秒后检查超时
				Thread.sleep(10000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}
}
