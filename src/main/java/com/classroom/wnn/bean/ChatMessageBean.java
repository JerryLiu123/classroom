package com.classroom.wnn.bean;

public class ChatMessageBean {
	//房间号
	private String roomid;
	//用户名
	private String userName;
	//机构名
	private String deptName;
	//当前系统时间
	private String curTime;
	//聊天内容
	private String chatContent;
	//是否是系统消息
	private String isSysMsg;
	public String getRoomid() {
		return roomid;
	}
	public void setRoomid(String roomid) {
		this.roomid = roomid;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	public String getCurTime() {
		return curTime;
	}
	public void setCurTime(String curTime) {
		this.curTime = curTime;
	}
	public String getChatContent() {
		return chatContent;
	}
	public void setChatContent(String chatContent) {
		this.chatContent = chatContent;
	}
	public String getIsSysMsg() {
		return isSysMsg;
	}
	public void setIsSysMsg(String isSysMsg) {
		this.isSysMsg = isSysMsg;
	}
	
	
}
