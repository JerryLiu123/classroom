package com.classroom.wnn.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.classroom.wnn.aop.annotation.Log;
import com.classroom.wnn.bean.ChatMessageBean;
import com.classroom.wnn.bean.LimitQueue;


@Controller
public class UserChatController extends BaseController{
	private static Logger logger = Logger.getLogger(UserChatController.class);
	
	
	private SimpMessagingTemplate template;
	@Autowired
	public UserChatController(SimpMessagingTemplate t) {
		this.template = t;
	}
    //消息缓存列表，先暂时不用redis 的队列了~
    private Map<String, Object> msgCache = new HashMap<String, Object>();
	
	/**
	 * WebSocket聊天的相应接收方法和转发方法
	 * 客户端通过app/userChat调用该方法，并将处理的消息发送客户端订阅的地址
	 * @param chatMessage  关于用户聊天的各个信息
	 */
	@MessageMapping("/userChat")
	public void userChat(ChatMessageBean chatMessage) {
		// 找到需要发送的地址(客户端订阅地址)
		String dest = "/userChat/chat" + chatMessage.getRoomid();
		// 获取缓存，并将用户最新的聊天记录存储到缓存中
		Object cache = msgCache.get(chatMessage.getRoomid());
		try {
			chatMessage.setRoomid(URLDecoder.decode(chatMessage.getRoomid(),"utf-8"));
			chatMessage.setUserName(URLDecoder.decode(chatMessage.getUserName(), "utf-8"));
			chatMessage.setDeptName(URLDecoder.decode(chatMessage.getDeptName(), "utf-8"));
			chatMessage.setChatContent(URLDecoder.decode(chatMessage.getChatContent(), "utf-8"));
			chatMessage.setIsSysMsg(URLDecoder.decode(chatMessage.getIsSysMsg(),"utf-8"));
			chatMessage.setCurTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(); 
		}
		// 发送用户的聊天记录
		this.template.convertAndSend(dest, chatMessage);
        ((LimitQueue<ChatMessageBean>) cache).offer(chatMessage);
	}

	
	@SubscribeMapping("/initChat/{roomid}")
	public LimitQueue<ChatMessageBean> initChatRoom(@DestinationVariable("roomid") String roomid) {
		//logger.info("-------新用户进入聊天室------");
		LimitQueue<ChatMessageBean> chatlist = new LimitQueue<ChatMessageBean>(5);
		// 发送用户的聊天记录
		if (!msgCache.containsKey(roomid)) {
			// 从来没有人进入聊天空间
			msgCache.put(roomid, chatlist);
		} else {
			chatlist = (LimitQueue<ChatMessageBean>) msgCache.get(roomid);
		}
		return chatlist;
	}
	
	@RequestMapping(value="/touserchat/{roomid}")
	public String toUserChat(HttpServletRequest req,HttpServletResponse resp, Map<String, Object> datamap, @PathVariable("roomid") String roomid){
		datamap = getBaseMap(datamap);
		datamap.put("roomid", roomid);
		return "socket";
	}
}
