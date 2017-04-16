package com.classroom.wnn.socket;

import net.sf.json.JSONArray;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class ThreadTestSock extends Thread {

	private WebSocketSession session;
	private TextMessage message;
	
	public ThreadTestSock(WebSocketSession session, TextMessage message) {
		super();
		this.session = session;
		this.message = message;
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		TextMessage returnMessage = new TextMessage(message.getPayload()+" received at server"); 
		while(!isInterrupted()){
			try {
				
			//System.out.println(session.isOpen());
			//System.out.println(JSONArray.fromObject(session.getAttributes()));
			//System.out.println(JSONObject.fromObject(session));
			
			session.sendMessage(returnMessage);
			
			Thread.sleep(300);
			} catch (Exception e) {
				// TODO: handle exception
				System.err.println("------------------------"+e.getMessage());
				break;
			}
		}
		System.out.println("线程已经退出!!!");
	
	}
}
