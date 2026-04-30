package com.campus.campus_system.websocket;

import com.campus.campus_system.entity.Message;
import com.campus.campus_system.service.FriendService;
import com.campus.campus_system.service.MessageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final MessageService messageService;
    private final OnlineUserSessionRegistry onlineUserSessionRegistry;
    private final FriendService friendService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public ChatWebSocketHandler(MessageService messageService,
                                OnlineUserSessionRegistry onlineUserSessionRegistry,
                                FriendService friendService) {
        this.messageService = messageService;
        this.onlineUserSessionRegistry = onlineUserSessionRegistry;
        this.friendService = friendService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long currentUserId = getCurrentUserId(session);
        onlineUserSessionRegistry.addSession(currentUserId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Long senderId = getCurrentUserId(session);
        Map<String, Object> payload = objectMapper.readValue(textMessage.getPayload(), new TypeReference<>() {
        });

        Long receiverId = Long.valueOf(String.valueOf(payload.get("receiverId")));
        String content = String.valueOf(payload.get("content")).trim();
        String messageType = payload.getOrDefault("messageType", "text").toString();

        if (content.isEmpty()) {
            sendError(session, "Message content must not be empty");
            return;
        }

        if (!friendService.areFriends(senderId, receiverId)) {
            sendError(session, "Only friends can chat");
            return;
        }

        Message savedMessage = messageService.sendMessage(senderId, receiverId, content, messageType);
        String outboundPayload = objectMapper.writeValueAsString(savedMessage);

        sendToUser(senderId, outboundPayload);
        if (!senderId.equals(receiverId)) {
            sendToUser(receiverId, outboundPayload);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        onlineUserSessionRegistry.removeSession(getCurrentUserId(session), session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        onlineUserSessionRegistry.removeSession(getCurrentUserId(session), session);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    private void sendToUser(Long userId, String payload) throws IOException {
        Set<WebSocketSession> sessions = onlineUserSessionRegistry.getSessions(userId);
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                onlineUserSessionRegistry.removeSession(userId, session);
                continue;
            }
            session.sendMessage(new TextMessage(payload));
        }
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"" + message + "\"}"));
    }

    private Long getCurrentUserId(WebSocketSession session) {
        Object currentUserId = session.getAttributes().get("currentUserId");
        if (currentUserId == null) {
            throw new IllegalStateException("Unauthenticated WebSocket session");
        }
        return Long.valueOf(String.valueOf(currentUserId));
    }
}