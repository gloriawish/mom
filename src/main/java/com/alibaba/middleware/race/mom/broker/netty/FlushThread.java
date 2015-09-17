package com.alibaba.middleware.race.mom.broker.netty;

import java.util.concurrent.TimeUnit;


/**
 * 刷磁盘线程  当收到多个生产者消息的时候缓存满 的时候刷磁盘，然后唤醒等待刷磁盘的线程  也就是等待发送ACK的线程将他们唤醒
 * @author sei.zz
 *
 */
public class FlushThread implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true)
		{
			try 
			{
				long start =System.currentTimeMillis();
				//等待一段时间，是否收集齐一定数量的消息
				if (!FlushTool.semp.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
					synchronized (FlushTool.syncObj) 
					{
						//TODO 把cacheList里面的数据刷入磁盘
						//FlushTool.logWriter.log("time out");
						FlushTool.flush();
					}
					continue;
				}
				else
				{
					long end=System.currentTimeMillis();
					FlushTool.logWriter.log("collect all:use time"+(end-start)+" num:"+FlushTool.cacheList.size());
					synchronized (FlushTool.syncObj) 
					{
						//TODO 把cacheList里面的数据刷入磁盘
						FlushTool.flush();
					}
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException();
			}
		}
	}
}
