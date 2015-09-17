/**
 * 
 */
package com.alibaba.middleware.race.mom.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Vector;

import com.alibaba.middleware.race.mom.broker.netty.Conf;
import com.alibaba.middleware.race.mom.model.SendTask;
import com.alibaba.middleware.race.mom.store.AllocateMapedFileService;
import com.alibaba.middleware.race.mom.store.MapedFile;
import com.alibaba.middleware.race.mom.store.MapedFileQueue;
import com.alibaba.middleware.race.mom.tool.Tool;


/**
 * @author showki
 *
 */
public  class MessageLog {
	private final int mapSize=1024;
	private MapedFileQueue mapedFileQueue;
	final private String dir ="/"+System.getProperty("user.home")+"/store/";//"$userhome/store/";
//	final private String fileSeparater = System.getProperty("file.separator");
	private FileHandler synFileHandler= null;
	private FileHandler asynFileHandler = null;
	/**
	 * @throws IOException 
	 * 
	 */
	public MessageLog(String fileName) throws IOException {
		AllocateMapedFileService allocateMapedFileService = new AllocateMapedFileService();
	    allocateMapedFileService.start();
	    mapedFileQueue =
	            new MapedFileQueue(dir, mapSize, allocateMapedFileService);
	    mapedFileQueue.load();
		// TODO Auto-generated constructor stub
		File fileDir = new File(dir);
		if (fileDir.exists() == false && fileDir.isDirectory() == false) {
			if (fileDir.mkdirs() == false) {
				throw new IOException("create dir error");
			}
		}
		
		String fullSynPath = dir+fileName+"_syn";
		String fullAsynPath = dir+fileName+"_asyn";
		System.out.println(fullSynPath);
		System.out.println(fullAsynPath);
		File synFile = new File(fullSynPath);
		File asynFile = new File(fullAsynPath);
		try {
			if (!synFile.exists()) {
				synFile.createNewFile();
			}
			if (!asynFile.exists()) {
				asynFile.createNewFile();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String abosoluteSynPath = synFile.getAbsolutePath();
		System.out.println(abosoluteSynPath);
//		synFileHandler = (FileHandler) new FileWRFactory().getRandomAccessFileHandler();
		synFileHandler = new FileWRFactory().getChannelFileHandler();
		synFileHandler.Open(abosoluteSynPath, true);
		
		String abosoluteAsynPath = asynFile.getAbsolutePath();
		System.out.println(abosoluteAsynPath);
//		asynFileHandler = (FileHandler) new FileWRFactory().getRandomAccessFileHandler();
		asynFileHandler = new FileWRFactory().getChannelFileHandler();
		asynFileHandler.Open(abosoluteAsynPath, false);
	}
	
	public boolean AsynSave(byte[] data) {
		return asynFileHandler.AppendObject(data);
	}
	
	public boolean SynSave(List<byte[]> list) {
		byte[] temp = toByteArray(list.get(0).length, 4);
		temp = byteMerger(temp, list.get(0));
		for (int i = 1; i < list.size(); i++) {
			temp = byteMerger(temp, toByteArray(list.get(i).length, 4));
			temp = byteMerger(temp, list.get(i));
		}
		
		return synFileHandler.AppendBytes(temp);
	}
	
	public boolean SynSave(byte[] data) {
		return synFileHandler.AppendObject(data);
	}
	
	public List<SendTask> Restore() throws IOException, ClassNotFoundException {
		List<SendTask> res = new Vector<SendTask>();
		synFileHandler.PrepareForReadNextLine();
		byte[] temp = null;
		while((temp = synFileHandler.ReadNextObject()) != null) {
//			ByteArrayInputStream is = new ByteArrayInputStream(temp);
//			ObjectInputStream in = new ObjectInputStream(is);
//			LogTask task = (LogTask) in.readObject();
			LogTask task = Tool.deserialize(temp, LogTask.class);
			
			res.add(task.getTask());
		}
		
		asynFileHandler.PrepareForReadNextLine();
		while ((temp = asynFileHandler.ReadNextObject()) != null) {
//			ByteArrayInputStream is = new ByteArrayInputStream(temp);
//			ObjectInputStream in = new ObjectInputStream(is);
//			LogTask task = (LogTask) in.readObject();

			LogTask task = Tool.deserialize(temp, LogTask.class);
			boolean isOk = res.remove(task.getTask());
			if (isOk == false) {
//				System.out.println("something error, don't contain this object");
			}
		}
		return res;
	}
	
	static byte[] byteMerger(byte[] byte_1, byte[] byte_2){  
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];  
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);  
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);  
        return byte_3;  
    }
	static byte[] toByteArray(int iSource, int iArrayLen) {
	    byte[] bLocalArr = new byte[iArrayLen];
	    for (int i = 0; (i < 4) && (i < iArrayLen); i++) {
	        bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
	    }
	    return bLocalArr;
	}

	// 将byte数组bRefArr转为一个整数,字节数组的低位是整型的低字节位
	static int toInt(byte[] bRefArr) {
	    int iOutcome = 0;
	    byte bLoop;

	    for (int i = 0; i < bRefArr.length; i++) {
	        bLoop = bRefArr[i];
	        iOutcome += (bLoop & 0xFF) << (8 * i);
	    }
	    return iOutcome;
	}
}
