package com.classroom.wnn.util.constants;

public class Constants {
	/**hadoop host**/
	public static final String NamenodeIP="125.216.242.200";
	/**hadoop port**/
	public static final String NamenodePort="9000";
	/**hadoop hdfs file**/
	public static final String HDFSAddress="hdfs://"+NamenodeIP+":"+NamenodePort;
	/**存储用户信息的SESSION名称 **/
	public static final String SESSION_USER_INFO = "userInfo";
	/**用户登录后生成的令牌，用于跳系统之间跳转时进行登录状态验证**/
	public static final String SESSION_USER_TOKEN = "userToken";
	
	/**站点虚拟目录，设置该值时不要加斜杠结尾**/
	public static String applicationPath = "";
	public static String staticUrl = "/resources";
	public static String homeUrl = "http://127.0.0.1:8080/classroom";
	public static String businessUrl = "";
}
