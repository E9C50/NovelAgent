package com.fuxin.springai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 设置消息代理的前缀
        config.enableSimpleBroker("/topic"); // 客户端订阅地址前缀
        config.setApplicationDestinationPrefixes("/app"); // 客户端发送消息地址前缀
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP协议的端点，客户端连接时使用
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 允许跨域
                .withSockJS(); // 支持SockJS
    }
}