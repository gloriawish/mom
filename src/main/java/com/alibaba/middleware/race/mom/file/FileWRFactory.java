package com.alibaba.middleware.race.mom.file;

import com.alibaba.middleware.race.mom.file.FileHandler;
import com.alibaba.middleware.race.mom.file.RandomAccessFileHandler;

public class FileWRFactory {
	public FileWRFactory() {
		// TODO Auto-generated constructor stub
	}
	FileHandler GetDefaultFileHandler(String filePath) {
//		return new DefaulteFileHandler();
		assert(false);
		return null;
	}
	
	FileHandler getRandomAccessFileHandler() {
		return new RandomAccessFileHandler();
	}
	
	FileHandler getChannelFileHandler() {
		return new ChannelFileHander();
	}
}
