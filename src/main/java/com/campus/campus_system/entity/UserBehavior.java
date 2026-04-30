package com.campus.campus_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_behavior")
public class UserBehavior {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 50)
    private String category;

    @Column(name = "behavior_type", length = 30)
    private String behaviorType;

    @Column(name = "target_type", length = 30)
    private String targetType;

    @Column(name = "target_id", length = 100)
    private String targetId;

    @Column(length = 200)
    private String keyword;

    @Column(name = "content_title", length = 255)
    private String contentTitle;

    @Column(name = "behavior_time")
    private LocalDateTime behaviorTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (behaviorTime == null) {
            behaviorTime = LocalDateTime.now();
        }
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getBehaviorType() { return behaviorType; }
    public void setBehaviorType(String behaviorType) { this.behaviorType = behaviorType; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getContentTitle() { return contentTitle; }
    public void setContentTitle(String contentTitle) { this.contentTitle = contentTitle; }
    public LocalDateTime getBehaviorTime() { return behaviorTime; }
    public void setBehaviorTime(LocalDateTime behaviorTime) { this.behaviorTime = behaviorTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
