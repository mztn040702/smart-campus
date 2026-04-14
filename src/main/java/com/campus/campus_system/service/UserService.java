package com.campus.campus_system.service;

import com.campus.campus_system.entity.User;
import com.campus.campus_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // 用户注册
    @Transactional
    public User register(String username, String password, String realName, String college) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName(realName);
        user.setCollege(college);
        user.setRole("student");
        user.setCreateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    // 用户登录
    public User login(String username, String password) {
        // 从数据库查询用户
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password))  // 验证密码
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
    }

    // 根据ID查询用户
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    // 获取所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 更新用户信息
    @Transactional
    public User updateUser(Long id, String realName, String college, String avatar) {
        User user = getUserById(id);
        if (realName != null) user.setRealName(realName);
        if (college != null) user.setCollege(college);
        if (avatar != null) user.setAvatar(avatar);
        return userRepository.save(user);
    }
}