package com.classroom.wnn.dao;

import com.classroom.wnn.model.BiVideoInfo;

public interface BiVideoInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(BiVideoInfo record);

    int insertSelective(BiVideoInfo record);

    BiVideoInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(BiVideoInfo record);

    int updateByPrimaryKey(BiVideoInfo record);
    
    /*====================================*/
    /**
     * 插入信息，并返回主键
     * */
    int insertReturnKey(BiVideoInfo record);
}