package com.ecnu.test;

import com.alibaba.middleware.race.mom.DefaultProducer;
import com.alibaba.middleware.race.mom.Message;
import com.alibaba.middleware.race.mom.Producer;
import com.alibaba.middleware.race.mom.SendResult;
import com.alibaba.middleware.race.mom.SendStatus;

public class TestProducer2 {
	
	
	public static void main(String[] args) {
		
		if(args.length<1)
		{
			System.out.println("please input groupid");
			System.exit(0);
		}
		Producer producer=new DefaultProducer();
		producer.setGroupId("PG-test2");
		producer.setTopic(args[0]);
		producer.start();
		Message message=new Message();
		message.setBody("Hello MOM".getBytes());
		message.setProperty("area", "us");
		
		
		while(true)
		{
			SendResult result=producer.sendMessage(message);
			
			
			if (result.getStatus().equals(SendStatus.SUCCESS)) {
				System.out.println("send success:"+result.getMsgId());
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
