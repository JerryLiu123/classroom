package com.classroom.wnn.task;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;

import com.classroom.wnn.util.lock.RedisLockUtil;

/**
 * 初始化redis任务队列
 * @author lgh
 *
 */
public class RedisThreadPool implements InitializingBean{
	private static Logger logger = Logger.getLogger(RedisThreadPool.class);
	
	private static final String TASK_LOCK = "taskLock";//此字段标志位redis分布式锁
    private RedisTemplate redisTemplate;  
    private RedisLockUtil redisLockUtil;
    private String key = "classroom";  
    //private int cap = Short.MAX_VALUE;//最大阻塞的容量，超过容量将会导致清空旧数据  
    private byte[] rawKey;  
    private RedisConnectionFactory factory;  
    private RedisConnection connection;//for blocking  
    private BoundListOperations<String, byte[]> listOperations;//noblocking
    //感觉hash主要用于对于对象的修改时比较有用，但是在本实例中对象并不需要修改，所以就没有使用hash
    //private BoundHashOperations<String, byte[], Task> hashOperations;
      
    private boolean isClosed;  
    /* 默认池中线程数 */
    private int worker_num = 5;
    /* 池中的所有线程 */
    public PoolWorker[] workers;
    /*处理完的线程ID*/
    private Map<String, Boolean> finishWorks;
      
