package com.classroom.wnn.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

import com.classroom.wnn.aop.annotation.Log;
import com.classroom.wnn.dao.BiVideoInfoMapper;
import com.classroom.wnn.dao.BiZoneInfoMapper;
import com.classroom.wnn.daoslave.BiZoneInfoMapperSlave;
import com.classroom.wnn.model.BiVideoInfo;
import com.classroom.wnn.model.BiZoneInfo;
import com.classroom.wnn.service.RedisService;
import com.classroom.wnn.service.VideoService;
import com.classroom.wnn.util.Convert;
import com.classroom.wnn.util.DataSourceContextHolder;
import com.classroom.wnn.util.HdfsFileSystem;
import com.classroom.wnn.util.constants.Constants;
import com.classroom.wnn.util.dynamicdatasource.CustomerContextHolder;

@Service(value="videoService")
public class VideoServiceImpl implements VideoService {
	private static Logger logger = Logger.getLogger(VideoServiceImpl.class);
	
	@Autowired
	private BiVideoInfoMapper videoMapper;
	@Autowired
	private BiZoneInfoMapper zoneMapper;
	@Autowired
	private RedisService redisService;
//	@Autowired
//	private BiZoneInfoMapperSlave zonemapperSlave;
	

	public int insertVideo(BiVideoInfo info) {
		// TODO Auto-generated method stub
		return videoMapper.insertReturnKey(info);
	}
	

	public int updateVideo(BiVideoInfo info) {
		// TODO Auto-generated method stub
		return videoMapper.updateByPrimaryKeySelective(info);
	}
	
	public int insertZoneVider(BiZoneInfo info) {
		// TODO Auto-generated method stub
		return zoneMapper.insertReturnKey(info);
	}

	public int updateHDFSFile(String key, String path) {
		// TODO Auto-generated method stub
		BiZoneInfo info = new BiZoneInfo();
		info.setId(Integer.parseInt(key));
		info.setzHdfsfile(path);
		return zoneMapper.updateByPrimaryKeySelective(info);
	}

//	@Override
//	public BiVideoInfo getVideoById(Integer id) {
//		// TODO Auto-generated method stub
//		return videoMapper.selectByPrimaryKey(id);
//	}

	@Transactional
	public List<BiZoneInfo> delIsHDFSIsLocal() {
		// TODO Auto-generated method stub
		List<BiZoneInfo> infos = zoneMapper.selectUpdateHDFS();
		List<BiZoneInfo> upInfos = new ArrayList<BiZoneInfo>();
		List<String> fileNames = new ArrayList<String>();
		if(infos != null && infos.size() > 0){
			for(BiZoneInfo info : infos){
				if(!redisService.exists("FileUser"+info.getzFile())){//如果不存在此锁，代表文件没有被占用，可以删除
					/*
					 * 到了这一步说明此文件有hdfs地址，所以文件不可能再被使用本地地址播放
					 * */
					File f = new File(info.getzFile());
					if(!f.getParentFile().exists()){//如果目录不存在则直接更新数据库信息,将本地文件目录设为0
						info.setzFile("0");
					}else if(!f.exists()){//如果目录不为空，但是找不到文件的话，也直接更新数据库信息
						info.setzFile("0");
					}else{//代表找到文件
						fileNames.add(info.getzFile());
						info.setzFile("0");
					}
					info.setzHdfsfile(null);
					upInfos.add(info);
				}
			}
			//先更新数据库信息，再删除文件
			zoneMapper.updateBatch(upInfos);
			for(String fileName : fileNames){
				File f = new File(fileName);
				f.delete();
			}
		}else{
			logger.info("----没有找到可清理的任务----");
		}
		return upInfos;
	}

