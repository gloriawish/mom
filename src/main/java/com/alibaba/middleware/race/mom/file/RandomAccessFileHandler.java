package com.alibaba.middleware.race.mom.file;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class RandomAccessFileHandler implements FileHandler{
//	final private String lineSeparater = System.getProperty("line.separator");

	RandomAccessFile randomFile;
	public RandomAccessFileHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void Open(String filePath, boolean isForce) {
		// TODO Auto-generated method stub
		try {
			if (isForce) {
				randomFile = new RandomAccessFile(filePath, "rwd");
			} else {
				randomFile = new RandomAccessFile(filePath, "rw");
			}
			randomFile.seek(randomFile.length());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean Write(String filePath, int position, byte[] data) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] Read(int position, int length) {
		// TODO Auto-generated method stub
		byte[] bytes = null;
		try {
			long fileLength = randomFile.length(); 
			if (position + length > fileLength) {
				System.err.println(" don't have enouch data from "+position);
				return null;
			}
			
			randomFile.seek(position); 
			bytes = new byte[length]; 
			 
			 int byteread = randomFile.read(bytes);
			 if (byteread != length) {
				 System.err.println("Read "+length+"bytes from line "+":"+position+" failed");
			 }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bytes;
	}

	@Override
	public boolean AppendObject(byte[] data) {
		// TODO Auto-generated method stub
		
//		long fileLength;
		try {
//			fileLength = randomFile.length();		 
//			randomFile.seek(fileLength); 
//			randomFile.writeInt(data.length);
//			randomFile.write(data);
			
			byte[] len = toByteArray(data.length,4);
			byte[] res = byteMerger(len,data);
			randomFile.write(res);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} 
		return true;
	}
	
	public boolean AppendBytes(byte[] data) {
		try {
			randomFile.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public byte[] ReadNextObject() {
		byte[] temp = null;
		try {
			byte[] len = new byte[4];
			long num = randomFile.read(len);
			if (num == -1)	// if file end
				return null;
			
			int length = toInt(len);
			temp = new byte[length];
			int readNum = randomFile.read(temp);
			if (readNum != length) {
				System.err.println("read not enough bytes from file");
				return null;
			}
		} catch (EOFException e) {
			// TODO: handle exception
			System.out.println("file end");
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("io failed");
			e.printStackTrace();
		} 
		
		return temp;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		try {
			randomFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see log.FileHandler#PrepareForReadNextLine()
	 */
	@Override
	public void PrepareForReadNextLine() {
		// TODO Auto-generated method stub
		try {
			randomFile.seek(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static byte[] toByteArray(int iSource, int iArrayLen) {
	    byte[] bLocalArr = new byte[iArrayLen];
	    for (int i = 0; (i < 4) && (i < iArrayLen); i++) {
	        bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
	    }
	    return bLocalArr;
	}

	// 将byte数组bRefArr转为一个整数,字节数组的低位是整型的低字节位
	public static int toInt(byte[] bRefArr) {
	    int iOutcome = 0;
	    byte bLoop;

	    for (int i = 0; i < bRefArr.length; i++) {
	        bLoop = bRefArr[i];
	        iOutcome += (bLoop & 0xFF) << (8 * i);
	    }
	    return iOutcome;
	}

	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){  
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];  
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);  
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);  
        return byte_3;  
    }
}