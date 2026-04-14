package com.campus.campus_system.repository;

import com.campus.campus_system.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // 查询两个用户之间的所有消息
    @Query("SELECT m FROM Message m WHERE (m.senderId = :userId1 AND m.receiverId = :userId2) OR (m.senderId = :userId2 AND m.receiverId = :userId1) ORDER BY m.createTime ASC")
    List<Message> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // 查询用户的所有未读消息
    List<Message> findByReceiverIdAndIsReadFalse(Long receiverId);

    // 查询与用户相关的所有联系人（发送过消息或接收过消息的用户）
    @Query("SELECT DISTINCT CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END FROM Message m WHERE m.senderId = :userId OR m.receiverId = :userId")
    List<Long> findContactUserIds(@Param("userId") Long userId);
}

