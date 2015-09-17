package com.ecnu.test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import com.alibaba.middleware.race.mom.broker.ClientChannelInfo;

public class Test {

	
	public static void main(String[] args) throws InterruptedException {
//		ConcurrentHashMap<String/* client id */, ClientChannelInfo> channelInfoTable =
//	            new ConcurrentHashMap<String, ClientChannelInfo>(16);
//		
//		
//		
//		channelInfoTable.put(null, null);
		
		
		Semaphore sem=new Semaphore(0);
		sem.release(0);
		sem.release(0);
		sem.acquire();
		System.out.println(sem.availablePermits());
	}
}