    /**
     * 初始化方法
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		logger.info("初始化"+worker_num+"个线程池");
        factory = redisTemplate.getConnectionFactory();  
        connection = RedisConnectionUtils.getConnection(factory);  
        rawKey = redisTemplate.getKeySerializer().serialize(key);  
        listOperations = redisTemplate.boundListOps(key); 
        //hashOperations = redisTemplate.boundHashOps(key);
        redisTemplate.setValueSerializer(null);//如果不将序列化方法设为null，后面它会自己再序列化一次
        workers = new PoolWorker[worker_num];
        finishWorks = new HashMap<String, Boolean>();
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new PoolWorker(i);
        }
	}
    /**
    * 线程池信息
    * @return
    */
    public String getInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nTask listOperations Size:" + listOperations.size());
        for (int i = 0; i < workers.length; i++) {
            sb.append("\nWorker " + i + " is "
                    + ((workers[i].isWaiting()) ? "Waiting." : "Running."));
        }
        return sb.toString();
    }

    /** 
     * 从队列的头，插入 
     * @throws Exception 
     */  
    public void pushFromHead(byte[] value) throws Exception{ 
        listOperations.leftPush(value);  
        //listOperations.notifyAll();
    }  
    /**
     * 从尾部插入  
     * @param value
     */
    public void pushFromTail(byte[] value){ 
        listOperations.rightPush(value);  
        //listOperations.notifyAll();
    }  
      
    /** 
     * noblocking 
     * @return null if no item in queue 
     */  
    public byte[] removeFromHead(){  
        return listOperations.leftPop();  
    }  
      
    public byte[] removeFromTail(){  
        return listOperations.rightPop();  
    }  
      
    /** 
     * blocking 
     * 从头取出并删除第一个元素 
     * @return 
     * @throws TimeoutException 
     */  
    public synchronized byte[] takeFromHead(int timeout) throws InterruptedException, TimeoutException{  
    	String value = redisLockUtil.addLock(TASK_LOCK, Long.valueOf(3*60*1000));
    	//Thread.sleep(2*60*1000);
        //lock.lockInterruptibly();  
        try{  
            List<byte[]> results = connection.bLPop(timeout, rawKey);  
            if(CollectionUtils.isEmpty(results)){  
                return null;  
            }
            return results.get(1);
            //return (byte[])redisTemplate.getValueSerializer().deserialize(results.get(1));  
        }finally{
        	redisLockUtil.unLock(TASK_LOCK, value);
            //lock.unlock();  
        }  
    }  
    
    /**
     * 取出任务
     * @return
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    public byte[] takeFromHead() throws InterruptedException, TimeoutException{  
        return takeFromHead(0);  
    }  
    
    /** 
     * blocking 
     * remove and get last item from queue:BRPOP 
     * 从尾部取出并删除第一个元素
     * @return 
     * @throws TimeoutException 
     */  
    public synchronized byte[] takeFromTail(int timeout) throws InterruptedException, TimeoutException{   
    	String value = redisLockUtil.addLock(TASK_LOCK, Long.valueOf(2*60*1000));
        //lock.lockInterruptibly();
        try{  
            List<byte[]> results = connection.bRPop(timeout, rawKey);  
            if(CollectionUtils.isEmpty(results)){  
                return null;  
            }  
            return results.get(1);
            //return (byte[])redisTemplate.getValueSerializer().deserialize(results.get(1));  
        }finally{  
        	redisLockUtil.unLock(TASK_LOCK, value);
            //lock.unlock();  
        }  
    }  
      
    public byte[] takeFromTail() throws InterruptedException, TimeoutException{  
        return takeFromHead(0);  
    } 
      
    /**
    * 销毁线程池
    */
    public synchronized void destroy() {
    	if(isClosed){
            for (int i = 0; i < worker_num; i++) {
                workers[i].stopWorker();
                workers[i] = null;
            }
            RedisConnectionUtils.releaseConnection(connection, factory); 
    	}
    }
    //获得UUID
    private String getUUID(){
    	UUID uuid = UUID.randomUUID();
    	return uuid.toString();
    }
    /**
    * 池中工作线程
    * 
    * @author lgh
    */
    private class PoolWorker extends Thread {
        private int index = -1;
        /* 该工作线程是否有效 */
        private boolean isRunning = true;
        /* 该工作线程是否可以执行新任务 */
        private boolean isWaiting = true;

        public PoolWorker(int index) {
            this.index = index;
            start();
        }

        public void stopWorker() {
            this.isRunning = false;
        }

        public boolean isWaiting() {
            return this.isWaiting;
        }
        /**
        * 循环执行任务
        */
        public void run() {
            while (isRunning) {
                Task r = null;
                synchronized (listOperations) {
                    while (listOperations.size()<=0) {
                        try {
                            /* 任务队列为空，则等待有新任务加入从而被唤醒 */
                        	listOperations.wait(20);
                        } catch (InterruptedException ie) {
                            logger.error(ie);
                        }
                    }
                    /* 取出任务执行 */
                    try {
                    	//反序列话对象
                    	ByteArrayInputStream in = new ByteArrayInputStream(takeFromHead());
                    	ObjectInputStream sIn = new ObjectInputStream(in);
						r = (Task) sIn.readObject();
					} catch (TimeoutException e) {
						// TODO Auto-generated catch block
						r = null;
						logger.error("获得锁超时----"+e);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						r = null;
						logger.error(e);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						r = null;
						logger.error(e);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						r = null;
						logger.error(e);
					}
                }
                if (r != null) {
                    isWaiting = false;
                    try {
                        /* 该任务是否需要立即执行 */
                        if (r.needExecuteImmediate()) {
                            new Thread(r).start();
                        } else {
                            r.run();
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
                    isWaiting = true;
                    r = null;
                }
            }
        }
    }
	public synchronized Map<String, Boolean> getFinishWorks() {
		return finishWorks;
	}

	public void setFinishWorks(Map<String, Boolean> finishWorks) {
		this.finishWorks = finishWorks;
	}
    public void setRedisTemplate(RedisTemplate redisTemplate) {  
        this.redisTemplate = redisTemplate;  
    } 
  
    public void setKey(String key) {  
        this.key = key;  
    }
	public int getWorker_num() {
		return worker_num;
	}
	public void setWorker_num(int worker_num) {
		this.worker_num = worker_num;
	}
	public RedisLockUtil getRedisLockUtil() {
		return redisLockUtil;
	}
	public void setRedisLockUtil(RedisLockUtil redisLockUtil) {
		this.redisLockUtil = redisLockUtil;
	}
	
}