	@Transactional
	public void uploadHDFS(File inputFile, String fileName, String infoKey) throws IOException {
		// TODO Auto-generated method stub
		logger.info("写入hdfs开始------");
		String[] names = fileName.split("\\.");
		StringBuffer c = new StringBuffer();
		for(int i=0;i<names.length-1;i++){
			c.append(names[i]);
		}
		fileName = Convert.toBase64String(c.toString()+"_"+System.currentTimeMillis())+"."+names[names.length-1];//将文件名加入当前时间时间戳，并进行base64编码
		String path = Constants.hdfsAddress+"/course/"+fileName;
		logger.info("写入hdfs结束，地址为------"+path);
		HdfsFileSystem.createFile(inputFile, path);
		/*将新的地址目录写入 视频信息表 */
		logger.info("更新视频信息表开始");
		BiZoneInfo info = new BiZoneInfo();
		info.setId(Integer.parseInt(infoKey));
		info.setzHdfsfile(path);
		info.setzFile("0");
		zoneMapper.updateByPrimaryKeySelective(info);
		logger.info("更新视频信息表成功");
		//删除本地文件
		if(!redisService.exists("FileUser"+inputFile.toString())){//如果不存在此锁，代表文件没有被占用，可以删除
			inputFile.delete();
		}
	}

	
	@Transactional(rollbackFor=java.lang.Exception.class, 
			isolation=Isolation.READ_UNCOMMITTED, 
			propagation=Propagation.REQUIRES_NEW)
	public void testException() throws Exception {
		// TODO Auto-generated method stub
		
//		CustomerContextHolder.setContextType(Constants.DATESOURCE2);
//		System.out.println("---线程1---"+DataSourceContextHolder.getDbType());
//		Thread.sleep(5000);
//		BiZoneInfo biZoneInfo = new BiZoneInfo();
//		biZoneInfo.setvFileid(11111);
//		biZoneInfo.setzAvailable(11111);
//		biZoneInfo.setzFile("testFile1");
//		biZoneInfo.setzHdfsfile("testHdfsFile1");
//		biZoneInfo.setzIsdel(11111);
//		System.out.println("---线程1插入数据库操作---A");
//		zoneMapper.insert(biZoneInfo);
//		
//		BiZoneInfo biZoneInfo2 = new BiZoneInfo();
//		biZoneInfo2.setvFileid(22222);
//		biZoneInfo2.setzAvailable(22222);
//		biZoneInfo2.setzFile("testFile2");
//		biZoneInfo2.setzHdfsfile("testHdfsFile2");
//		biZoneInfo2.setzIsdel(22222);
//		//切换数据源
//		CustomerContextHolder.setContextType(Constants.DATESOURCE2);
//		System.out.println("---线程1插入数据库操作---B");
//		zoneMapper.insert(biZoneInfo2);
		//throw new Exception("测试异常拦截");
		/*
		 * 将第一步(线程2未提交事务)是查询不出数据，则第二步(线程2提交事务)后的查询操作也查询不出数据
		 * 将第一步(线程2未提交事务)时的查询操作删除，则第二步(线程2提交事务)后的查询操作可以查询出数据
		 * 也就是说我设置的脏读没有效果？？？？
		 * 经测试后的初步结论为，JTA事务无法使用注解的方式切换事务的隔离级别~但是使用本地事务的时候可以
		 * 
		 * 在 AbstractPlatformTransactionManager.getTransaction(TransactionDefinition definition)中看到isolationLevel已经被修改了，值为1但是没有效果啊！！！
		 * TransactionSynchronizationManager中currentTransactionIsolationLevel也赋值了~~~操！！！！
		 * */
		
		
		//休眠等待线程2插入结束
		Thread.sleep(2000);
		System.err.println("---------线程1未提交事务查询开始---------");
		//执行切换数据源前的查询
		List<BiZoneInfo> listDB1 = zoneMapper.selectUpdateHDFS();
		
		CustomerContextHolder.setContextType(Constants.DATESOURCE2);
		//执行切换数据源后的查询
		List<BiZoneInfo> listDB2 = zoneMapper.selectUpdateHDFS();
		
		//分别输出两个查询的数据
		System.out.println("------------db1中数据输出开始------------");
		for(BiZoneInfo db1 : listDB1) {
			System.out.println(db1.getvFileid()+"---"+db1.getzAvailable()+"---"+db1.getzFile()+"---"+db1.getzHdfsfile()+"---"+db1.getzIsdel());
		}
		System.out.println("------------db1中数据输出结束------------");
		
		System.out.println("------------db2中数据输出开始------------");
		for(BiZoneInfo db2 : listDB2) {
			System.out.println(db2.getvFileid()+"---"+db2.getzAvailable()+"---"+db2.getzFile()+"---"+db2.getzHdfsfile()+"---"+db2.getzIsdel());
		}
		System.out.println("------------db2中数据输出结束------------");
		System.err.println("---------线程1未提交事务查询结束---------");
		
		//休眠等待线程2执行结束
		Thread.sleep(6000);
		//再次查询
		System.err.println("》》》》》》》》》》》》》》》线程1提交事务查询开始《《《《《《《《《《《《《");
		CustomerContextHolder.setContextType(Constants.DATESOURCE1);
		//执行切换数据源前的查询
		List<BiZoneInfo> listDB3 = zoneMapper.selectUpdateHDFS();
		
		CustomerContextHolder.setContextType(Constants.DATESOURCE2);
		//执行切换数据源后的查询
		List<BiZoneInfo> listDB4 = zoneMapper.selectUpdateHDFS();
		
		//分别输出两个查询的数据
		System.out.println("》》》》》》》》》》》》》》》db1中数据输出开始《《《《《《《《《《《");
		for(BiZoneInfo db1 : listDB3) {
			System.out.println(db1.getvFileid()+"---"+db1.getzAvailable()+"---"+db1.getzFile()+"---"+db1.getzHdfsfile()+"---"+db1.getzIsdel());
		}
		System.out.println("》》》》》》》》》》》》》》》db1中数据输出结束《《《《《《《《《《《");
		
		System.out.println("》》》》》》》》》》》》》》》db2中数据输出开始《《《《《《《《《《《");
		for(BiZoneInfo db2 : listDB4) {
			System.out.println(db2.getvFileid()+"---"+db2.getzAvailable()+"---"+db2.getzFile()+"---"+db2.getzHdfsfile()+"---"+db2.getzIsdel());
		}
		System.out.println("》》》》》》》》》》》》》》》db2中数据输出结束《《《《《《《《《《《");
		System.err.println("》》》》》》》》》》》》》》》线程1提交事务查询结束《《《《《《《《《《《");
		
	}
	
