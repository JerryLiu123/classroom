package com.classroom.wnn.schedule;

import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailAwareTrigger;

public class PersistableCronTriggerFactoryBean extends CronTriggerFactoryBean {

	@Override
	public void afterPropertiesSet() {
		// TODO Auto-generated method stub
		super.afterPropertiesSet();
		getJobDataMap().remove(JobDetailAwareTrigger.JOB_DETAIL_KEY);
	}
}
