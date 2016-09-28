package task;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import base.BaseTest;

import com.alibaba.fastjson.JSONObject;
import com.classroom.wnn.schedule.BaseQuartzScheduler;
import com.classroom.wnn.service.RedisService;
import com.classroom.wnn.service.VideoService;
import com.classroom.wnn.task.RedisThreadPool;
import com.classroom.wnn.util.HdfsFileSystem;
import com.classroom.wnn.util.ObjectUtil;
import com.classroom.wnn.util.constants.Constants;
import com.classroom.wnn.util.lock.RedisLockUtil;

public class TaskTest extends BaseTest{

	@Autowired
	private RedisThreadPool redisThreadPool;
	@Autowired
	private BaseQuartzScheduler myWork;
	@Autowired
	private RedisService redisService;
	@Autowired
	private RedisLockUtil redisLockUtil;
	@Autowired
	private VideoService videoService;
	
	private int fuck = 0;
	
	@Test
	public void test(){
//		System.err.println("-----------hdfs--开始------------");
//		try {
//			HdfsFileSystem.listDataNodeInfo();
//			
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		System.out.println(HdfsFileSystem.readFileFromHdfs(Constants.hdfsAddress+"/data/lgh/warehouse/lgh/hive_log.txt"));
//		System.err.println("-----------hdfs--结束------------");
//		System.err.println();
		
		videoService.delIsHDFSIsLocal();
		
//		System.err.println("-----------reids基础应用--开始------------");
//		redisService.set("测试key".getBytes(), "测试value".getBytes());
//		System.out.println(redisService.get("测试key"));
//		redisService.del("测试key");
//		System.out.println(redisService.get("测试key"));
//		System.err.println("-----------reids基础应用--结束------------");
//		System.err.println();
		
//		System.err.println("-----------reids添加任务队列--开始------------");
//		for(int i=0; i<10 ;i++){
//			TaskImportTest test = new TaskImportTest(i+"");
//			test.setTaskId(i);
//			try {
//				redisThreadPool.pushFromTail(ObjectUtil.objectToBytes(test));
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		System.out.println("--------当前队列状态 begin----------");
//		System.out.println(redisThreadPool.getInfo());
//		System.out.println("--------当前队列状态 end----------");
//		System.err.println("-----------reids添加任务队列---结束-----------");
	
		
//		System.err.println("-----测试redis锁------");
//		 Thread t1 = new Thread(new Runnable(){  
//			 public void run(){
//				 cccc1();
//				 System.err.println(fuck);
//	            }});  
//	        t1.start(); 
//		for(int i=1;i<10;i++){
//			 Thread t = new Thread(new Runnable(){  
//				 public void run(){
//					 cccc();
//					 System.err.println(fuck);
//		            }});  
//		        t.start();  
//		}
//		System.err.println("-----测试redis锁------");

//		Boolean f = true;
//		int i = 0;
//		while (f) {
//			
//			if(redisThreadPool.getFinishWorks().get("0") != null){
//				i = i+1;
//			}
//			if(redisThreadPool.getFinishWorks().get("1") != null){
//				i = i+1;
//			}
//			if(redisThreadPool.getFinishWorks().get("2") != null){
//				i = i+1;
//			}
//			if(redisThreadPool.getFinishWorks().get("3") != null){
//				i = i+1;
//			}
//			if(redisThreadPool.getFinishWorks().get("4") != null){
//				i = i+1;
//			}
//			if(i == 5){
//				f = false;
//			}
//			
//		}
//
//		System.err.println("----------所有任务已执行完成----------");
		
//		System.err.println("----------quartz开始----------");
//		System.out.println("开启一个job----"+myWork.srartJob("job_testJob1", "myGroup"));
//		System.err.println("停止一个job----"+myWork.pauseJob("job_testJob1", "myGroup"));;
//		System.err.println("----------quartz结束----------");
		try {
//			solrServer.getServer();
//			
//			SolrQueryBean queryBean = new SolrQueryBean();
//			Map<String, SortWay> aa = new HashMap<String, SortWay>();
//			aa.put("name", SortWay.DESC);
//			Map<String, String> bb = new HashMap<String, String>();
//			bb.put("name", "testName_13*");
//			bb.put("title", "这是测试title13*");
//			queryBean.setQ(bb);
//			queryBean.setSortField(aa);
//			queryBean.setHighlightField("name");
			
			
//			QueryResponse queryResponse = solrServer.getValue(queryBean);
			
//			System.out.println("本次查询时间为-----"+queryResponse.getQTime()+"毫秒");
//			
//			//按照时间获得数据
////			Map<String, Integer> maps = queryResponse.getFacetQuery();
////            for (Entry<String, Integer> entry : maps.entrySet()) { 
////            	System.out.println(entry.getKey() + ":" + entry.getValue());  
////            } 
//			List<FacetField> facets = queryResponse.getFacetFields();// 返回的facet列表  
//			if(facets != null){
//				for (FacetField facet : facets) {  
//				    System.out.println(facet.getName());  
//				    System.out.println("----------------");  
//				    List<Count> counts = facet.getValues();  
//				    for (Count countitem : counts) {  
//				       System.out.println(countitem.getName() + ":"  
//				                          + countitem.getCount());  
//				    }  
//				    System.out.println();
//				}
//			}
//			
//			//获得高亮列表
//			Map<String,Map<String,List<String>>> tempMap = queryResponse.getHighlighting();
//			System.err.println(JSONObject.toJSON(tempMap));
//			
////			for(Map.Entry<String, Map<String,List<String>>> entry : tempMap.entrySet()){
////				System.err.println(entry.getKey());
////				System.err.println("---------------");
////			}
//			
//			//获得返回值
//			SolrDocumentList list = queryResponse.getResults();
//			if(list != null){
//				for(SolrDocument doc : list){
//					System.out.println(doc.getFieldValue("name"));
//				}
//			}else{
//				System.out.println("没有数据");
//			}
			System.err.println("===================");
			Thread.sleep(10 * 60 *1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void cccc(){
		String value=null;
		try {
			value = redisLockUtil.addLock("taskLock", Long.valueOf(3*60*1000));
			System.err.println("线程:"+System.currentTimeMillis()+"已获得锁");
			Thread.sleep(1 *1000);
			fuck = fuck + 1;
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			redisLockUtil.unLock("taskLock", value);
		}
		//fuck = fuck + 1;
	}
	
	public void cccc1(){
		String value=null;
		try {
			value = redisLockUtil.addLock("taskLock", Long.valueOf(3*60*1000));
			System.err.println("线程-------:"+System.currentTimeMillis()+"------已获得锁");
			Thread.sleep(20 *1000);
			fuck = fuck + 1;
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			redisLockUtil.unLock("taskLock", value);
		}
		//fuck = fuck + 1;
	}
}
