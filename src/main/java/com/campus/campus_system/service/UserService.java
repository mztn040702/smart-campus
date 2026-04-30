package com.campus.campus_system.service;

import com.campus.campus_system.entity.User;
import com.campus.campus_system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String username, String password, String realName, String college) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setCollege(college);
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setCreateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String status = normalizeUserStatus(user);
        if ("DISABLED".equalsIgnoreCase(status)) {
            throw new RuntimeException("Account disabled");
        }

        return user;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User getCurrentUserProfile(Long currentUserId) {
        User user = getUserById(currentUserId);
        normalizeUserStatus(user);
        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUser(Long id, String realName, String college, String avatar) {
        User user = getUserById(id);
        if (realName != null) {
            user.setRealName(realName);
        }
        if (college != null) {
            user.setCollege(college);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        return userRepository.save(user);
    }

    @Transactional
    public User updateCurrentUserProfile(
            Long currentUserId,
            String nickname,
            String email,
            String phone,
            String bio,
            String avatar
    ) {
        User user = getUserById(currentUserId);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPhone(phone);
        user.setBio(bio);
        user.setAvatar(avatar);
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long currentUserId, String oldPassword, String newPassword) {
        User user = getUserById(currentUserId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public String normalizeUserStatus(User user) {
        String status = user.getStatus();
        if (status == null || status.isBlank()) {
            user.setStatus("ACTIVE");
            userRepository.save(user);
            return "ACTIVE";
        }
        return status;
    }
}
