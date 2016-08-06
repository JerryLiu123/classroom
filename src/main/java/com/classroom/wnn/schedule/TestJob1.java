package com.classroom.wnn.schedule;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class TestJob1 extends IJob implements org.quartz.StatefulJob{
	Logger logger = Logger.getLogger(TestJob1.class);
	
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		// TODO Auto-generated method stub
		logger.info("-----------------------开始调度任务1");
//		for(int i =0;i<10000;i++){
//			logger.info("-----------------------开始调度任务1----这是第"+i+"次执行");
//			try {
//				Thread.sleep(1*1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}

	@Override
	public String getCrty() {
		// TODO Auto-generated method stub
		return "0/2 * * * * ?";
	}
}
