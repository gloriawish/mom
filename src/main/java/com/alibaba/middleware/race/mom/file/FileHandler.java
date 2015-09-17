package com.alibaba.middleware.race.mom.file;


public interface FileHandler {
	public void Open(String filePath, boolean isForce);
	public boolean Write(String filePath, int position, byte[] data);
	public byte[] Read(int position ,int length);
	public void PrepareForReadNextLine();
	public byte[] ReadNextObject();
	public boolean AppendBytes(byte[] data);
	public boolean AppendObject(byte[] data);
	public void close();
}


