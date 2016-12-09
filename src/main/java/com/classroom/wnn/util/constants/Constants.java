package com.classroom.wnn.util.constants;

public class Constants {
	/**hadoop host**/
	public static String namenodeIP="192.168.56.1";
	/**hadoop port**/
	public static String namenodePort="9000";
	/**hadoop hdfs file**/
	public static String hdfsAddress="hdfs://"+namenodeIP+":"+namenodePort;
	/**站点虚拟目录，设置该值时不要加斜杠结尾**/
	public static String applicationPath = "";
	/**站点静态文件目录，设置该值时不要加斜杠结尾**/
	public static String staticUrl = "";
	/**站点主页目录**/
	public static String homeUrl = "http://127.0.0.1:8080/classroom";
	public static String businessUrl = "";
	/**当前环境是否位测试**/
	public static String isTest = "yes"; 
	/**视频上传本地目录**/
	public static String streamFileRepository;
	/**是否立即删除**/
	public static String streamDeleteFinish = "false";
	/**本服务器是否允许其他域上传**/
	public static String streamIsCross = "false";
	/**允许的域**/
	public static String streamCrossOrigin = "*";
	public static String streamCrossServer="";
	/**ffmpeg 路径**/
	public static String ffmpegPath = "";
	
	
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
	
	/**文件上传所用常量**/
	public static final int BUFFER_LENGTH = 1024 * 1024 * 10;
	public static final String FILE_NAME_FIELD = "name";
	public static final String FILE_SIZE_FIELD = "size";
	public static final String TOKEN_FIELD = "token";
	public static final String SERVER_FIELD = "server";
	public static final String SUCCESS = "success";
	public static final String MESSAGE = "message";
	
	/**数据源名称**/
	public static final String DATESOURCE1 = "dataMySQL1";
	public static final String DATESOURCE2 = "dataMySQL2";
	
	public String getNamenodeIP() {
		return namenodeIP;
	}
	public void setNamenodeIP(String namenodeIP) {
		Constants.namenodeIP = namenodeIP;
	}
	public String getNamenodePort() {
		return namenodePort;
	}
	public void setNamenodePort(String namenodePort) {
		Constants.namenodePort = namenodePort;
	}
	public String getHdfsAddress() {
		return hdfsAddress;
	}
	public void setHdfsAddress(String hdfsAddress) {
		Constants.hdfsAddress = hdfsAddress;
	}
	public String getStaticUrl() {
		return staticUrl;
	}
	public void setStaticUrl(String staticUrl) {
		Constants.staticUrl = staticUrl;
	}
	public String getHomeUrl() {
		return homeUrl;
	}
	public void setHomeUrl(String homeUrl) {
		Constants.homeUrl = homeUrl;
	}
	public String getBusinessUrl() {
		return businessUrl;
	}
	public void setBusinessUrl(String businessUrl) {
		Constants.businessUrl = businessUrl;
	}
	public String getApplicationPath() {
		return applicationPath;
	}
	public void setApplicationPath(String applicationPath) {
		Constants.applicationPath = applicationPath;
	}
	public String getIsTest() {
		return isTest;
	}
	public void setIsTest(String isTest) {
		Constants.isTest = isTest;
	}
	public Constants() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getStreamFileRepository() {
		return streamFileRepository;
	}
	public void setStreamFileRepository(String streamFileRepository) {
		Constants.streamFileRepository = streamFileRepository;
	}
	public String getStreamDeleteFinish() {
		return streamDeleteFinish;
	}
	public void setStreamDeleteFinish(String streamDeleteFinish) {
		Constants.streamDeleteFinish = streamDeleteFinish;
	}
	public String getStreamIsCross() {
		return streamIsCross;
	}
	public void setStreamIsCross(String streamIsCross) {
		Constants.streamIsCross = streamIsCross;
	}
	public String getStreamCrossOrigin() {
		return streamCrossOrigin;
	}
	public void setStreamCrossOrigin(String streamCrossOrigin) {
		Constants.streamCrossOrigin = streamCrossOrigin;
	}
	public String getStreamCrossServer() {
		return streamCrossServer;
	}
	public void setStreamCrossServer(String streamCrossServer) {
		Constants.streamCrossServer = streamCrossServer;
	}
	public String getFfmpegPath() {
		return ffmpegPath;
	}
	public void setFfmpegPath(String ffmpegPath) {
		Constants.ffmpegPath = ffmpegPath;
	}
	
	
}
