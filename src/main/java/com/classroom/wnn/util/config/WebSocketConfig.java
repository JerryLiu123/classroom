package com.classroom.wnn.util.config;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebMvc
@EnableWebSocketMessageBroker
/**
 * 这个逼东西必须放在web.xml DispatcherServlet 配置项中 contextConfigLocation 属性的配置文件中 坑死爹了！！！
 * */
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private static Logger logger = Logger.getLogger(WebSocketConfig.class);

	@Override
	public void configureClientInboundChannel(ChannelRegistration arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration arg0) {
		// TODO Auto-generated method stub
		System.out.println(arg0.toString());
		
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry arg0) {
		// TODO Auto-generated method stub
        //这里设置的simple broker是指可以订阅的地址，也就是服务器可以发送的地址
        arg0.enableSimpleBroker("/userChat","/initChat");  
        arg0.setApplicationDestinationPrefixes("/app");
        logger.info("webSocket 服务启动成功");
	}
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry arg0) {
		// TODO Auto-generated method stub
		//添加这个Endpoint，这样在网页中就可以通过websocket连接上服务了
		arg0.addEndpoint("/webchat").withSockJS();
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean configureMessageConverters(List<MessageConverter> arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration arg0) {
		// TODO Auto-generated method stub
		
	}

}
