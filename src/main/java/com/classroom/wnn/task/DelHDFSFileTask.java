package com.classroom.wnn.task;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.classroom.wnn.util.HdfsFileSystem;
import com.classroom.wnn.util.SpringContextHelper;

public class DelHDFSFileTask extends Task{
	private static Logger logger = Logger.getLogger(DelHDFSFileTask.class);
	private String path;
	private SpringContextHelper contextHelper;
	
	public DelHDFSFileTask(SpringContextHelper contextHelper, String path) {
		super();
		this.path = path;
		this.contextHelper = contextHelper;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			logger.info("线程删除文件开始"+this.path);
			HdfsFileSystem.deleteFile(this.contextHelper ,this.path);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			logger.error("删除文件异常-获得锁超时"+e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("删除文件异常-IO异常"+e);
		}
		logger.info("线程删除文件结束"+this.path);
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
