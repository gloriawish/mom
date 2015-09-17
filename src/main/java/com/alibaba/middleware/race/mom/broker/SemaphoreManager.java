package com.alibaba.middleware.race.mom.broker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;




//信号量管管理
public class SemaphoreManager {
	
	//每一个队列一个信号量管理里面的消费同步
	private static Map<String,Semaphore> semMap=new HashMap<String, Semaphore>();
	
	//创建一个信号量
	public static void createSemaphore(String key)
	{
		if(semMap.containsKey(key))
			return;
		Semaphore sem=new Semaphore(0);
		semMap.put(key, sem);
	}
	
	public static void increase(String key)
	{
		semMap.get(key).release();
	}
	
	public static void decrease(String key)
	{
		try 
		{
			semMap.get(key).acquire();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	
}
