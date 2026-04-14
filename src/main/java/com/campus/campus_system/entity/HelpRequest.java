package com.campus.campus_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 互助请求实体
 */
@Entity
@Table(name = "help_request")
public class HelpRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;  // 求助者ID

    @Column(nullable = false, length = 200)
    private String title;  // 求助标题

    @Column(columnDefinition = "TEXT")
    private String description;  // 求助描述

    @Column(length = 50)
    private String category;  // 分类：学习、生活、技术、其他

    @Column(length = 100)
    private String location;  // 地点

    @Column(length = 20)
    private String urgency;  // 紧急程度：low, medium, high

    @Column(length = 20)
    private String status = "pending";  // 状态：pending(待帮助), helping(帮助中), completed(已完成)

    @Column(name = "helper_id")
    private Long helperId;  // 帮助者ID

    @Column(name = "view_count")
    private Integer viewCount = 0;  // 浏览次数

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    // Getter and Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getHelperId() { return helperId; }
    public void setHelperId(Long helperId) { this.helperId = helperId; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

