package com.campus.campus_system.interceptor;

import com.campus.campus_system.entity.User;
import com.campus.campus_system.repository.UserRepository;
import com.campus.campus_system.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    public AuthInterceptor(JwtTokenUtil jwtTokenUtil, UserRepository userRepository) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorizedResponse(response, "Unauthorized");
            return false;
        }

        String token = authorization.substring(7);
        if (!jwtTokenUtil.isTokenValid(token)) {
            writeUnauthorizedResponse(response, "Invalid or expired token");
            return false;
        }

        Long userId = Long.valueOf(jwtTokenUtil.parseToken(token).getSubject());
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            writeUnauthorizedResponse(response, "User not found");
            return false;
        }

        String status = user.getStatus();
        if (status == null || status.isBlank()) {
            user.setStatus("ACTIVE");
            userRepository.save(user);
        } else if ("DISABLED".equalsIgnoreCase(status)) {
            writeUnauthorizedResponse(response, "Account disabled");
            return false;
        }

        request.setAttribute("currentUserId", userId);
        return true;
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"" + message + "\"}");
    }
}
