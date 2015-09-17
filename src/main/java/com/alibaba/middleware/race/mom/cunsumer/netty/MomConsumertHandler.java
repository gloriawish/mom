package com.alibaba.middleware.race.mom.cunsumer.netty;

import com.alibaba.middleware.race.mom.ConsumeResult;
import com.alibaba.middleware.race.mom.model.InvokeFuture;
import com.alibaba.middleware.race.mom.model.MomRequest;
import com.alibaba.middleware.race.mom.model.MomResponse;
import com.alibaba.middleware.race.mom.model.RequestResponseFromType;
import com.alibaba.middleware.race.mom.model.RequestType;
import com.alibaba.middleware.race.mom.model.ResponseType;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
public class MomConsumertHandler extends ChannelInboundHandlerAdapter  {
	
	private MomConsumerConnection connect;
	private Throwable cause;
	private ResponseCallbackListener listener;
	MomConsumertHandler()
	{
		//TODO nothing
	}
	public MomConsumertHandler(MomConsumerConnection conn,ResponseCallbackListener listener)
	{
		this.connect=conn;
		this.listener=listener;
	}
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		System.out.println("connected on server:"+ctx.channel().remoteAddress().toString());
	}
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		super.channelRead(ctx, msg);
		MomResponse response=(MomResponse)msg;
		
		String key = response.getRequestId();
		if(connect.containsFuture(key))
		{
			InvokeFuture<Object> future = connect.removeFuture(key);
			//没有找到对应的发送请求，则返回
			if (future==null)
			{
				return;
			}
			if(this.cause!=null)
			{
				//设置异常结果，会出发里面的回调函数
				future.setCause(cause);
				
				if(listener!=null)
					listener.onException(cause);
			}
			else
			{
				future.setResult(response);
			}
		}
		else
		{
			//如果不是consumer主动发送的数据，说明是服务主动搭讪，则调用消息收到
			if(listener!=null)
			{
				ConsumeResult result=(ConsumeResult)listener.onResponse(response);
				//回答consumer的消费情况,相当于服务器调用RPC
				MomRequest request=new MomRequest();
				request.setRequestId(response.getRequestId());
				request.setFromType(RequestResponseFromType.Consumer);
				request.setRequestType(RequestType.ConsumeResult);
				request.setParameters(result);
				
				ctx.writeAndFlush(request);
			}
		}
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		//super.exceptionCaught(ctx, cause);
		this.cause=cause;
		System.out.println("MomHandler caught exception");
		if(listener!=null)
			listener.onException(cause);
	}
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		if(listener!=null)
			listener.onDisconnect(ctx.channel().remoteAddress().toString());
	}
}

