package com.ecnu.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import com.alibaba.middleware.race.mom.Message;
import com.alibaba.middleware.race.mom.file.LogTask;
import com.alibaba.middleware.race.mom.file.MessageLog;
import com.alibaba.middleware.race.mom.model.SendTask;
import com.alibaba.middleware.race.mom.tool.ByteObjConverter;
import com.alibaba.middleware.race.mom.tool.Tool;


public class FileTest {
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		
		
		MessageLog log = new MessageLog("");
		
		long startTime=System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			SendTask sendTask = new SendTask();
			
			Message msg=new Message();
			msg.setMsgId(String.valueOf(i));
			msg.setBody("hello word".getBytes());
			sendTask.setMessage(msg);
			sendTask.setGroupId("GROUP-A");
			
			LogTask logTask = new LogTask(sendTask, 0);
			
			byte[] b = Tool.serialize(logTask);
			
			//log.Save(b);
		}
		long endTime=System.currentTimeMillis();
		
		System.out.println((float)(endTime-startTime)/1000F);
		
		startTime=System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			LogTask logTask = null;
			SendTask sendTask = new SendTask();
			
			Message msg=new Message();
			msg.setMsgId(String.valueOf(i));
			sendTask.setMessage(msg);
			sendTask.setGroupId("GROUP-A");
			
			logTask = new LogTask(sendTask, 1);
			
			byte[] b = Tool.serialize(logTask);
			
			//log.Save(b);
		}
		endTime=System.currentTimeMillis();
		
		System.out.println((float)(endTime-startTime)/1000F);
		startTime=System.currentTimeMillis();
		List<SendTask> list = log.Restore();
		System.out.println(list.size());
		endTime=System.currentTimeMillis();
			
		System.out.println((float)(endTime-startTime)/1000F);
		
		
	}
}
