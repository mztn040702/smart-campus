package com.campus.campus_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户偏好实体 - 用于智能推荐
 */
@Entity
@Table(name = "user_preference")
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 50)
    private String category;  // 偏好分类：二手交易、求职、互助等

    @Column(length = 100)
    private String keyword;  // 关键词

    @Column(name = "click_count")
    private Integer clickCount = 1;  // 点击次数

    @Column(name = "last_click_time")
    private LocalDateTime lastClickTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (lastClickTime == null) {
            lastClickTime = LocalDateTime.now();
        }
    }

    // Getter and Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Integer getClickCount() { return clickCount; }
    public void setClickCount(Integer clickCount) { this.clickCount = clickCount; }
    public LocalDateTime getLastClickTime() { return lastClickTime; }
    public void setLastClickTime(LocalDateTime lastClickTime) { this.lastClickTime = lastClickTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

