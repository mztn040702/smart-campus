package com.campus.campus_system.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 二手商品实体
 */
@Entity
@Table(name = "second_hand_product")
public class SecondHandProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;  // 卖家ID

    @Column(nullable = false, length = 200)
    private String title;  // 商品标题

    @Column(columnDefinition = "TEXT")
    private String description;  // 商品描述

    @Column(precision = 10, scale = 2)
    private BigDecimal price;  // 价格

    @Column(length = 500)
    private String images;  // 商品图片（JSON格式存储多个图片URL）

    @Column(length = 50)
    private String category;  // 分类：书籍、电子产品、生活用品等

    @Column(length = 20)
    private String status = "on_sale";  // 状态：on_sale(在售), sold(已售), removed(已下架)

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
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

