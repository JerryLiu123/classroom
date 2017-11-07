package com.classroom.wnn.task;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
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
	/**
	 * 对象的初始化是先初始化成员变量再执行构造方法
	 * （类构造器是编译器收集所有静态语句块和类变量的赋值语句按语句在源码中的顺序合并生成类构造器，
	 * 对象的构造方法是init，类的构造方法是cinit，可以在堆栈信息中看到）
	 * 以下设置用于 JDK 1.6本项目使用JDK 1.8 所以不能照搬
	 * -XX:PretenureSizeThreshold来控制直接升入老年代的对象大小，大于这个值的对象会直接分配在老年代上。
	 * -XX:+UseAdaptiveSizePolicy开关来控制是否采用动态控制策略，如果动态控制，则动态调整Java堆中各个区域的大小以及进入老年代的年龄。
	 * 当两个存活区切换了几次（HotSpot虚拟机默认15次，-XX:MaxTenuringThreshold控制，大于该值进入老年代）之后，仍然存活的对象（其实只有一小部分，比如，我们自己定义的对象），将被复制到老年代。
	 * -XX:SurvivorRatio参数来配置Eden区域Survivor区的容量比值，默认是8，代表Eden：Survivor1：Survivor2=8:1:1.
	 * -XX:+HandlePromotionFailure（允许担保失败），如果允许，则只会进行MinorGC，此时可以容忍内存分配失败；如果不 允许，则仍然进行Full GC（这代表着如果设置-XX:+Handle PromotionFailure，则触发MinorGC就会同时触发Full GC，哪怕老年代还有很多内存，所以，最好不要这样做
	 * 
	 * 对于无用的类进行回收，必须保证3点：
	 *		1.类的所有实例都已经被回收
	 *		2.加载类的ClassLoader已经被回收
	 *		3.类对象的Class对象没有被引用（即没有通过反射引用该类的地方）
	 *HotSpot提供-Xnoclassgc进行控制
     *	使用-verbose，-XX:+TraceClassLoading、-XX:+TraceClassUnLoading可以查看类加载和卸载信息
     *		-verbose、-XX:+TraceClassLoading可以在Product版HotSpot中使用
     *		-XX:+TraceClassUnLoading需要fastdebug版HotSpot支持
     *所以使用CMS的收集器并不是老年代满了才触发Full GC，而是在使用了一大半（默认68%，即2/3，使用-XX:CMSInitiatingOccupancyFraction来设置）的时候就要进行Full GC，如果用户线程消耗内存不是特别大，
     *可以适当调高-XX:CMSInitiatingOccupancyFraction以降低GC次数，提高性能，
     *如果预留的用户线程内存不够，则会触发Concurrent Mode Failure，此时，将触发备用方案：使用Serial Old 收集器进行收集，但这样停顿时间就长了，因此-XX:CMSInitiatingOccupancyFraction不宜设的过大。
	 *还有，CMS采用的是标记清除算法，会导致内存碎片的产生，可以使用-XX：+UseCMSCompactAtFullCollection来设置是否在Full GC之后进行碎片整理，
	 *用-XX：CMSFullGCsBeforeCompaction来设置在执行多少次不压缩的Full GC之后，来一次带压缩的Full GC。
	 *Serial收集器：新生代收集器，使用停止复制算法:
	 *	使用-XX:+UseSerialGC可以使用Serial+Serial Old模式运行进行内存回收（这也是虚拟机在Client模式下运行的默认值）
	 *ParNew收集器：新生代收集器，使用停止复制算法，Serial收集器的多线程版:
	 *	使用-XX:+UseParNewGC开关来控制使用ParNew+Serial Old收集器组合收集内存；使用-XX:ParallelGCThreads来设置执行内存回收的线程数。
	 *Parallel Scavenge 收集器：新生代收集器，使用停止复制算法，关注CPU吞吐量:
	 *	使用-XX:+UseParallelGC开关控制使用 Parallel Scavenge+Serial Old收集器组合回收垃圾（这也是在Server模式下的默认值）；使用-XX:GCTimeRatio来设置用户执行时间占总时间的比例，默认99，即 1%的时间用来进行垃圾回收。
	 *	使用-XX:MaxGCPauseMillis设置GC的最大停顿时间（这个参数只对Parallel Scavenge有效）
	 *Parallel Old收集器：老年代收集器，多线程 :
	 *	使用-XX:+UseParallelOldGC开关控制使用Parallel Scavenge +Parallel Old组合收集器进行收集。
	 *CMS（Concurrent Mark Sweep）老年代收集器 致力于获取最短回收停顿时间 ：
	 *	使用-XX:+UseConcMarkSweepGC进行ParNew+CMS+Serial Old进行内存回收，优先使用ParNew+CMS（原因见后面），当用户线程内存不足时，采用备用方案Serial Old收集。
	 */
	private static final String TASK_LOCK = "taskLock";//此字段标志位redis分布式锁
    private RedisTemplate redisTemplate;  
    private RedisLockUtil redisLockUtil;
    private String key = "classroom";  
    //private int cap = Short.MAX_VALUE;//最大阻塞的容量，超过容量将会导致清空旧数据  
    private byte[] rawKey;  
    private RedisConnectionFactory factory;  
    private RedisConnection connection;//for blocking  
    private BoundListOperations<String, Task> listOperations;//noblocking
    //感觉hash主要用于对于对象的修改时比较有用，但是在本实例中对象并不需要修改，所以就没有使用hash
    //private BoundHashOperations<String, byte[], Task> hashOperations;
      
    private boolean isClosed;  
    /* 默认池中线程数 */
    private int worker_num = 5;
    /* 池中的所有线程 */
    public PoolWorker[] workers;
      
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
        workers = new PoolWorker[worker_num];
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
    public void pushFromHead(Task value) throws Exception{ 
        listOperations.leftPush(value);  
        //listOperations.notifyAll();
    }  
    /**
     * 从尾部插入  
     * @param value
     * @throws Exception 
     */
    public void pushFromTail(Task value) throws Exception{ 
        listOperations.rightPush(value);  
        //listOperations.notifyAll();
    }  
      
    /** 
     * 从头取出任务
     * @return null if no item in queue 
     */  
    public Task takeFromHead() throws Exception{  
        return listOperations.leftPop();  
    }  
    
    /**
     * 从尾部取出任务
     * @return
     */
    public Task takeFromTail() throws Exception{  
        return listOperations.rightPop();  
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
                        	listOperations.wait(200);
                        } catch (InterruptedException ie) {
                            logger.error(ie);
                        }
                    }
                    /* 取出任务执行 */
                    try {
                    	//反序列话对象
//                    	ByteArrayInputStream in = new ByteArrayInputStream(takeFromHead());
//                    	if(in != null) {
//                         	ObjectInputStream sIn = new ObjectInputStream(in);
//                        	Object ob = sIn.readObject();
//    						if(Task.class.isAssignableFrom(ob.getClass())) {
//    							r = (Task) ob;
//    						}else {
//    							logger.error(ob.getClass().getName()+"--不是此线程池可以执行的方法");
//    							r = null;
//    						}
//                    	}
                    	r = takeFromHead();
					}catch (Exception e) {
						// TODO Auto-generated catch block
						logger.error("获得任务失败", e);
						r = null;
					}
                }
                if (r != null) {
                    isWaiting = false;
                    try {
                        /* 该任务是否需要立即执行 */
                        if (r.needExecuteImmediate()) {
                            new Thread(r).start();
                        } else {
                        	r.setBeginExceuteTime(new Date());
                            r.run();
                            r.setFinishTime(new Date());
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
