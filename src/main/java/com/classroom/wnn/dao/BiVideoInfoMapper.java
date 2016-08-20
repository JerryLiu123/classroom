package com.classroom.wnn.dao;

import java.util.List;

import com.classroom.wnn.model.BiVideoInfo;

public interface BiVideoInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(BiVideoInfo record);

    int insertSelective(BiVideoInfo record);

    BiVideoInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(BiVideoInfo record);

    int updateByPrimaryKey(BiVideoInfo record);
    
    /*=======================================*/
    int insertReturnKey(BiVideoInfo record);
    
    /**
     * 获得已经上传hdfs但是还未删除本地文件的文件列表
     * */
    List<BiVideoInfo> selectUpdateHDFS();
}