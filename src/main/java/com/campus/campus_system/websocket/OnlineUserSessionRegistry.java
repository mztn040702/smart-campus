package com.campus.campus_system.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineUserSessionRegistry {
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessionsByUserId = new ConcurrentHashMap<>();

    public void addSession(Long userId, WebSocketSession session) {
        sessionsByUserId
                .computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    public void removeSession(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsByUserId.get(userId);
        if (sessions == null) {
            return;
        }

        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByUserId.remove(userId);
        }
    }

    public Set<WebSocketSession> getSessions(Long userId) {
        return sessionsByUserId.getOrDefault(userId, Collections.emptySet());
    }
}
