package com.campus.campus_system.config;

import com.campus.campus_system.websocket.ChatWebSocketHandler;
import com.campus.campus_system.websocket.WebSocketAuthHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final WebSocketAuthHandshakeInterceptor webSocketAuthHandshakeInterceptor;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler,
                           WebSocketAuthHandshakeInterceptor webSocketAuthHandshakeInterceptor) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.webSocketAuthHandshakeInterceptor = webSocketAuthHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(webSocketAuthHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
