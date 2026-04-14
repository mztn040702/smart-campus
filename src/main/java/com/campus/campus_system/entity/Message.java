package com.campus.campus_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 消息实体 - 用于点对点聊天
 */
@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;  // 发送者ID

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;  // 接收者ID

    @Column(columnDefinition = "TEXT")
    private String content;  // 消息内容

    @Column(name = "message_type", length = 20)
    private String messageType = "text";  // 消息类型：text, image, file

    @Column(name = "is_read")
    private Boolean isRead = false;  // 是否已读

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    // Getter and Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

