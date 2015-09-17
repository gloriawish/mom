package com.ecnu.test;

import com.alibaba.middleware.race.mom.ConsumeResult;
import com.alibaba.middleware.race.mom.ConsumeStatus;
import com.alibaba.middleware.race.mom.Consumer;
import com.alibaba.middleware.race.mom.DefaultConsumer;
import com.alibaba.middleware.race.mom.Message;
import com.alibaba.middleware.race.mom.MessageListener;

public class TestConsumer2 {
	
	public static void main(String[] args) {
		
		if(args.length<2)
		{
			System.out.println("please input groupid");
			System.exit(0);
		}
		String groupId=args[0];
		Consumer consumer = new DefaultConsumer();
		consumer.setGroupId(groupId);
		consumer.subscribe(args[1], "area=us"/*如果改属性为null或者空串，那么表示接收这个topic下的所有消息*/, new MessageListener() {

			@Override
			public ConsumeResult onMessage(Message message) {
				assert "T-test".equals(message.getTopic()) && "us".equals(message.getProperty("area"));
				System.out.println("consume success:" + message.getMsgId());
				ConsumeResult result = new ConsumeResult();
				result.setStatus(ConsumeStatus.SUCCESS);
				return result;
			}
		});
		consumer.start();
	}

}
