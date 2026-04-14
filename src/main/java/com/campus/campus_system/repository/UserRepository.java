package com.campus.campus_system.repository;

import com.campus.campus_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// 继承JpaRepository，自动获得CRUD功能
public interface UserRepository extends JpaRepository<User, Long> {
    // 根据用户名查询用户
    Optional<User> findByUsername(String username);
}