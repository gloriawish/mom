package com.alibaba.middleware.race.mom.broker.netty;

import java.util.ArrayList;
import java.util.List;

/**
 * 重发那些之前消费失败的消息的管理器
 * 
 * 主要通过发送方的发送速率来控制 重新发送线程的数量
 * @author zz
 *
 */
public class ResendManager {
	
	
	//正在重发的线程数
	public static int resendThreadNumber=0;
	
	private static List<Long> sendList=new ArrayList<Long>();
	
	public static int recordTime=5;//5秒记录一次值
	
	public static void record()
	{
		sendList.add(SendThread.sendToal.get());
	}
	//获取发送速率
	public static int getSendSpeed()
	{
		if(sendList.size()<2)
			return 0;
		//上次和这次的差值  除以时间就是速度
		long num=sendList.get(sendList.size()-1)-sendList.get(sendList.size()-2);
		
		int speed=(int) (num/10);
		return speed;
	}
	
	public static void startResend()
	{
		new Thread(new ResendThread()).start();
		resendThreadNumber--;
	}
}
