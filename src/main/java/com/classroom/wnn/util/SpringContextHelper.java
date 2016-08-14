package com.classroom.wnn.util;

import java.io.Serializable;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextHelper implements ApplicationContextAware ,Serializable{
	private static ApplicationContext applicationContext;

	public static Object getBean(String beanName) {
		return applicationContext.getBean(beanName);
	}

//	public static Object getBean(Class className) {
//		return applicationContext.getBean(className);
//	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub
		SpringContextHelper.applicationContext = applicationContext;
	}
	public static ApplicationContext getApplicationContext() {
		if (applicationContext == null) {
			throw new NullPointerException("applicationContext is null   = =!");
		}
		return applicationContext;
	}

}
