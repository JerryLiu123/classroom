package com.classroom.wnn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.classroom.wnn.dao.BiVideoInfoMapper;
import com.classroom.wnn.model.BiVideoInfo;
import com.classroom.wnn.service.VideoService;

@Service(value="videoService")
public class VideoServiceImpl implements VideoService {

	@Autowired
	private BiVideoInfoMapper videoMapper;
	
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
		info.setvFile("0");
		return videoMapper.updateByPrimaryKeySelective(info);
	}

	@Override
	public BiVideoInfo getVideoById(Integer id) {
		// TODO Auto-generated method stub
		return videoMapper.selectByPrimaryKey(id);
	}

}
