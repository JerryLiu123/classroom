package com.classroom.wnn.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroom.wnn.dao.BiVideoInfoMapper;
import com.classroom.wnn.model.BiVideoInfo;
import com.classroom.wnn.service.RedisService;
import com.classroom.wnn.service.VideoService;
import com.classroom.wnn.util.Convert;
import com.classroom.wnn.util.HdfsFileSystem;
import com.classroom.wnn.util.constants.Constants;

@Service(value="videoService")
public class VideoServiceImpl implements VideoService {
	private static Logger logger = Logger.getLogger(VideoServiceImpl.class);
	
	@Autowired
	private BiVideoInfoMapper videoMapper;
	@Autowired
	private RedisService redisService;
	
	@Override
	public int insertVider(BiVideoInfo info) {
		// TODO Auto-generated method stub
		return videoMapper.insertReturnKey(info);
	}

	@Override
	public int updateHDFSFile(String key, String path) {
		// TODO Auto-generated method stub
		BiVideoInfo info = new BiVideoInfo();
		info.setId(Integer.parseInt(key));
		info.setvHdfsfile(path);
		return videoMapper.updateByPrimaryKeySelective(info);
	}

	@Override
	public BiVideoInfo getVideoById(Integer id) {
		// TODO Auto-generated method stub
		return videoMapper.selectByPrimaryKey(id);
	}

	@Override
	@Transactional
	public List<BiVideoInfo> delIsHDFSIsLocal() {
		// TODO Auto-generated method stub
		List<BiVideoInfo> infos = videoMapper.selectUpdateHDFS();
		List<BiVideoInfo> upInfos = new ArrayList<BiVideoInfo>();
		List<String> fileNames = new ArrayList<String>();
		for(BiVideoInfo info : infos){
			if(!redisService.exists("FileUser"+info.getvFile())){//如果不存在此锁，代表文件没有被占用，可以删除
				File f = new File(info.getvFile());
				if(!f.getParentFile().exists()){//如果目录不存在则直接更新数据库信息,将本地文件目录设为0
					info.setvFile("0");
				}else if(!f.exists()){//如果目录不为空，但是找不到文件的话，也直接更新数据库信息
					info.setvFile("0");
				}else{//代表找到文件
					fileNames.add(info.getvFile());
					info.setvFile("0");
				}
				upInfos.add(info);
			}
		}
		//先更新数据库信息，再删除文件
		videoMapper.updateBatch(upInfos);
		for(String fileName : fileNames){
			File f = new File(fileName);
			f.delete();
		}
		return null;
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
		BiVideoInfo info = new BiVideoInfo();
		info.setId(Integer.parseInt(infoKey));
		info.setvHdfsfile(path);
		info.setvFile("0");
		videoMapper.updateByPrimaryKeySelective(info);
		logger.info("更新视频信息表成功");
		//删除本地文件
		if(!redisService.exists("FileUser"+inputFile.toString())){//如果不存在此锁，代表文件没有被占用，可以删除
			inputFile.delete();
		}
	}

}
