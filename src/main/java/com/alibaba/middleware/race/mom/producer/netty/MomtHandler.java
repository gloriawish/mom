package com.alibaba.middleware.race.mom.producer.netty;

import com.alibaba.middleware.race.mom.model.InvokeFuture;
import com.alibaba.middleware.race.mom.model.MomResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
public class MomtHandler extends ChannelInboundHandlerAdapter  {
	private MomConnection connect;
	private Throwable cause;
	private ConnectListener listener;
	MomtHandler()
	{
		//TODO nothing
	}
	public MomtHandler(MomConnection conn)
	{
		this.connect=conn;
	}
	public MomtHandler(MomConnection conn,ConnectListener listener)
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
			}
			else
			{
				future.setResult(response);
			}
		}
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		this.cause=cause;
		System.out.println("MomHandler caught exception");
	}
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
		System.out.println("disconnect to broker");
		if(listener!=null)
			listener.onDisconnected(ctx.channel().remoteAddress().toString());
	}
}
