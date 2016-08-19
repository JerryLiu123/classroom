package com.classroom.wnn.service;

import com.classroom.wnn.model.BiVideoInfo;

public interface VideoService {

	/**
	 * 插入视频信息数据
	 * @param info
	 * @return
	 */
	public int insertVider(BiVideoInfo info);
	
	/**
	 * 更新hdfs地址，并将本地地址设置为0
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
}
