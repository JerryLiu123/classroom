package com.classroom.wnn.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义注解,打印日志
 * @author lgh
 */
@Retention(RetentionPolicy.RUNTIME)//此语句表示 此注解表示注解的信息被保留在class文件(字节码文件)中当程序编译时，会被虚拟机保留在运行时
@Target({ElementType.METHOD})//修饰的注解表示该注解只能用来修饰在方法上。
public @interface Log {

	//定义一个变量,默认值为 “”
	String name() default "" ;
}
