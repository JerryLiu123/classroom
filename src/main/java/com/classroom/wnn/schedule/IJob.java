package com.classroom.wnn.schedule;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 定时任务基类，所有定时任务必须继承此抽象类
 * @author lgh
 *
 */
public abstract class IJob extends QuartzJobBean {
	
	public abstract String getCrty();
	
	@Override
	protected abstract void executeInternal(JobExecutionContext context)throws JobExecutionException;
}
