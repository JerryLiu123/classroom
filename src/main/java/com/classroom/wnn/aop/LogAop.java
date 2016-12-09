package com.classroom.wnn.aop;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.classroom.wnn.aop.annotation.Log;


@Aspect
@Component//将bean 实例化到spring中
public class LogAop {

	ThreadLocal<Long> time = new ThreadLocal<Long>();// 线程安全，使用ThreadLocal会在线程使用该变量的时候自动创建一个变量的副本
	ThreadLocal<String> tag=new ThreadLocal<String>();
	
	@Pointcut("@annotation(com.classroom.wnn.aop.annotation.Log)")
	public void log(){
		System.err.println("我是一个切入点");
	}
	
	/**
	 * 在所有标注@Log的地方切入
	 * @param joinPoint
	 */
	
	/**
	 * 在方法执行之前运行
	 * 当配置 args 时，被拦截的方法必须带有 args 中的参数才可呗拦截
	 * @param joinPoint
	 */
//	@Before("log() && args(request, response, datamap)")
//	public void beforeExec(JoinPoint joinPoint, 
//							HttpServletRequest request, 
//							HttpServletResponse response, 
//							Map<String, Object> datamap){
//		System.out.println("===============切入点方法执行之前执行================");
//		time.set(System.currentTimeMillis());
//		tag.set(UUID.randomUUID().toString());
//		if(request != null){
//			System.out.println("URL:"+request.getRequestURL());
//		}
//		
//		info(joinPoint);
//		MethodSignature ms=(MethodSignature) joinPoint.getSignature();
//		Method method=ms.getMethod();
//		System.out.println(method.getAnnotation(Log.class).name()+"--标记--"+tag.get());
//	}
	
	/**
	 * 在方法执行之前运行
	 * @param joinPoint
	 */
	@Before("log()")
	public void beforeExec(JoinPoint joinPoint){
		time.set(System.currentTimeMillis());
		tag.set(UUID.randomUUID().toString());
		
		info(joinPoint);
		MethodSignature ms=(MethodSignature) joinPoint.getSignature();
		Method method=ms.getMethod();
		System.out.println(method.getAnnotation(Log.class).name()+"--标记--"+tag.get());
	}
	/**
	 * 在方法执行之后运行
	 * @param joinPoint
	 */
	@After("log()")
	public void afterExec(JoinPoint joinPoint){
		MethodSignature ms=(MethodSignature) joinPoint.getSignature();
		Method method=ms.getMethod();
		System.out.println("标记为--"+tag.get()+"--的方法"+method.getName()+"运行消耗"+(System.currentTimeMillis()-time.get())+"ms");
	}
	
	@Around("log()")
	public Object aroundExec(ProceedingJoinPoint pjp) throws Throwable{
		//System.out.println("我是Around----开始");
		Object o = pjp.proceed();
		//System.out.println("我是Around----结束");
		return o;
	}

	private void info(JoinPoint joinPoint){
		System.out.println("--------------------------------------------------");
		System.out.println("King:\t"+joinPoint.getKind());
		System.out.println("Target:\t"+joinPoint.getTarget().toString());
		Object[] os=joinPoint.getArgs();
		System.out.println("Args:");
		for(int i=0;i<os.length;i++){
			System.out.println("\t==>参数["+i+"]:\t"+os[i].toString());
		}
		System.out.println("Signature:\t"+joinPoint.getSignature());
		System.out.println("SourceLocation:\t"+joinPoint.getSourceLocation());
		System.out.println("StaticPart:\t"+joinPoint.getStaticPart());
		System.out.println("--------------------------------------------------");
	}
}
