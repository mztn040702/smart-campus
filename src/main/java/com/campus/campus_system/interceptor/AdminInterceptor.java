package com.campus.campus_system.interceptor;

import com.campus.campus_system.entity.User;
import com.campus.campus_system.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    private final UserRepository userRepository;

    public AdminInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object currentUserId = request.getAttribute("currentUserId");
        if (!(currentUserId instanceof Long userId)) {
            writeForbiddenResponse(response, "Forbidden");
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            writeForbiddenResponse(response, "Admin access required");
            return false;
        }

        return true;
    }

    private void writeForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"msg\":\"" + message + "\"}");
    }
}
