package com.classroom.wnn.dao;

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
}