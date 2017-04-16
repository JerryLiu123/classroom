package com.classroom.wnn.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroom.wnn.aop.annotation.Log;
import com.classroom.wnn.dao.BiVideoInfoMapper;
import com.classroom.wnn.dao.BiZoneInfoMapper;
import com.classroom.wnn.model.BiVideoInfo;
import com.classroom.wnn.model.BiZoneInfo;
import com.classroom.wnn.service.RedisService;
import com.classroom.wnn.service.VideoService;
import com.classroom.wnn.util.Convert;
import com.classroom.wnn.util.DataSourceContextHolder;
import com.classroom.wnn.util.HdfsFileSystem;
import com.classroom.wnn.util.constants.Constants;

@Service(value="videoService")
public class VideoServiceImpl implements VideoService {
	private static Logger logger = Logger.getLogger(VideoServiceImpl.class);
	
	@Autowired
	private BiVideoInfoMapper videoMapper;
	@Autowired
	private BiZoneInfoMapper zoneMapper;
	@Autowired
	private RedisService redisService;
	

	@Override
	public int insertVideo(BiVideoInfo info) {
		// TODO Auto-generated method stub
		return videoMapper.insertReturnKey(info);
	}
	

	@Override
	public int updateVideo(BiVideoInfo info) {
		// TODO Auto-generated method stub
		return videoMapper.updateByPrimaryKeySelective(info);
	}
	
	@Override
	public int insertZoneVider(BiZoneInfo info) {
		// TODO Auto-generated method stub
		return zoneMapper.insertReturnKey(info);
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
	@Transactional
	public void testException() throws Exception {
		// TODO Auto-generated method stub
		
		BiZoneInfo biZoneInfo = new BiZoneInfo();
		biZoneInfo.setvFileid(11111);
		biZoneInfo.setzAvailable(11111);
		biZoneInfo.setzFile("testFile1");
		biZoneInfo.setzHdfsfile("testHdfsFile1");
		biZoneInfo.setzIsdel(11111);
		zoneMapper.insert(biZoneInfo);
		
		//切换数据源
		DataSourceContextHolder.setDbType(Constants.DATESOURCE2);
		
		BiZoneInfo biZoneInfo2 = new BiZoneInfo();
		biZoneInfo2.setvFileid(22222);
		biZoneInfo2.setzAvailable(22222);
		biZoneInfo2.setzFile("testFile2");
		biZoneInfo2.setzHdfsfile("testHdfsFile2");
		biZoneInfo2.setzIsdel(22222);
		zoneMapper.insert(biZoneInfo2);
		throw new Exception("测试异常拦截");
	}
}
