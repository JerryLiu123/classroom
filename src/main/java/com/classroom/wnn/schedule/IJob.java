package com.classroom.wnn.schedule;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public abstract class IJob extends QuartzJobBean {
	
	public abstract String getCrty();
	
	@Override
	protected abstract void executeInternal(JobExecutionContext context)throws JobExecutionException;
}
