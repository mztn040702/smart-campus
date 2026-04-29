package com.campus.campus_system.controller;

import com.campus.campus_system.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import com.campus.campus_system.service.UserService;
import com.campus.campus_system.util.JwtTokenUtil;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    public UserController(UserService userService, JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = params.get("username");
            String password = params.get("password");
            User user = userService.login(username, password);
            result.put("code", 0);
            result.put("data", user);
            result.put("token", jwtTokenUtil.generateToken(user.getId(), user.getUsername()));
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = params.get("username");
            String password = params.get("password");
            String realName = params.get("realName");
            String college = params.get("college");
            User user = userService.register(username, password, realName, college);
            result.put("code", 0);
            result.put("data", user);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUserInfo(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.getUserById(id);
            result.put("code", 0);
            result.put("data", user);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/list")
    public Map<String, Object> getAllUsers() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("code", 0);
            result.put("data", userService.getAllUsers());
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/profile")
    public Map<String, Object> getCurrentUserProfile(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.getCurrentUserProfile(getCurrentUserId(request));
            result.put("code", 0);
            result.put("data", user);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @PutMapping("/profile")
    public Map<String, Object> updateCurrentUserProfile(
            @RequestBody Map<String, String> params,
            HttpServletRequest request
    ) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.updateCurrentUserProfile(
                    getCurrentUserId(request),
                    params.get("nickname"),
                    params.get("email"),
                    params.get("phone"),
                    params.get("bio"),
                    params.get("avatar")
            );
            result.put("code", 0);
            result.put("data", user);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @PutMapping("/password")
    public Map<String, Object> changePassword(
            @RequestBody Map<String, String> params,
            HttpServletRequest request
    ) {
        Map<String, Object> result = new HashMap<>();
        try {
            userService.changePassword(
                    getCurrentUserId(request),
                    params.get("oldPassword"),
                    params.get("newPassword")
            );
            result.put("code", 0);
            result.put("msg", "Password updated");
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Object value = request.getAttribute("currentUserId");
        if (value instanceof Long id) {
            return id;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new RuntimeException("Unauthorized");
    }
}
