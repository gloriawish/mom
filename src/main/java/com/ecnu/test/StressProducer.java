package com.ecnu.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.middleware.race.mom.DefaultProducer;
import com.alibaba.middleware.race.mom.Message;
import com.alibaba.middleware.race.mom.Producer;
import com.alibaba.middleware.race.mom.SendResult;
import com.alibaba.middleware.race.mom.SendStatus;

public class StressProducer {
	private static ExecutorService executorService=Executors.newFixedThreadPool(30);
	public static void main(String[] args) {
		
		if(args.length<1)
		{
			System.out.println("please input groupid");
			System.exit(0);
		}
		
		final Producer producer=new DefaultProducer();
		producer.setGroupId("PG-test");
		producer.setTopic(args[0]);
		producer.start();
		
		for (int i = 0; i < 10; i++) {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							Message message=new Message();
							message.setBody("Hello MOM".getBytes());
							message.setProperty("area", "us");
							SendResult result=producer.sendMessage(message);
							if (result.getStatus()==SendStatus.SUCCESS) {
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
				}
			});
		}
		
	}

}
