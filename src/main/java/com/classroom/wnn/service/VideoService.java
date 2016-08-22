package com.classroom.wnn.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.classroom.wnn.model.BiVideoInfo;

public interface VideoService {

	/**
	 * 插入视频信息数据
	 * @param info
	 * @return
	 */
	public int insertVider(BiVideoInfo info);
	
	/**
	 * 更新hdfs地址
	 * @param key
	 * @param path
	 * @return
	 */
	public int updateHDFSFile(String key, String path);
	
	/**
	 * 根据id获得视频信息
	 * @param id
	 * @return
	 */
	public BiVideoInfo getVideoById(Integer id);
	
	/**
	 * 将已上传hdfs并且未从本地删除的文件，从本地删除
	 * */
	public List<BiVideoInfo> delIsHDFSIsLocal();
	
	/**
	 * 上传文件到hdfs
	 * @throws IOException 
	 */
	public void uploadHDFS(File inputFile, String fileName, String infoKey) throws IOException;
}
