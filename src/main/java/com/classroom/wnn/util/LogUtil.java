package com.classroom.wnn.util;

import com.classroom.wnn.util.constants.Constants;

/**
 * 
 * 功能描述： 日志工具类<br>
 */
public class LogUtil {

	/**
	 * 日志信息获取
	 * 
	 * @param functionName
	 *            功能说明
	 * @param status
	 *            状态
	 * @param inputParams
	 *            输入参数
	 * @param outputParams
	 *            输出参数
	 * @param exceptionMsg
	 *            异常信息
	 * @return String
	 * @author:Jiyong.Wei
	 * @date:2013-4-9
	 */
	public static String getLogStr(String functionName, String status,
			Object inputParams, Object outputParams, String exceptionMsg) {
		StringBuffer sb = new StringBuffer();
		sb.append(functionName).append(Constants.LOG_SPLIT);

		sb.append(status).append(Constants.LOG_SPLIT);

		sb.append(Constants.LOG_MARKS_QUOTATION);
		sb.append(inputParams != null ? inputParams.toString() : "");
		sb.append(Constants.LOG_MARKS_QUOTATION);
		sb.append(Constants.LOG_SPLIT);

		sb.append(Constants.LOG_MARKS_QUOTATION);
		sb.append(outputParams != null ? outputParams.toString() : "");
		sb.append(Constants.LOG_MARKS_QUOTATION);
		sb.append(Constants.LOG_SPLIT);

		sb.append(Constants.LOG_MARKS_QUOTATION);
		sb.append(exceptionMsg != null ? exceptionMsg : "");
		sb.append(Constants.LOG_MARKS_QUOTATION);
		return sb.toString();
	}
}
