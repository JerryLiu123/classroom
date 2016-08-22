package com.classroom.wnn.schedule;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.classroom.wnn.model.BiVideoInfo;
import com.classroom.wnn.service.RedisService;
import com.classroom.wnn.service.VideoService;

/**
 * 因暂时无法解决分布式系统下找不到文件的问题，暂时弃用
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
