package com.classroom.wnn.socket;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class NewWebSockTest extends TextWebSocketHandler {

	private Map<String, Thread> threadMap = new HashMap<String, Thread>();
	
	@Override
	protected void handleTextMessage(WebSocketSession session,
			TextMessage message) throws Exception {
		// TODO Auto-generated method stub
		super.handleTextMessage(session, message);
		TextMessage returnMessage = new TextMessage(message.getPayload()+" received at server"); 
		Thread thread = new ThreadTestSock(session, returnMessage);
		threadMap.put(session.getId(), thread);
		thread.start();
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session,
			CloseStatus status) throws Exception {
		// TODO Auto-generated method stub
		super.afterConnectionClosed(session, status);
		System.out.println("---------");
		threadMap.get(session.getId()).interrupt();
		threadMap.get(session.getId()).join();
	}
}
