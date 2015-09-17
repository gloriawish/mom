package com.alibaba.middleware.race.mom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.middleware.race.mom.broker.netty.Conf;
import com.alibaba.middleware.race.mom.model.MomRequest;
import com.alibaba.middleware.race.mom.model.MomResponse;
import com.alibaba.middleware.race.mom.model.RequestResponseFromType;
import com.alibaba.middleware.race.mom.model.RequestType;
import com.alibaba.middleware.race.mom.producer.netty.ConnectListener;
import com.alibaba.middleware.race.mom.producer.netty.MomConnection;
import com.alibaba.middleware.race.mom.producer.netty.MomNettyConnection;
import com.alibaba.middleware.race.mom.producer.netty.MomtHandler;


//生产者类
public class DefaultProducer implements Producer{
	
	//broker服务器ip地址
	private String brokerIp;
	//连接broker服务器的连接
	private MomConnection brokerConn;
	private static AtomicLong callTimes = new AtomicLong(0L);
	private List<MomConnection> brokerConn_list;//拓展的连接，解决网络IO瓶颈
	
	private AtomicInteger requestId=new AtomicInteger(0);//消息的ID
	
	//组id
	private String groupId;
	
	//生产着的topic
	private String topic;
	
	private boolean isRunning=false;
	
	private boolean isConnected=false;
	public DefaultProducer() {
		//brokerIp=System.getProperty("SIP");
		brokerIp="127.0.0.1";
		brokerConn=new MomNettyConnection(brokerIp, 8888);
		brokerConn_list=new ArrayList<MomConnection>();
		int num=Runtime.getRuntime().availableProcessors()/2;
		//int num=Conf.connNum;
		for (int i = 0; i < num; i++) {
			brokerConn_list.add(new MomNettyConnection(brokerIp, 8888));
		}
	}
	
	
 	synchronized MomConnection select()
	{
		//Random rd=new Random(System.currentTimeMillis());
		int d=(int) (callTimes.getAndIncrement()%(brokerConn_list.size()+1));
		if(d==0)
			return brokerConn;
		else
		{
			return brokerConn_list.get(d-1);
		}
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		//设置处理器
		brokerConn.setHandle(new MomtHandler(brokerConn,new ConnectListener() {
			
			@Override
			public void onDisconnected(String addr) {
				// TODO Auto-generated method stub
				synchronized (this) {
					if(isRunning)
					{
						isConnected=false;
						restartConnect();
					}
				}
			}
		}));
		//连接服务器
		brokerConn.connect();
		for (MomConnection conn:brokerConn_list) 
		{
			conn.setHandle(new MomtHandler(conn));
			conn.connect();
		}
		isRunning=true;
		isConnected=true;
	}

	@Override
	public void setTopic(String topic) {
		// TODO Auto-generated method stub
		this.topic=topic;
	}

	@Override
	public void setGroupId(String groupId) {
		// TODO Auto-generated method stub
		this.groupId=groupId;
	}

	public void restartConnect()
	{
		System.out.println("restartConnect");
		//brokerIp=System.getProperty("SIP");//通过zookeeper获取broker服务器ip
		brokerIp="127.0.0.1";
		brokerConn=new MomNettyConnection(brokerIp, 8888);
		brokerConn_list=new ArrayList<MomConnection>();
		int num=Conf.connNum;
		for (int i = 0; i < num; i++) {
			brokerConn_list.add(new MomNettyConnection(brokerIp, 8888));
		}
		//设置处理器
		brokerConn.setHandle(new MomtHandler(brokerConn,new ConnectListener() {
			
			@Override
			public void onDisconnected(String t) {
				// TODO Auto-generated method stub
				synchronized (this) {
					if(isRunning)
					{
						isConnected=false;
						restartConnect();
					}
				}
			}
		}));
		//连接服务器
		try 
		{
			brokerConn.connect();
			for (MomConnection conn:brokerConn_list) 
			{
				conn.setHandle(new MomtHandler(conn));
				conn.connect();
			}
		}catch (Exception e) {
			// TODO: handle exception
			try 
			{
				Thread.sleep(3000);
			}
			catch (InterruptedException ie) 
			{
				//TODO nothing todo
			}
			restartConnect();
		}
		isConnected=true;
	}
	
	@Override
	public SendResult sendMessage(Message message) {
		// TODO Auto-generated method stub
		if(!isRunning)
			return null;
		if(!isConnected)//未连接，broker可能是重启了
		{
			SendResult temp=new SendResult();
			temp.setStatus(SendStatus.FAIL);
			return temp;
		}
		//要发送的消息设置主题
		message.setTopic(topic);
		message.setBornTime(System.currentTimeMillis());
		
		//构建请求信息
		MomRequest request=new MomRequest();
		//request.setRequestId(UUID.randomUUID().toString());
		request.setRequestId(requestId.incrementAndGet()+"");
		request.setParameters(message);//设置要发送的内容
		//发送的请求为消息
		request.setRequestType(RequestType.Message);
		request.setFromType(RequestResponseFromType.Producer);
		message.setMsgId(request.getRequestId());
		//同步发送信息
		MomResponse response=(MomResponse)select().Send(request);//按照一定的顺序选择一个连接发送数据
		
		SendResult result=(SendResult)response.getResponse();
		return result;
	}

	@Override
	public void asyncSendMessage(Message message, SendCallback callback) {
		// TODO Auto-generated method stub
		//要发送的消息设置主题
		message.setTopic(topic);
		message.setBornTime(System.currentTimeMillis());
		
		//构建请求信息
		MomRequest request=new MomRequest();
		request.setRequestId(UUID.randomUUID().toString());
		request.setParameters(message);//设置要发送的内容
		//发送的请求为消息
		request.setRequestType(RequestType.Message);
		request.setFromType(RequestResponseFromType.Producer);
		message.setMsgId(request.getRequestId());
		
		brokerConn.Send(request, callback);
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		if(isRunning)
		{
			isRunning=false;
			brokerConn.close();
			
			for (MomConnection momConnection : brokerConn_list) {
				momConnection.close();
			}
		}
	}
	
}