	@Transactional(rollbackFor=java.lang.Exception.class, isolation=Isolation.READ_COMMITTED)
	public void testException2() throws Exception {
		// TODO Auto-generated method stub
		
		//每一个线程都会生成一个新的sqlSession
		//单个线程中上下两个sql操作使用了同一个sqlsession?   但是中间更换了SqlSessionTemplate之后会不会导致原来的隔离级别失效了？
		
		/*测试内容
			1.设置隔离级别为脏读(事务为总体事务，所有sql操作后提交)
			2.进行插入操作
			3.进行查询操作(脏读的话这条应该可以查出来)
			4.进行切换数据库SqlSessionTemplate操作(切换数据库操作)
			5.进行查询操作(如果用的不是一个sqlSession这个应该为默认的隔离级别
						1.查不出数据说明隔离级别变了
						2.查出数据说明隔离级别没变<希望这样吧！！！>)
			6.退出操作
		  结果
		         
		*/
		
		BiZoneInfo biZoneInfo = new BiZoneInfo();
		biZoneInfo.setvFileid(33333);
		biZoneInfo.setzAvailable(33333);
		biZoneInfo.setzFile("testFile13");
		biZoneInfo.setzHdfsfile("testHdfsFile13");
		biZoneInfo.setzIsdel(33333);
		System.out.println("---线程2插入数据库操作---A");
		zoneMapper.insert(biZoneInfo);
		//执行切换数据源前的查询
//		List<BiZoneInfo> listDB3 = zoneMapper.selectUpdateHDFS();
		
		BiZoneInfo biZoneInfo2 = new BiZoneInfo();
		biZoneInfo2.setvFileid(44444);
		biZoneInfo2.setzAvailable(44444);
		biZoneInfo2.setzFile("testFile4");
		biZoneInfo2.setzHdfsfile("testHdfsFile4");
		biZoneInfo2.setzIsdel(44444);
		//切换数据源
		CustomerContextHolder.setContextType(Constants.DATESOURCE2);
		System.out.println("---线程2插入数据库操作---B");
		zoneMapper.insert(biZoneInfo2);
		
		System.err.println("-------------线程2！！！插入成功！！！未提交事务!!!!-------------");
		Thread.sleep(3000);
//		CustomerContextHolder.setContextType(Constants.DATESOURCE2);
//		//执行切换数据源后的查询
//		List<BiZoneInfo> listDB4 = zoneMapper.selectUpdateHDFS();
		
		//分别输出两个查询的数据
//		System.out.println("》》》》》》》》》》》》》》》db1中数据输出开始《《《《《《《《《《《");
//		for(BiZoneInfo db1 : listDB3) {
//			System.out.println(db1.getvFileid()+"---"+db1.getzAvailable()+"---"+db1.getzFile()+"---"+db1.getzHdfsfile()+"---"+db1.getzIsdel());
//		}
//		System.out.println("》》》》》》》》》》》》》》》db1中数据输出结束《《《《《《《《《《《");
//		
//		System.out.println("》》》》》》》》》》》》》》》db2中数据输出开始《《《《《《《《《《《");
//		for(BiZoneInfo db2 : listDB4) {
//			System.out.println(db2.getvFileid()+"---"+db2.getzAvailable()+"---"+db2.getzFile()+"---"+db2.getzHdfsfile()+"---"+db2.getzIsdel());
//		}
//		System.out.println("》》》》》》》》》》》》》》》db2中数据输出结束《《《《《《《《《《《");
//		
		System.err.println("-------------线程2！！！执行结束!!!!-------------");
//		//DataSourceContextHolder.clearDbType();
//		
//		throw new Exception("测试异常拦截");
	}
}
