package com.classroom.wnn.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextHelper implements ApplicationContextAware {
	private static ApplicationContext applicationContext;

	public Object getBean(String beanName) {
		return applicationContext.getBean(beanName);
	}

	public Object getBean(Class className) {
		return applicationContext.getBean(className);
	}

	public void setApplicationContext(ApplicationContext _applicationContext) throws BeansException {

		applicationContext = _applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		if (applicationContext == null) {
			throw new NullPointerException("applicationContext is null   = =!");
		}
		return applicationContext;
	}

}
