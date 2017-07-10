package com.classroom.wnn.util;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 弃用
 * @author xiaoming
 *
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

	@Override
	protected Object determineCurrentLookupKey() {
		// TODO Auto-generated method stub
		return DataSourceContextHolder.getDbType();
	}

}
