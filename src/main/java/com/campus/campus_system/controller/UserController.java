package com.campus.campus_system.controller;

import com.campus.campus_system.entity.User;
import com.campus.campus_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserService userService;

    // 登录接口（接收JSON参数）
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = params.get("username");
            String password = params.get("password");
            User user = userService.login(username, password);
            result.put("code", 0);  // 0表示成功
            result.put("data", user);
        } catch (Exception e) {
            result.put("code", 1);  // 1表示失败
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 用户注册接口
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

    // 获取用户信息接口
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

    // 获取所有用户
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
}