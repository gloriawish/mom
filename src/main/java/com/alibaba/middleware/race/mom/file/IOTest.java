package com.alibaba.middleware.race.mom.file;

import java.io.IOException;

public class IOTest {
	
	
	 private static final String StoreMessage = "Once, there was a chance for me!";
	public static void main(String[] args) throws IOException {
		
		MessageLog log = new MessageLog("test1");
		long startTime = System.currentTimeMillis();
		
		byte[] defaultdata=new byte[1024*4];
		for (int i = 0; i < 10000; i++) {

			long start=System.currentTimeMillis();
			log.SynSave(defaultdata);
			long end=System.currentTimeMillis();
			//System.out.println("save time:"+(end-start));
		}
		
		long endTime=System.currentTimeMillis();
		
		System.out.println("use time:"+(endTime-startTime));
	}
}
