package com.alibaba.middleware.race.mom.broker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.middleware.race.mom.tool.QueueFile;

/**
 * 队列管理器
 * @author sei.zz
 * 对应某个队列的key 是由 groupid + topic 构成的
 */
public class QueueManager {
	
	//记录没有一个队列最后一次发送的时间
	private static ConcurrentHashMap<String,Long> timeTable=new ConcurrentHashMap<String, Long>();
	//队列管理器
	private static ConcurrentHashMap<String/* queueId */, QueueFile> queueTable =
            new ConcurrentHashMap<String, QueueFile>();
	
	//把key值课 group映射一下
	private static ConcurrentHashMap<String/* queueId */, String> groupTable =
            new ConcurrentHashMap<String, String>();
	
	//private static String basePath="/home/admin/zhujun_java";
	private static String basePath="D:\\queue";
	
	//根据队列名获取文件队列
	public static QueueFile findQueue(String key)
	{
		return queueTable.get(key);
	}
	
	public static QueueFile createQueue(String key) throws IOException
	{
		if(findQueue(key)==null)
		{
			QueueFile queue=new QueueFile(new File(basePath+"\\"+key));
			queueTable.put(key, queue);
			return queue;
		}
		return findQueue(key);
	}
	//记录group信息
	public static void recordGroup(String key,String group)
	{
		groupTable.put(key, group);
	}
	
	public static String getGroup(String key)
	{
		return groupTable.get(key);
	}
	
	//获取队列文件目录的文件名列表
	public static List<String> getQueueFileName() {  
		List<String> vecFile = new ArrayList<String>();  
        File file = new File(basePath);  
        File[] subFile = file.listFiles();
        for (int i = 0; i < subFile.length; i++) {  
            //判断是否为文件
            if (!subFile[i].isFile())
            {  
                String fileName = subFile[i].getName();  
                vecFile.add(fileName);
            }  
        }  
        return vecFile;  
    }
	
	//初始化队列，重启服务的时候调用
	public static void initQueue() throws IOException
	{
		//在重启的时候重新加载队列
		for (String fileName : getQueueFileName()) 
		{
			if(!queueTable.containsKey(fileName))
			{
				QueueFile queue=new QueueFile(new File(basePath+"\\"+fileName));
				queueTable.put(fileName, queue);
			}
		}
	}
	public static void deleteQueue() throws IOException
	{
		//在重启的时候重新加载队列
		for (String fileName : getQueueFileName()) 
		{
			File file=new File(fileName);
			if(file.exists())
				file.delete();
		}
	}
	
	//获取所有的队列名
	public static List<String> getAllQueue()
	{
		List<String> list=new ArrayList<String>();
		Iterator<Entry<String, QueueFile>> it = queueTable.entrySet().iterator();
        while (it.hasNext())
        {
        	Map.Entry<String, QueueFile> next = it.next();
        	list.add(next.getKey());
        }
        return list;
	}
	
	//记录时间
	public static void recordTime(String key,long time)
	{
		if(findQueue(key)!=null)
		{
			timeTable.put(key, time);
		}
	}
	//获取记录的时间
	public static Long getRecordTime(String key)
	{
		return timeTable.get(key);
	}
	
	
}
