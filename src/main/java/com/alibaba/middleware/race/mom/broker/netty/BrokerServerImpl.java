package com.alibaba.middleware.race.mom.broker.netty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import com.alibaba.middleware.race.mom.broker.ConsumerManager;
import com.alibaba.middleware.race.mom.broker.SemaphoreManager;
import com.alibaba.middleware.race.mom.broker.TaskManager;
import com.alibaba.middleware.race.mom.model.MomRequest;
import com.alibaba.middleware.race.mom.model.MomResponse;
import com.alibaba.middleware.race.mom.serializer.RpcDecoder;
import com.alibaba.middleware.race.mom.serializer.RpcEncoder;


/**
 * 在我们的系统中服务端收到的数据只能是MomRequest  客户端收到的数据只能是MomResponse
 * producer 是客户端  consumer 也是客户端   broker是服务器
 * 
 * 所以 producer->broker 是request
 * 所以 consumer->broker 是request
 * broker->consumer 是response broker->producer 是respose
 * 通过以上模型，我们的对数据编解码就可以不变
 * @author zz
 *
 */
//broker 服务器实现类
public class BrokerServerImpl implements BrokerServer {
	
	//订阅关系管理器
	//private ConsumerManager cmanager;
	
	private ServerBootstrap bootstrap;
	
	public BrokerServerImpl() {
		init();
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
		//需要做成保存成为文件的功能，broker重启的时刻可以从文件中恢复
		//cmanager=new ConsumerManager();
		final BrokerHandler handler=new BrokerHandler();
				
		//设置两个监听器
		handler.setConsumerRequestListener(new ConsumerMessageListener());
		handler.setProducerListener(new ProducerMessageListener());
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		//处理事件的线程池
        EventLoopGroup workerGroup = new NioEventLoopGroup(30);
        try 
        {
        	bootstrap = new ServerBootstrap();
        	bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
           .handler(new LoggingHandler(LogLevel.INFO))
           .childHandler(new ChannelInitializer<SocketChannel>() {
                           @Override
                           public void initChannel(SocketChannel ch)
                                    throws Exception {
                            ch.pipeline().addLast(new RpcEncoder(MomResponse.class));
                            ch.pipeline().addLast(new RpcDecoder(MomRequest.class));
                           	ch.pipeline().addLast(handler);
                          }
                     }).option(ChannelOption.SO_KEEPALIVE , true );
       }
       catch (Exception e)
       {
    	   //TODO 异常处理
       }
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		try
		{
			ChannelFuture cfuture = bootstrap.bind(8888).sync();
			
			//创建一个写文件的锁
			SemaphoreManager.createSemaphore("SendTask");
			//创建一个发送ack消息的信号量
			SemaphoreManager.createSemaphore("Ack");
			//恢复之前的发送任务到队列
			TaskManager.RecoverySendTask();			
			
			//启动发送线程
			ExecutorService executorService=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2+2);
			for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
				executorService.execute(new SendThread());
				System.out.println("start sendThread:"+(i+1));
			}
			//启动ack发送线程
			for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
				executorService.execute(new AckSendThread());
				System.out.println("start ack sendThread:"+(i+1));
			}
			
			//启动一个记录发送tps的线程
			executorService.execute(new RecordThread());
			//启动刷磁盘线程
			executorService.execute(new FlushThread());
			
			cfuture.channel().closeFuture().sync();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			// TODO: handle exception
			System.out.println("Broker start error!");
		}
		
		 
	}
	public static void main(String[] args) {
		BrokerServer broker=new BrokerServerImpl();
		broker.start();
	}

}
