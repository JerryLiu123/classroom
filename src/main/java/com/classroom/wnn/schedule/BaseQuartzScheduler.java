package com.classroom.wnn.schedule;

import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import com.classroom.wnn.util.AopTargetUtils;


public class BaseQuartzScheduler {
	
	
	@Autowired
	private ApplicationContext context;
	
	private StdSchedulerFactory schedulerFactoryBean;
	private Scheduler scheduler;
	
	Logger logger = Logger.getLogger(BaseQuartzScheduler.class);
	
	public void doWork() throws Exception{
		logger.info("---------初始化所有定时任务----------");
		//获得所有任务
		Map<String, IJob> map_pluMap = context.getBeansOfType(IJob.class);
//		Trigger[] triggers = new Trigger[map_pluMap.size()];
//		JobDetail[] jobDetails = new JobDetail[map_pluMap.size()];
		logger.info("---------定时任务数量为:"+map_pluMap.size()+"--------");
		for (String key : map_pluMap.keySet()) {
//			int i=0;
			IJob p = (IJob) AopTargetUtils.getTarget(map_pluMap.get(key));
			
			JobDetailFactoryBean rollbackOrderStatus = new JobDetailFactoryBean();
			rollbackOrderStatus.setRequestsRecovery(true);
			rollbackOrderStatus.setJobClass(p.getClass());
			rollbackOrderStatus.setDurability(true);
			rollbackOrderStatus.setGroup("myGroup");
			rollbackOrderStatus.setName("job_"+key);
			rollbackOrderStatus.afterPropertiesSet();
			
			// 定义业务作业
			JobDetail job = rollbackOrderStatus.getObject();
			
			
			PersistableCronTriggerFactoryBean rollbackOrderStatusTrigger = new PersistableCronTriggerFactoryBean();
			rollbackOrderStatusTrigger.setJobDetail(job);
			rollbackOrderStatusTrigger.setCronExpression(p.getCrty());
			rollbackOrderStatusTrigger.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
			rollbackOrderStatusTrigger.setName("trigger_"+key);
			rollbackOrderStatusTrigger.afterPropertiesSet();
			try {
				scheduler.scheduleJob(job, rollbackOrderStatusTrigger.getObject());
			} catch (Exception e) {
				// TODO: handle exception
				logger.error("---------数据库已存在当前实例--------"+key);
			}
			
		}
		//schedulerFactoryBean.setTriggers(triggers);
		//schedulerFactoryBean.setJobDetails(jobDetails);
		scheduler.start();
	}

	public StdSchedulerFactory getSchedulerFactoryBean() {
		return schedulerFactoryBean;
	}

