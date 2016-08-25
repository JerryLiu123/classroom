package com.classroom.wnn.schedule;

import java.util.List;

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
public class DelLocalVideoJob extends IJob implements org.quartz.StatefulJob{

	@Autowired
	VideoService videoService;
	
	@Override
	public String getCrty() {
		// TODO Auto-generated method stub
		//每5分钟运行一次
		return "0 0/5 * * * ?";
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub
		videoService.delIsHDFSIsLocal();
	}

}
