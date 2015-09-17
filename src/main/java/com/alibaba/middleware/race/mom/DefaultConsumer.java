package com.alibaba.middleware.race.mom;

import java.util.UUID;

import com.alibaba.middleware.race.mom.cunsumer.netty.MomConsumerConnection;
import com.alibaba.middleware.race.mom.cunsumer.netty.MomConsumerNettyConnection;
import com.alibaba.middleware.race.mom.cunsumer.netty.MomConsumertHandler;
import com.alibaba.middleware.race.mom.cunsumer.netty.ResponseCallbackListener;
import com.alibaba.middleware.race.mom.model.MomRequest;
import com.alibaba.middleware.race.mom.model.MomResponse;
import com.alibaba.middleware.race.mom.model.RequestType;
import com.alibaba.middleware.race.mom.model.SubscriptRequestInfo;

public class DefaultConsumer implements Consumer{
	//broker服务器ip地址
	private String brokerIp;
	//连接broker服务器的连接
	private MomConsumerConnection consumerConn;
	//消费者组id
	private String groupId;
	//订阅的topic
	private String topic;
	//过滤的属性名
	private String propertieName;
	//过滤的值
	private String propertieValue;
	//监听器
	private MessageListener listener;
	//是否有属性过滤
	private boolean isFilter=false;
	
	//是否输入订阅信息
	private boolean isSubcript=false;
	
	private boolean isRunning=false;
	
	private String clientKey="";
	
	public DefaultConsumer() {
		//this.brokerIp=System.getProperty("SIP");
		brokerIp="127.0.0.1";
		consumerConn=new MomConsumerNettyConnection(brokerIp,8888);
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		if(isSubcript)
		{
			//设置处理器 和结果回调函数
			consumerConn.setHandle(new MomConsumertHandler(consumerConn,new ResponseCallbackListener() {
				@Override
				public void onTimeout() {
					// TODO Auto-generated method stub
					
				}
				@Override
				public Object onResponse(Object response) {
					// TODO Auto-generated method stub
					MomResponse mr=(MomResponse)response;
					//调用上层设置的回调函数
					ConsumeResult result=listener.onMessage((Message)mr.getResponse());
					result.setGroupID(groupId);
					result.setTopic(topic);
					result.setMsgId(((Message)mr.getResponse()).getMsgId());
					return result;
				}
				@Override
				public void onException(Throwable e) {
					// TODO Auto-generated method stub
					if(e instanceof java.net.ConnectException)
					{
						System.out.println("connect error");
						if(isRunning)
							restartConnect();
					}
				}
				@Override
				public void onDisconnect(String msg) {
					// TODO Auto-generated method stub
					if(isRunning)
						restartConnect();
					
				}
			}));
			//连接服务器
			consumerConn.connect();
			clientKey=groupId+topic+UUID.randomUUID().toString();
			//clientKey=groupId+topic+"0000007";
			//TODO 发送一个自己的订阅信息给服务器
			isRunning=true;
			MomRequest request=new MomRequest();
			request.setRequestType(RequestType.Subscript);
			request.setRequestId(UUID.randomUUID().toString());
			
			//构造订阅信息发送给
			SubscriptRequestInfo subscript=new SubscriptRequestInfo();
			subscript.setGroupId(groupId);
			subscript.setTopic(topic);
			subscript.setPropertieName(propertieName);
			subscript.setPropertieValue(propertieValue);
			subscript.setClientKey(clientKey);
			request.setParameters(subscript);
			consumerConn.Send(request);
		}
	}
	
	//重新连接broker服务器
	public void restartConnect()
	{
		boolean isConnected=false;
		System.out.println("restart");
		//TODO 获取到新的brokerIp地址 从zookeeper获取
		//brokerIp=System.getProperty("SIP");
		brokerIp="127.0.0.1";
		consumerConn=new MomConsumerNettyConnection(brokerIp,8888);
		//重新设置处理器
		//设置处理器 和结果回调函数
		consumerConn.setHandle(new MomConsumertHandler(consumerConn,new ResponseCallbackListener() {
			@Override
			public void onTimeout() {
				// TODO Auto-generated method stub
				
			}
			@Override
			public Object onResponse(Object response) {
				// TODO Auto-generated method stub
				MomResponse mr=(MomResponse)response;
				//调用上层设置的回调函数
				ConsumeResult result=listener.onMessage((Message)mr.getResponse());
				result.setGroupID(groupId);
				result.setTopic(topic);
				result.setMsgId(((Message)mr.getResponse()).getMsgId());
				return result;
			}
			@Override
			public void onException(Throwable e) {
				// TODO Auto-generated method stub
				//System.out.println("DefaultConsumer error");
				if(e instanceof java.net.ConnectException)
				{
					System.out.println("connect error");
					if(isRunning)
						restartConnect();
				}
			}
			@Override
			public void onDisconnect(String msg) {
				// TODO Auto-generated method stub
				if(isRunning)
					restartConnect();
				
			}
		}));
		//连接服务器
		try 
		{
			consumerConn.connect();
			isConnected=true;
		}
		catch (Exception e)
		{
			// 重新连接服务器失败，过3秒重新连接
			try 
			{
				Thread.sleep(3000);
			}
			catch (InterruptedException ie) 
			{
				//TODO nothing todo
			}
			isConnected=false;
			restartConnect();
		}
		
		//TODO 发送一个自己的订阅信息给服务器
		if(isConnected)
		{
			MomRequest request=new MomRequest();
			request.setRequestType(RequestType.Subscript);
			request.setRequestId(UUID.randomUUID().toString());
			
			//构造订阅信息发送给
			SubscriptRequestInfo subscript=new SubscriptRequestInfo();
			subscript.setGroupId(groupId);
			subscript.setTopic(topic);
			subscript.setPropertieName(propertieName);
			subscript.setPropertieValue(propertieValue);
			subscript.setClientKey(clientKey);
			request.setParameters(subscript);
			consumerConn.Send(request);
		}
	}

	@Override
	public void subscribe(String topic, String filter, MessageListener listener) {
		// TODO Auto-generated method stub
		this.topic=topic;
		if(filter.trim().length()>0&&filter.contains("="))
		{
			this.propertieName=filter.split("=")[0];
			this.propertieValue=filter.split("=")[1];
			this.isFilter=true;
		}
		this.listener=listener;//设置监听
		isSubcript=true;
	}

	@Override
	public void setGroupId(String groupId) {
		// TODO Auto-generated method stub
		this.groupId=groupId;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
		if(isRunning)
		{
			isRunning=false;
			//发送一个退订消息，把自己从订阅关系里面移除
			MomRequest request=new MomRequest();
			request.setRequestType(RequestType.Stop);
			request.setRequestId(UUID.randomUUID().toString());
			request.setParameters(new String(clientKey));
			consumerConn.SendSync(request);
			consumerConn.close();
		}
		
	}
	
}
