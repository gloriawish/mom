/**
 * 
 */
package com.alibaba.middleware.race.mom.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author showki
 *
 */
public class ChannelFileHander implements FileHandler{

	private FileChannel channel = null;  
    private RandomAccessFile fs = null; 
    private boolean isForce = false;
    private ByteBuffer buf = ByteBuffer.allocate(4*1024*8);  // 4KB
	/* (non-Javadoc)
	 * @see log.FileHandler#Open(java.lang.String, boolean)
	 */
	@Override
	public void Open(String filePath, boolean isForce) {
        try {
        	fs = new RandomAccessFile(filePath, "rw");
        	channel = fs.getChannel();
        	this.isForce = isForce;
//        	channel = FileChannel.open(filePath, "DSYC"|"READ");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}    
	}

	/* (non-Javadoc)
	 * @see log.FileHandler#Write(java.lang.String, int, byte[])
	 */
	@Override
	public boolean Write(String filePath, int position, byte[] data) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see log.FileHandler#Read(int, int)
	 */
	@Override
	public byte[] Read(int position, int length) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see log.FileHandler#PrepareForReadNextLine()
	 */
	@Override
	public void PrepareForReadNextLine() {
		// TODO Auto-generated method stub
		try {
			channel.position(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see log.FileHandler#ReadNextObject()
	 */
	@Override
	public byte[] ReadNextObject() {
		// TODO Auto-generated method stub 
        try {  
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            while ((channel.read(byteBuffer)) > 0) ; 
            if (byteBuffer.array().length != 4) {
            	System.out.println("Read length info failed");
            	return null;
            }
            int len = MessageLog.toInt(byteBuffer.array());
            ByteBuffer dataBuffer = ByteBuffer.allocate(len);
            while ((channel.read(dataBuffer)) > 0);
            if (dataBuffer.array().length > 0) {
//            	System.out.println("Read data successfully");
            	return dataBuffer.array();
            } else {
            	System.out.println("Read data failed");
				return null;
			}
        } catch (IOException e) {  
            e.printStackTrace(); 
        }  
        return null;
	}

	/* (non-Javadoc)
	 * @see log.FileHandler#AppendBytes(byte[])
	 */
	@Override
	public boolean AppendBytes(byte[] data)  {
		// TODO Auto-generated method stub
		buf.clear();
		buf.put(data);
		buf.flip();
//		System.out.println("data is:"+data.toString()+", in buf:"+buf.array().toString());
		try {
			while(buf.hasRemaining()) {
				channel.write(buf);
			}
			if (isForce) {
				channel.force(false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see log.FileHandler#AppendObject(byte[])
	 */
	@Override
	public boolean AppendObject(byte[] data) {
		// TODO Auto-generated method stub
		byte[] len = MessageLog.toByteArray(data.length,4);
		byte[] res = MessageLog.byteMerger(len,data);
		
		return AppendBytes(res);
	}

	/* (non-Javadoc)
	 * @see log.FileHandler#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub
		try {
			channel.close();
			fs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
