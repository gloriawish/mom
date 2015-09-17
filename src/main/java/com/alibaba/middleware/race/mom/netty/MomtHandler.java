package com.alibaba.middleware.race.mom.netty;

import com.alibaba.middleware.race.mom.model.MomResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
public class MomtHandler extends ChannelInboundHandlerAdapter  {
	private MomConnection connect;
	private Throwable cause;
	MomtHandler()
	{
		//TODO nothing
	}
	public MomtHandler(MomConnection conn)
	{
		this.connect=conn;
	}
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		System.out.println("connected on server:"+ctx.channel().localAddress().toString());
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
			if (future==null)
			{
				return;
			}
			if(this.cause!=null)
			{
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
		super.exceptionCaught(ctx, cause);
		this.cause=cause;
		cause.printStackTrace();
	}
}
