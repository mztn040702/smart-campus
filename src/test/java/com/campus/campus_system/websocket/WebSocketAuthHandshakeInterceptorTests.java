package com.campus.campus_system.websocket;

import com.campus.campus_system.util.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketAuthHandshakeInterceptorTests {

    private final JwtTokenUtil jwtTokenUtil =
            new JwtTokenUtil("smart-campus-jwt-secret-key-smart-campus-jwt-secret-key", 86400000L);

    @Test
    void beforeHandshakeRejectsMissingToken() throws Exception {
        WebSocketAuthHandshakeInterceptor interceptor = new WebSocketAuthHandshakeInterceptor(jwtTokenUtil);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/ws/chat");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        boolean allowed = interceptor.beforeHandshake(
                new ServletServerHttpRequest(servletRequest),
                new ServletServerHttpResponse(servletResponse),
                null,
                new HashMap<>()
        );

        assertThat(allowed).isFalse();
        assertThat(servletResponse.getStatus()).isEqualTo(401);
    }

    @Test
    void beforeHandshakeAcceptsValidTokenAndStoresCurrentUserId() throws Exception {
        WebSocketAuthHandshakeInterceptor interceptor = new WebSocketAuthHandshakeInterceptor(jwtTokenUtil);
        String token = jwtTokenUtil.generateToken(15L, "student1");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/ws/chat");
        servletRequest.setParameter("token", token);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        Map<String, Object> attributes = new HashMap<>();

        boolean allowed = interceptor.beforeHandshake(
                new ServletServerHttpRequest(servletRequest),
                new ServletServerHttpResponse(servletResponse),
                null,
                attributes
        );

        assertThat(allowed).isTrue();
        assertThat(attributes).containsEntry("currentUserId", 15L);
        assertThat(attributes).containsEntry("username", "student1");
    }
}
