package com.alibaba.middleware.race.mom.producer.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.alibaba.middleware.race.mom.SendCallback;
import com.alibaba.middleware.race.mom.SendResult;
import com.alibaba.middleware.race.mom.model.InvokeFuture;
import com.alibaba.middleware.race.mom.model.InvokeListener;
import com.alibaba.middleware.race.mom.model.MomRequest;
import com.alibaba.middleware.race.mom.model.MomResponse;
import com.alibaba.middleware.race.mom.serializer.RpcDecoder;
import com.alibaba.middleware.race.mom.serializer.RpcEncoder;

/**
 * 连接的具体实现类
 * @author sei.zz
 *
 */
public class MomNettyConnection implements MomConnection {

	private InetSocketAddress inetAddr;
	
	private volatile Channel channel;
	
	//助理类
	private  ChannelInboundHandlerAdapter handle;
	
	private Map<String, InvokeFuture<Object>> futrues=new ConcurrentHashMap<String, InvokeFuture<Object>>();
	//连接数组
	private Map<String, Channel> channels=new ConcurrentHashMap<String, Channel>();
	
	private Bootstrap bootstrap;
	
	private long timeout=10000;//默认超时
	
	private boolean connected=false;
	MomNettyConnection()
	{
		
	}
	public MomNettyConnection(String host,int port)
	{
		inetAddr=new InetSocketAddress(host,port);
	}
	//设置要处理连接的类
	public void setHandle(ChannelInboundHandlerAdapter handle) {
		this.handle = handle;
	}
	private Channel getChannel(String key)
	{
		return channels.get(key);
	}
	@Override
	public void init() {
		try 
        {	EventLoopGroup group = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                    	channel.pipeline().addLast(new RpcDecoder(MomResponse.class));
                    	channel.pipeline().addLast(new RpcEncoder(MomRequest.class));
                        channel.pipeline().addLast(handle);
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true);
        }
        catch (Exception ex) 
        {
        	ex.printStackTrace();
        }
	}

	@Override
	public void connect() {
		//连接的时候初始化
		if(handle!=null)
			init();
		else
		{
			System.out.println("handle is null");
			System.exit(0);
		}
		try
		{
			ChannelFuture future = bootstrap.connect(this.inetAddr).sync();
			channels.put(this.inetAddr.toString(), future.channel()); 
			connected=true;
		} 
		catch(InterruptedException e) 
		{
			e.printStackTrace();
		}
		/*
		ChannelFuture future=bootstrap.connect(this.inetAddr);
		future.addListener(new ChannelFutureListener(){
			@Override
			public void operationComplete(ChannelFuture cfuture) throws Exception {
				 Channel channel = cfuture.channel();
				 //添加进入连接数组
			     channels.put(channel.remoteAddress().toString(), channel); 
			     System.out.println(channel.remoteAddress().toString());
			}
		});*/
	}

	@Override
	public void connect(String host, int port) {
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
		future.addListener(new ChannelFutureListener(){
			@Override
			public void operationComplete(ChannelFuture cfuture) throws Exception {
				 Channel channel = cfuture.channel();
				 //添加进入连接数组
			     channels.put(channel.remoteAddress().toString(), channel); 
			}
		});
	}

	@Override
	public Object Send(MomRequest request) {//同步发送消息给服务器，并且收到服务器结果
		if(channel==null)
			channel=getChannel(inetAddr.toString());
		if(channel!=null)
		{	
			final InvokeFuture<Object> future=new InvokeFuture<Object>();
			futrues.put(request.getRequestId(), future);
			//设置这次请求的ID
			future.setRequestId(request.getRequestId());
			ChannelFuture cfuture=channel.writeAndFlush(request);
			cfuture.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture rfuture) throws Exception {
					if(!rfuture.isSuccess()){
						future.setCause(rfuture.cause());
					}
				}
			});
			try
			{
				Thread.sleep(0);
				Object result=future.getResult(timeout, TimeUnit.MILLISECONDS);
				return result;
			}
			catch(RuntimeException e)
			{
				throw e;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			finally
			{
				//这个结果已经收到
				futrues.remove(request.getRequestId());
			}
		}
		else
		{
			return null;
		}
	}
	/**
	 * 发送并且回调函数
	 */
	public void Send(MomRequest request,final SendCallback listener) {
		if(channel==null)
			channel=getChannel(inetAddr.toString());
		if(channel!=null)
		{	
			final InvokeFuture<Object> future=new InvokeFuture<Object>();
			futrues.put(request.getRequestId(), future);
			//设置这次请求的ID
			future.setRequestId(request.getRequestId());
			//设置回调函数
			future.addInvokerListener(new InvokeListener<Object>() {
				@Override
				public void onResponse(Object t) {
					// TODO Auto-generated method stub
					MomResponse response=(MomResponse)t;
					//回调函数
					listener.onResult((SendResult)response.getResponse());
					
				}
			});
			ChannelFuture cfuture=channel.writeAndFlush(request);
			cfuture.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture rfuture) throws Exception {
					if(!rfuture.isSuccess()){
						future.setCause(rfuture.cause());
					}
				}
			});
			try
			{
				//Object result=future.getResult(timeout, TimeUnit.MILLISECONDS);
			}
			catch(RuntimeException e)
			{
				throw e;
			}
			finally
			{
				//移除已经收到的消息
				//futrues.remove(request.getRequestId());
			}
		}
	}
	@Override
	public void close() {
		// TODO Auto-generated method stub
		if(channel!=null)
			try {
				channel.close().sync();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return connected;
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return (null == channel) || !channel.isOpen()
				|| !channel.isWritable() || !channel.isActive();
	}

	@Override
	public boolean containsFuture(String key) {
		// TODO Auto-generated method stub
		if(key==null)
			return false;
		return futrues.containsKey(key);
	}

	@Override
	public InvokeFuture<Object> removeFuture(String key) {
		// TODO Auto-generated method stub
		if(containsFuture(key))
			return futrues.remove(key);
		else
			return null;
	}
	@Override
	public void setTimeOut(long timeout) {
		// TODO Auto-generated method stub
		this.timeout=timeout;
	}

}