	public void setSchedulerFactoryBean(StdSchedulerFactory schedulerFactoryBean) {
		this.schedulerFactoryBean = schedulerFactoryBean;
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	/*动态操作*/
	/**
	 * 停止一个 调度
	 * @author lgh
	 * @date 创建时间：2016年2月16日 上午10:16:13 
	 * @return 1 成功 .0 不存在改实例 -1异常
	 */
	public int pauseTrigger(String triggerName, String triggerGroup){
		try {
			
			TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
			if(scheduler.checkExists(triggerKey)){
				scheduler.pauseTrigger(triggerKey);
				return 1;
			}else{
				return 0;
			}
			
//			JobKey jobKey = JobKey.jobKey("job_testJob1");
//			System.err.println("------check Job------"+scheduler.checkExists(jobKey));
//			if(scheduler.checkExists(jobKey)){
//				scheduler.pauseJob(jobKey);
//				System.err.println("停止job");
//			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("job 暂停失败");
			return -1;
		}
	}
	
	/**
	 * 开始一个调度
	 * @author lgh
	 * @date 创建时间：2016年2月16日 上午10:36:16 
	 * @version 1.0 
	 * @return 1 成功 .0 不存在改实例 -1异常
	 */
	public int srartTrigger(String triggerName, String triggerGroup){
		TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
		try {
			
			if(scheduler.checkExists(triggerKey) ){
				scheduler.resumeTrigger(triggerKey);
				return 1;
			}else{
				return 0;
			}
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e);
			return -1;
		}
		
	}
	/**
	 * 停止一个 任务
	 * @author lgh
	 * @date 创建时间：2016年2月16日 上午10:16:13 
	 * @return 1 成功 .0 不存在改实例 -1异常
	 */
	public int pauseJob(String jobName, String jobGroup){
		try {
			
			JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
			System.err.println("------check Job------"+scheduler.checkExists(jobKey));
			if(scheduler.checkExists(jobKey)){
				scheduler.pauseJob(jobKey);
				System.err.println("停止job");
				return 1;
			}else{
				return 0;
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("job 暂停失败");
			return -1;
		}
	}
	/**
	 * 开始一个任务
	 * @author lgh
	 * @date 创建时间：2016年2月16日 上午10:36:16 
	 * @version 1.0 
	 * @return 1 成功 .0 不存在改实例 -1异常
	 */
	public int srartJob(String jobName, String jobGroup){
		JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
		try {
			
			if(scheduler.checkExists(jobKey) ){
				scheduler.resumeJob(jobKey);
				return 1;
			}else{
				return 0;
			}
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e);
			return -1;
		}
		
	}
	/**
	 * 添加一个任务 
	 * @author lgh
	 * @date 创建时间：2016年2月16日 下午12:59:48 
	 * @version 1.0 
	 * @return 1 成功 0当前实例已存在 -1异常
	 */
	public int addJob(String jobName, String jobGroup, Class job, String time, String triggerName, String triggerGroup){
		try {
			String jobName_ = "job_"+jobName;
			String triggerName_ = "trigger_"+triggerName;
			TriggerKey triggerKey = TriggerKey.triggerKey(triggerName_, triggerGroup);
			JobKey jobKey = JobKey.jobKey(jobName_, jobGroup);
			if(scheduler.checkExists(jobKey) || scheduler.checkExists(triggerKey)){
				return 0;
			}
			if(StringUtils.isEmpty(time)){
				return -1;
			}
			
			JobDetailFactoryBean rollbackOrderStatus = new JobDetailFactoryBean();
			rollbackOrderStatus.setRequestsRecovery(true);
			rollbackOrderStatus.setJobClass(job);
			rollbackOrderStatus.setDurability(true);
			rollbackOrderStatus.setGroup(jobGroup);
			rollbackOrderStatus.setName(jobName_);
			rollbackOrderStatus.afterPropertiesSet();
			
			// 定义业务作业
			JobDetail job2 = rollbackOrderStatus.getObject();
			
			
			PersistableCronTriggerFactoryBean rollbackOrderStatusTrigger = new PersistableCronTriggerFactoryBean();
			rollbackOrderStatusTrigger.setJobDetail(job2);
			rollbackOrderStatusTrigger.setCronExpression(time);
			rollbackOrderStatusTrigger.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
			rollbackOrderStatusTrigger.setName(triggerName_);
			rollbackOrderStatusTrigger.afterPropertiesSet();
		
			scheduler.scheduleJob(job2, rollbackOrderStatusTrigger.getObject());
			if(scheduler.isShutdown()){
				scheduler.start();
			}
		}catch (Exception e) {
				// TODO: handle exception
				logger.error("---------数据库已存在当前实例--------");
				return -1;
		}
		return 1;
	}
	
    /** 
     * 修改一个任务的触发时间 
     * @param triggerName 
     * @param triggerGroupName 
     * @param time 
     * @author lgh
     * @date 2016年2月16日13:10:10 
     */ 
    public int modifyJobTime(String triggerName,  
            String triggerGroup, String time) {  
        try {
        	TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
        	CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        	if (trigger == null) {  
                return 0;  
            }  
            String oldTime = trigger.getCronExpression();
            //表达式调度构建器
            System.err.println("oidTime----"+oldTime);
            if (!oldTime.equalsIgnoreCase(time)) {
            	CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(time);
                // 修改时间 
            	trigger = trigger.getTriggerBuilder().withIdentity(triggerKey)
            		    .withSchedule(scheduleBuilder).build();
                // 重启触发器  
            	scheduler.rescheduleJob(triggerKey, trigger);  
            }
            return 1;
        } catch (Exception e) {
        	e.printStackTrace();
        	return -1;
        }  
    }
    
    /**
     * 删除一个job
     * @param jobName
     * @param jobGroup
     * @param triggerName
     * @param triggerGroup
     * @return 0 不存在 1 成功 -1 失败
     */
    public int removeJob(String jobName, String jobGroup,  
            String triggerName, String triggerGroup){
    	try {
			TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
			JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
		
			if( !(scheduler.checkExists(jobKey)) || !(scheduler.checkExists(triggerKey))){
				return 0;
			}
			scheduler.pauseTrigger(triggerKey);
			scheduler.unscheduleJob(triggerKey);
			scheduler.deleteJob(jobKey);
			return 1;
			
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			logger.error("");
			return -1;
		}
    }
    
    /** 
     * 启动所有定时任务 
     */  
    public int startJobs() {  
        try {  
        	scheduler.start();
        	return 1;
        } catch (Exception e) {  
            logger.error("");
            return -1;
        }  
    }  
  
    /** 
     * 关闭所有定时任务 
     *  
     */  
    public int shutdownJobs() {  
        try {  
            if (!scheduler.isShutdown()) {  
            	scheduler.shutdown();  
            } 
            return 1;
        } catch (Exception e) {  
            logger.error("");
            return -1;
        }  
    } 
	
}
