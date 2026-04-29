-- 智慧校园系统数据库初始化脚本
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS campus_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_system;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    avatar VARCHAR(255) COMMENT '头像URL',
    college VARCHAR(100) COMMENT '学院',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '账号状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 消息表
CREATE TABLE IF NOT EXISTS message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    content TEXT COMMENT '消息内容',
    message_type VARCHAR(20) DEFAULT 'text' COMMENT '消息类型',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_conversation (sender_id, receiver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 二手商品表
CREATE TABLE IF NOT EXISTS second_hand_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL COMMENT '卖家ID',
    title VARCHAR(200) NOT NULL COMMENT '商品标题',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10, 2) COMMENT '价格',
    images VARCHAR(500) COMMENT '商品图片（JSON格式）',
    category VARCHAR(50) COMMENT '分类',
    status VARCHAR(20) DEFAULT 'on_sale' COMMENT '状态：on_sale(在售), sold(已售), removed(已下架)',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_seller (seller_id),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手商品表';

-- 求职信息表
CREATE TABLE IF NOT EXISTS job_posting (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    publisher_id BIGINT NOT NULL COMMENT '发布者ID',
    title VARCHAR(200) NOT NULL COMMENT '职位标题',
    description TEXT COMMENT '职位描述',
    company VARCHAR(100) COMMENT '公司名称',
    location VARCHAR(100) COMMENT '工作地点',
    job_type VARCHAR(50) COMMENT '工作类型：全职、兼职、实习',
    salary DECIMAL(10, 2) COMMENT '薪资',
    requirements VARCHAR(200) COMMENT '任职要求',
    contact VARCHAR(50) COMMENT '联系方式',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active(有效), closed(已关闭)',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_publisher (publisher_id),
    INDEX idx_job_type (job_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='求职信息表';

-- 互助请求表
CREATE TABLE IF NOT EXISTS help_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id BIGINT NOT NULL COMMENT '求助者ID',
    title VARCHAR(200) NOT NULL COMMENT '求助标题',
    description TEXT COMMENT '求助描述',
    category VARCHAR(50) COMMENT '分类：学习、生活、技术、其他',
    location VARCHAR(100) COMMENT '地点',
    urgency VARCHAR(20) COMMENT '紧急程度：low, medium, high',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending(待帮助), helping(帮助中), completed(已完成)',
    helper_id BIGINT COMMENT '帮助者ID',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_requester (requester_id),
    INDEX idx_helper (helper_id),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='互助请求表';

-- 用户偏好表（用于智能推荐）
CREATE TABLE IF NOT EXISTS user_preference (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    category VARCHAR(50) COMMENT '偏好分类：product, job, help等',
    keyword VARCHAR(100) COMMENT '关键词',
    click_count INT DEFAULT 1 COMMENT '点击次数',
    last_click_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后点击时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user (user_id),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户偏好表';

-- 插入测试数据
-- 测试用户
INSERT INTO user (username, password, real_name, college, role, status) VALUES
('admin', 'admin123', '管理员', '计算机学院', 'ADMIN', 'ACTIVE'),
('student1', '123456', '张三', '计算机学院', 'USER', 'ACTIVE'),
('student2', '123456', '李四', '经济管理学院', 'USER', 'ACTIVE'),
('student3', '123456', '王五', '外国语学院', 'USER', 'ACTIVE');

-- 测试二手商品
INSERT INTO second_hand_product (seller_id, title, description, price, category, status) VALUES
(2, 'Java编程思想', '经典Java教材，九成新', 50.00, '书籍', 'on_sale'),
(2, 'MacBook Pro 2019', '13寸，8GB内存，256GB存储', 6000.00, '电子产品', 'on_sale'),
(3, '台灯', 'LED护眼台灯，几乎全新', 30.00, '生活用品', 'on_sale');

-- 测试职位
INSERT INTO job_posting (publisher_id, title, description, company, location, job_type, salary, contact, status) VALUES
(1, 'Java开发工程师', '负责后端开发，要求熟悉Spring Boot', 'XX科技公司', '北京', '全职', 15000.00, 'hr@example.com', 'active'),
(1, '前端开发实习生', '负责Vue.js前端开发', 'YY互联网公司', '上海', '实习', 3000.00, 'recruit@example.com', 'active');

-- 测试求助
INSERT INTO help_request (requester_id, title, description, category, location, urgency, status) VALUES
(2, '需要高数辅导', '高数期中考试需要辅导，希望有同学能帮助', '学习', '图书馆', 'high', 'pending'),
(3, '帮忙搬宿舍', '需要帮忙搬一些行李到新宿舍', '生活', '宿舍楼', 'medium', 'pending');
