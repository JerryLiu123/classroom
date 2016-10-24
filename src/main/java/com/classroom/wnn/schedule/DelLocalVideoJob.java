package com.classroom.wnn.schedule;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.classroom.wnn.model.BiVideoInfo;
import com.classroom.wnn.service.RedisService;
import com.classroom.wnn.service.VideoService;

/**
 * 定时删除本地磁盘中已上传的视频文件
 * @author lgh
 */
@DisallowConcurrentExecution
public class DelLocalVideoJob extends IJob implements org.quartz.StatefulJob{

	private static Logger logger = Logger.getLogger(DelLocalVideoJob.class);
	
	@Autowired
	VideoService videoService;
	
	@Override
	public String getCrty() {
		// TODO Auto-generated method stub
		//每5分钟运行一次
		return "0 0/1 * * * ?";
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub
		logger.info("开始本地文件清理定时任务");
		videoService.delIsHDFSIsLocal();
		logger.info("结束本地文件清理定时任务");
	}

}
