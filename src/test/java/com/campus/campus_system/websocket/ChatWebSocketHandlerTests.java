package com.campus.campus_system.websocket;

import com.campus.campus_system.entity.Message;
import com.campus.campus_system.service.FriendService;
import com.campus.campus_system.service.MessageService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatWebSocketHandlerTests {

    @Test
    void handleTextMessageStoresMessageAndPushesToOnlineReceiver() throws Exception {
        MessageService messageService = mock(MessageService.class);
        FriendService friendService = mock(FriendService.class);
        OnlineUserSessionRegistry registry = new OnlineUserSessionRegistry();
        ChatWebSocketHandler handler = new ChatWebSocketHandler(messageService, registry, friendService);

        WebSocketSession senderSession = mock(WebSocketSession.class);
        when(senderSession.getAttributes()).thenReturn(Map.of("currentUserId", 1L));
        when(senderSession.isOpen()).thenReturn(true);

        WebSocketSession receiverSession = mock(WebSocketSession.class);
        when(receiverSession.getAttributes()).thenReturn(Map.of("currentUserId", 2L));
        when(receiverSession.isOpen()).thenReturn(true);

        registry.addSession(1L, senderSession);
        registry.addSession(2L, receiverSession);
        when(friendService.areFriends(1L, 2L)).thenReturn(true);

        Message savedMessage = new Message();
        savedMessage.setId(99L);
        savedMessage.setSenderId(1L);
        savedMessage.setReceiverId(2L);
        savedMessage.setContent("hello");
        savedMessage.setMessageType("text");
        savedMessage.setIsRead(false);
        savedMessage.setCreateTime(LocalDateTime.of(2026, 4, 29, 12, 0));
        when(messageService.sendMessage(1L, 2L, "hello", "text")).thenReturn(savedMessage);

        handler.handleTextMessage(senderSession, new TextMessage("""
                {"senderId":999,"receiverId":2,"content":"hello","messageType":"text"}
                """));

        verify(messageService).sendMessage(1L, 2L, "hello", "text");

        ArgumentCaptor<TextMessage> outbound = ArgumentCaptor.forClass(TextMessage.class);
        verify(senderSession, times(1)).sendMessage(outbound.capture());
        verify(receiverSession, times(1)).sendMessage(any(TextMessage.class));

        assertThat(outbound.getValue().getPayload()).contains("\"senderId\":1");
        assertThat(outbound.getValue().getPayload()).contains("\"receiverId\":2");
        assertThat(outbound.getValue().getPayload()).contains("\"content\":\"hello\"");
    }

    @Test
    void afterConnectionClosedRemovesSessionFromRegistry() throws Exception {
        MessageService messageService = mock(MessageService.class);
        FriendService friendService = mock(FriendService.class);
        OnlineUserSessionRegistry registry = new OnlineUserSessionRegistry();
        ChatWebSocketHandler handler = new ChatWebSocketHandler(messageService, registry, friendService);

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(Map.of("currentUserId", 7L));
        when(session.getId()).thenReturn("session-7");

        registry.addSession(7L, session);
        assertThat(registry.getSessions(7L)).hasSize(1);

        handler.afterConnectionClosed(session, null);

        assertThat(registry.getSessions(7L)).isEmpty();
    }

    @Test
    void handleTextMessageRejectsNonFriends() throws Exception {
        MessageService messageService = mock(MessageService.class);
        FriendService friendService = mock(FriendService.class);
        OnlineUserSessionRegistry registry = new OnlineUserSessionRegistry();
        ChatWebSocketHandler handler = new ChatWebSocketHandler(messageService, registry, friendService);

        WebSocketSession senderSession = mock(WebSocketSession.class);
        when(senderSession.getAttributes()).thenReturn(Map.of("currentUserId", 1L));
        when(senderSession.isOpen()).thenReturn(true);
        when(friendService.areFriends(1L, 2L)).thenReturn(false);

        handler.handleTextMessage(senderSession, new TextMessage("""
                {"receiverId":2,"content":"hello","messageType":"text"}
                """));

        verify(messageService, times(0)).sendMessage(any(), any(), any(), any());
        verify(senderSession, times(1)).sendMessage(any(TextMessage.class));
    }
}
