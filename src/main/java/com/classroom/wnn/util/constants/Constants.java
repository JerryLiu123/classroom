package com.classroom.wnn.util.constants;

public class Constants {
	/**hadoop host**/
	public static final String NamenodeIP="192.168.56.1";
	/**hadoop port**/
	public static final String NamenodePort="9000";
	/**hadoop hdfs file**/
	public static final String HDFSAddress="hdfs://"+NamenodeIP+":"+NamenodePort;
	/**存储用户信息的SESSION名称 **/
	public static final String SESSION_USER_INFO = "userInfo";
	/**用户登录后生成的令牌，用于跳系统之间跳转时进行登录状态验证**/
	public static final String SESSION_USER_TOKEN = "userToken";
	public static final String LOG_SPLIT = " ";
	public static final String LOG_MARKS_QUOTATION = "\"";
	/**
	 * 验证整数的正则式
	 */
	public static final String P_INT = "^\\d+$";
	/**
	 * 验证浮点数的正则式
	 */
	public static final String P_FLOAT = "^\\d+(\\.\\d+){0,1}$";
	/**
	 * 验证电话号码的正则式
	 */
	public static final String P_PHONE = "^\\d+(-\\d+)*$";
	/**
	 * 验证 e-mail 的正则式
	 */
	public static final String P_EMAIL = "^[a-zA-Z_]\\w*@\\w+(\\.\\w+)+$";
	/**
	 * 验证是否为整数
	 */
	public static final int INT = 1;
	/**
	 * 验证是否为浮点数
	 */
	public static final int FLOAT = 2;
	/**
	 * 验证是否为电话号码
	 */
	public static final int PHONE = 3;
	/**
	 * 验证是否为 e-mail
	 */
	public static final int EMAIL = 4;
	/**失败**/
	public static final int STATUS_FAILURE = 1;
	/**成功**/
	public static final int STATUS_SUCCESS= 2;
	/**状态：有效**/
	public static final int VALID_TYPE_EFFECT = 1;
	/**状态：无效**/
	public static final int VALID_TYPE_INVALID = 2;
	
	/**站点虚拟目录，设置该值时不要加斜杠结尾**/
	public static String applicationPath = "";
	/**站点静态文件目录，设置该值时不要加斜杠结尾**/
	public static String staticUrl = "/resources";
	/**站点主页目录**/
	public static String homeUrl = "http://127.0.0.1:8080/classroom";
	public static String businessUrl = "";
}
