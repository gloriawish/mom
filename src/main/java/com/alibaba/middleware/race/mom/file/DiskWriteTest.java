/**
 * 
 */
package com.alibaba.middleware.race.mom.file;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.middleware.race.mom.Message;
import com.alibaba.middleware.race.mom.model.SendTask;


/**
 * @author showki
 *
 */
public class DiskWriteTest {
	
	public static AtomicInteger toal=new AtomicInteger(0);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<Thread> list=new ArrayList<Thread>();
		int threadNum=1;
		for (int i = 0; i <threadNum; i++) {
			list.add(new Thread(new SimpleThread()));
		}
		long start=System.currentTimeMillis();
		for (int i = 0; i <threadNum; i++) {
			list.get(i).start();
		}
		for (int i = 0; i <threadNum; i++) {
			try {
				list.get(i).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		long end=System.currentTimeMillis();
		System.out.println(toal.get()/(float)(end-start)*1000F);
	}

	
}
class SimpleThread implements Runnable
{
//	static MessageLog log=null;
//	static
//	{
//		try {
//			log=new MessageLog(Thread.currentThread().getId()+"");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	public static byte[] toByteArray(int iSource, int iArrayLen) {
	    byte[] bLocalArr = new byte[iArrayLen];
	    for (int i = 0; (i < 4) && (i < iArrayLen); i++) {
	        bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
	    }
	    return bLocalArr;
	}

	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){  
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];  
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);  
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);  
        return byte_3;  
    }
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {

			MessageLog log=new MessageLog(Thread.currentThread().getId()+"");
			
			for (int i = 0; i < 10000; ++i) {
				
				Message msg = new Message();
				msg.setMsgId(String.valueOf(i));
				msg.setBody("sfsefs".getBytes());
				
				SendTask sendTask = new SendTask();
				sendTask.setGroupId(String.valueOf(i));
				sendTask.setMessage(msg);
				
				LogTask logTask = new LogTask(sendTask, 0);
				
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(os);
				out.writeObject(logTask);
				byte[] b = os.toByteArray();
				
				
				byte[] len = toByteArray(b.length,4);
				byte[] res = byteMerger(len,b);
				long start=System.currentTimeMillis();
				log.SynSave(res);
				long end=System.currentTimeMillis();
				
				System.out.println("save use:"+(end-start));
				//				randomFile.write(b);
				DiskWriteTest.toal.incrementAndGet();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}