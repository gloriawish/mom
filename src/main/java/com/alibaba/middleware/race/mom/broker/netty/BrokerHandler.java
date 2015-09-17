package com.alibaba.middleware.race.mom.broker.netty;

import java.util.Random;

import com.alibaba.middleware.race.mom.ConsumeResult;
import com.alibaba.middleware.race.mom.Message;
import com.alibaba.middleware.race.mom.SendResult;
import com.alibaba.middleware.race.mom.broker.ClientChannelInfo;
import com.alibaba.middleware.race.mom.broker.ConsumerGroupInfo;
import com.alibaba.middleware.race.mom.broker.ConsumerManager;
import com.alibaba.middleware.race.mom.broker.SendHelper;
import com.alibaba.middleware.race.mom.model.InvokeFuture;
import com.alibaba.middleware.race.mom.model.MomRequest;
import com.alibaba.middleware.race.mom.model.MomResponse;
import com.alibaba.middleware.race.mom.model.RequestResponseFromType;
import com.alibaba.middleware.race.mom.model.RequestType;
import com.alibaba.middleware.race.mom.model.ResponseType;
import com.alibaba.middleware.race.mom.model.SubscriptRequestInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;
/**
 * 在我们的系统中服务端收到的数据只能是MomRequest  客户端收到的数据只能是MomResponse
 * broker 对于网络通讯收到的消息的处理函数  
 * 
 * 
 * @author zz
 *
 */
@Sharable
public class BrokerHandler  extends ChannelInboundHandlerAdapter{

	//对producer发送的消息进行处理，只有一种消息就是Message
	private MessageListener producerListener;
	
	//对consumer发送的消息进行处理，有两种消息，第一种是订阅信息，第二种是消费信息
	private MessageListener consumerRequestListener;
	
	public void setProducerListener(MessageListener producerListener) {
		this.producerListener = producerListener;
	}

	public void setConsumerRequestListener(MessageListener consumerRequestListener) {
		this.consumerRequestListener = consumerRequestListener;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelActive(ctx);
		System.out.println("connect from :"+ctx.channel().remoteAddress());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		// TODO 这里对broker收到的所有消息进行dispatch
		
		MomRequest request=(MomRequest)msg;
		
		//构建响应消息
		MomResponse response=new MomResponse();
		response.setRequestId(request.getRequestId());
		response.setFromType(RequestResponseFromType.Broker);
		switch (request.getRequestType()) 
		{
		case ConsumeResult:
			//收到客户端消费结果信息
			ConsumeResult result=(ConsumeResult) request.getParameters();
			
			String key = response.getRequestId();
			if(SendHelper.containsFuture(key))
			{
				InvokeFuture<Object> future = SendHelper.removeFuture(key);
				
				if (future==null)
				{
					return;
				}
				else
				{
					future.setResult(request);
				}
			}
			//TODO应该在收到这个消息的时候就得到下一个需要发送的消息
			//BUT 为了做负载均衡不能再这里面发送消息
			consumerRequestListener.onConsumeResultReceived(result);
			consumerRequestListener.onRequest(request);
			
			break;
		case Message:
			//收到来自producer的信息
			Message message=(Message)request.getParameters();
			//当有多个生产者事同时刷入磁盘的数据量根据生产者上深
			if(request.getFromType()==RequestResponseFromType.Producer)
			{
				Conf.Increase(message.getTopic());
			}
			producerListener.onProducerMessageReceived(message,request.getRequestId(),ctx.channel());
			//返回给producer消息发送状态，由单独的线程返回数据给发送者
			//System.out.println("send message ack");
			break;
		case Subscript:
			//收到的是来自consumer的订阅信息
			SubscriptRequestInfo subcript=(SubscriptRequestInfo)request.getParameters();
			
			//构建一个channelinfo   clientid => groupid : topic + 随机数
			//String clientId=subcript.getGroupId()+":"+subcript.getTopic()+(new Random()).nextInt(100);
			String clientKey=subcript.getClientKey();
			ClientChannelInfo channel=new ClientChannelInfo(ctx.channel(), clientKey);
			consumerRequestListener.onConsumeSubcriptReceived(subcript,channel);
			consumerRequestListener.onRequest(request);
			
			response.setResponseType(ResponseType.AckSubscript);
			
			ctx.writeAndFlush(response);
			break;
		case Stop:
			String clientId=(String)request.getParameters();
			ConsumerManager.stopConsumer(clientId);
			break;
		default:
			System.out.println("type invalid");
			break;
		}
		
		
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelReadComplete(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		//super.exceptionCaught(ctx, cause);
//		consumerRequestListener.onError(cause);
//		producerListener.onError(cause);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
		System.out.println("disconnected");
	}

}
