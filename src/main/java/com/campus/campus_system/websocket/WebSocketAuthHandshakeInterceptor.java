package com.campus.campus_system.websocket;

import com.campus.campus_system.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtTokenUtil jwtTokenUtil;

    public WebSocketAuthHandshakeInterceptor(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        String token = extractToken(request);
        if (token == null || !jwtTokenUtil.isTokenValid(token)) {
            writeUnauthorizedResponse(response, "Invalid or missing token");
            return false;
        }

        Claims claims = jwtTokenUtil.parseToken(token);
        attributes.put("currentUserId", Long.valueOf(claims.getSubject()));
        attributes.put("username", claims.get("username", String.class));
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }

    private String extractToken(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token != null && !token.isBlank()) {
                return normalizeToken(token);
            }
        }

        String query = request.getURI().getQuery();
        if (query == null || query.isBlank()) {
            return null;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && "token".equals(parts[0]) && !parts[1].isBlank()) {
                return normalizeToken(parts[1]);
            }
        }

        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            return normalizeToken(authHeaders.get(0));
        }

        return null;
    }

    private String normalizeToken(String token) {
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    private void writeUnauthorizedResponse(ServerHttpResponse response, String message) throws IOException {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        if (response instanceof ServletServerHttpResponse servletResponse) {
            servletResponse.getServletResponse().setCharacterEncoding("UTF-8");
            servletResponse.getServletResponse().setContentType("application/json;charset=UTF-8");
            servletResponse.getServletResponse().getWriter()
                    .write("{\"code\":401,\"msg\":\"" + message + "\"}");
        }
    }
}
