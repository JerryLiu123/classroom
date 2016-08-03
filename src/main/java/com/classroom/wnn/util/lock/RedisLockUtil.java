package com.classroom.wnn.util.lock;

import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.classroom.wnn.service.RedisService;

public class RedisLockUtil {

	@Autowired
    private RedisService redisService;
	private static Logger logger = Logger.getLogger(RedisLockUtil.class);
	private static final int LOCK_TIME_OUT = 1*10*1000;//超时时间为一分钟

	/**
	 * 获得锁
	 * @param lock
	 * @return
	 * @throws TimeoutException 
	 */
	@SuppressWarnings("unchecked")
	public String addLock(String lock, Long timeOut) throws TimeoutException{
		String r = null;
		synchronized (this) {
			try {
				Long timeStart = System.currentTimeMillis();//获得开始时间
				boolean isRun = true;
				while (isRun) {
					//尝试加锁，如果成功则返回true
					String timeNow = String.valueOf(System.currentTimeMillis()); //t0
					r = timeNow;
					Boolean flag = redisService.setValueForExist(lock, timeNow);
					if(flag){
						isRun = false;
						logger.info("++++++++++++获得锁++++++++++++");
						break;
					}else{//代表已经有程序获得锁
						/*
						 * 还有一种 可以 只要是有锁就休眠，然后再次进入获得锁的流程
						 * 如果锁等超时，则直接抛异常
						 * */
						String timeOld = redisService.get(lock);//t1 
						if(StringUtils.isEmpty(timeOld)){//为空表示没有锁了，直接进行下次加锁逻辑
							//System.out.println("锁为空");
							if((!timeOut.equals(0L)) && (System.currentTimeMillis() - timeStart) > timeOut){
								throw new TimeoutException("获得锁超时");//锁等超时，超过指定时间则 直接抛出超时异常
							}
							continue;
						}
						if(((Long.valueOf(timeNow) - Long.valueOf(timeOld)) < LOCK_TIME_OUT)){////和当前时间戳相比较，判断有无超时 如果锁没有超时,表示程序不能获得锁
							//System.out.println("已有程序获得锁---"+(Long.valueOf(timeNow) - Long.valueOf(timeOld))+"---"+LOCK_TIME_OUT);
							if((!timeOut.equals(0L)) && (System.currentTimeMillis() - timeStart) > timeOut){
								throw new TimeoutException("获得锁超时");//锁等超时，超过指定时间则 直接抛出超时异常
							}
							Thread.sleep(2000);
							continue;
						}else{
							/*
							 * 1.如果C1 和C2 同时走到这里 那么C1会获得锁并修改 time
							 * 	 C2 会修改time 但是不会获得锁，因为 C2  的  timeOld_ 是获得的C1修改后的time
							 * 	  但是 因为C2已经修改个time 
							 * 	  会导致C1删除自己的锁的时候找不到自己的锁，现在的解决方案是将删除时的 时间判断改为有
							 * 	 一个时间区间
							 *
							 *
							 * 2.如果此时上一个超时的线程把锁释放了，那么C1将取不到timeOld_，并添加当前锁的time，并获得锁
							 * 	 C2将会区到C1添加的时间 并进行比对，发现时间不一样，无法获得锁
							 * 	 但是此时C2已经把C1的时间修改了 所以会出现 “1” 的问题~
							 * 
							 * 也就是说走完下一步之后上一个线程已经无法也不用再释放锁
							 * */
							String timeOld_ = redisService.getSet(lock, timeNow);//t2  //加锁 
							/*
							 * redis的自带操作都是 原子的 所以 应该不存在N个线程同时获得timeOld_并设置 timeNow
							 * 多个线程竞争时，当第一个走到这里的线程因为已经添加了time所以以后的线程不存在timeOld_为空的情况
							 * */
							if(StringUtils.isEmpty(timeOld_)){//为null说明 没有锁,并且值已经设置上了
								isRun = false;
								logger.info("------------获得锁------------");
								break;
							}
							//System.out.println(timeOld_+"----"+timeOld);
							if(timeOld_.equals(timeOld)){
								isRun = false;
								logger.info("============获得锁============");
								break;
							}else{//如果不相同则表示有另一个程序获得锁
								System.out.println("锁超时并且已有另一个程序获得锁");
								if((!timeOut.equals(0L)) && (System.currentTimeMillis() - timeStart) > timeOut){
									throw new TimeoutException("获得锁超时");//锁等超时，超过指定时间则 直接抛出超时异常
								}
								Thread.sleep(2000);
								continue;
							}
						}
					}
				}
				
			} catch (InterruptedException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return r;
	}
	
	/**
	 * 释放锁
	 * @param lock
	 * @return
	 */
	public Boolean unLock(String lock, String value){
		String timeOld = redisService.get(lock);//t1 如果为null  代表 已经没有锁了
		if(StringUtils.isEmpty(timeOld)){//直接返回，不用删除锁
			return true;
		}else{
			//判断是不是自己的锁
			/*
			 * 这里并不是很严谨，允许时间有几毫秒的误差
			 * 但是如果两个程序进入获得锁的时间间隔超过 1000 毫秒，但那时却在同一时间走到了 getSet方法，那么依旧无法释放锁
			 * */
			Long ti = Long.valueOf(value);
			if(timeOld.equals(value) || (ti <= (ti + 1000) && ti >= (ti - 1000))){
				logger.info("------------删除锁------------");
				redisService.del(lock);
			}
			return true;
		}
	}

	public RedisLockUtil() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RedisService getRedisService() {
		return redisService;
	}

	public void setRedisService(RedisService redisService) {
		this.redisService = redisService;
	}
	
}
