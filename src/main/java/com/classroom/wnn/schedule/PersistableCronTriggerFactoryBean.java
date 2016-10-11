package com.classroom.wnn.schedule;

import java.text.ParseException;

import org.springframework.scheduling.quartz.CronTriggerFactoryBean;

public class PersistableCronTriggerFactoryBean extends CronTriggerFactoryBean {

	@Override
	public void afterPropertiesSet() throws ParseException {
		// TODO Auto-generated method stub
		super.afterPropertiesSet();
		getJobDataMap().remove("jobDetail");
	}
}
