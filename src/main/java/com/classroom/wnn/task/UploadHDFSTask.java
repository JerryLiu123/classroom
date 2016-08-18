package com.classroom.wnn.task;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.classroom.wnn.util.Convert;
import com.classroom.wnn.util.HdfsFileSystem;
import com.classroom.wnn.util.constants.Constants;

public class UploadHDFSTask extends Task {
	private static Logger logger = Logger.getLogger(UploadHDFSTask.class);

	private File inputFile;
	private String fileName;
	
	
	public UploadHDFSTask(File inputFile, String fileName) {
		super();
		this.inputFile = inputFile;
		this.fileName = fileName;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//super.run();
		try {
			logger.info("新线程写入hdfs开始------");
			String[] names = this.fileName.split("\\.");
			StringBuffer c = new StringBuffer();
			for(int i=0;i<names.length-1;i++){
				c.append(names[i]);
			}
			this.fileName = Convert.toBase64String(c.toString()+"_"+System.currentTimeMillis())+names[names.length-1];//将文件名加入当前时间时间戳，并进行base64编码
			String path = Constants.hdfsAddress+"/course/"+this.fileName;
			logger.info("新写入hdfs地址为------"+path);
			HdfsFileSystem.createFile(inputFile, path);
			/*下面是要将新的地址目录写入 视频信息表 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("写入hdfs文件出错----"+e);
		}
	}

	@Override
	public Task[] taskCore() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean useDb() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean needExecuteImmediate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String info() {
		// TODO Auto-generated method stub
		return null;
	}

}