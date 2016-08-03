package com.classroom.wnn.schedule;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory
		implements ApplicationContextAware {

	  private transient AutowireCapableBeanFactory beanFactory;

	  public void setApplicationContext(final ApplicationContext context) {
	    beanFactory = context.getAutowireCapableBeanFactory();
	  }

	  @Override
	  protected Object createJobInstance(final TriggerFiredBundle bundle)
	      throws Exception {
	    final Object job = super.createJobInstance(bundle);
	    beanFactory.autowireBean(job);
	    //System.out.println("AutowiringSpringBeanJobFactory-------------------");
	    return job;
	  }

}
