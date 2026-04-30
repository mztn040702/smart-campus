SET NAMES utf8mb4;

USE campus_system;

CREATE TABLE IF NOT EXISTS user_behavior (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    category VARCHAR(50) COMMENT '推荐分类：product, job, help',
    behavior_type VARCHAR(30) COMMENT '行为类型：SEARCH, VIEW, CLICK, PUBLISH',
    target_type VARCHAR(30) COMMENT '目标类型：product, job, help',
    target_id VARCHAR(100) COMMENT '目标内容ID',
    keyword VARCHAR(200) COMMENT '搜索关键词',
    content_title VARCHAR(255) COMMENT '浏览内容标题',
    behavior_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '行为时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_behavior_user (user_id),
    INDEX idx_behavior_category (category),
    INDEX idx_behavior_time (behavior_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为表';

INSERT INTO user (username, password, real_name, nickname, email, phone, bio, college, role, status)
SELECT 'demo_reco_seller', '123456', '推荐演示卖家', '推荐卖家', 'demo_reco_seller@campus.test', '13800000991', '智能推荐商品演示账号', '计算机学院', 'USER', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'demo_reco_seller');

INSERT INTO user (username, password, real_name, nickname, email, phone, bio, college, role, status)
SELECT 'demo_reco_recruiter', '123456', '推荐演示招聘方', '推荐招聘', 'demo_reco_recruiter@campus.test', '13800000992', '智能推荐招聘演示账号', '信息工程学院', 'USER', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'demo_reco_recruiter');

INSERT INTO user (username, password, real_name, nickname, email, phone, bio, college, role, status)
SELECT 'demo_reco_helper', '123456', '推荐演示求助方', '推荐求助', 'demo_reco_helper@campus.test', '13800000993', '智能推荐互助演示账号', '软件学院', 'USER', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'demo_reco_helper');

SET @demo_product_user_id := (SELECT id FROM user WHERE username = 'demo_reco_seller' LIMIT 1);
SET @demo_job_user_id := (SELECT id FROM user WHERE username = 'demo_reco_recruiter' LIMIT 1);
SET @demo_help_user_id := (SELECT id FROM user WHERE username = 'demo_reco_helper' LIMIT 1);

DELETE p
FROM second_hand_product p
WHERE p.seller_id = @demo_product_user_id;

DELETE j
FROM job_posting j
WHERE j.publisher_id = @demo_job_user_id;

DELETE h
FROM help_request h
WHERE h.requester_id = @demo_help_user_id;

DELETE ub
FROM user_behavior ub
JOIN user u ON u.id = ub.user_id
WHERE u.username IN ('student1', 'student2');

INSERT INTO second_hand_product (
    seller_id, title, description, price, images, category, status, view_count, create_time, update_time
)
SELECT
    @demo_product_user_id,
    CASE
        WHEN seq_num <= 50 THEN
            CASE MOD(seq_num - 1, 5)
                WHEN 0 THEN CONCAT('考研数学复习资料 第', FLOOR((seq_num - 1) / 5) + 1, '套')
                WHEN 1 THEN CONCAT('考研英语真题精讲 第', FLOOR((seq_num - 1) / 5) + 1, '版')
                WHEN 2 THEN CONCAT('政治复习笔记整理版 ', FLOOR((seq_num - 1) / 5) + 1)
                WHEN 3 THEN CONCAT('计算机408复习资料包 ', FLOOR((seq_num - 1) / 5) + 1)
                ELSE CONCAT('专业课复习资料合集 ', FLOOR((seq_num - 1) / 5) + 1)
            END
        WHEN seq_num <= 100 THEN
            CASE MOD(seq_num - 51, 5)
                WHEN 0 THEN CONCAT('Java编程思想 学习版 ', FLOOR((seq_num - 51) / 5) + 1)
                WHEN 1 THEN CONCAT('Spring Boot实战 项目笔记 ', FLOOR((seq_num - 51) / 5) + 1)
                WHEN 2 THEN CONCAT('Python数据分析 课程资料 ', FLOOR((seq_num - 51) / 5) + 1)
                WHEN 3 THEN CONCAT('算法导论 重点批注本 ', FLOOR((seq_num - 51) / 5) + 1)
                ELSE CONCAT('MySQL数据库教程 练习手册 ', FLOOR((seq_num - 51) / 5) + 1)
            END
        WHEN seq_num <= 150 THEN
            CASE MOD(seq_num - 101, 5)
                WHEN 0 THEN CONCAT('二手笔记本电脑 办公学习款 ', FLOOR((seq_num - 101) / 5) + 1)
                WHEN 1 THEN CONCAT('机械键盘 青轴手感版 ', FLOOR((seq_num - 101) / 5) + 1)
                WHEN 2 THEN CONCAT('蓝牙耳机 续航增强版 ', FLOOR((seq_num - 101) / 5) + 1)
                WHEN 3 THEN CONCAT('平板电脑 上课记笔记版 ', FLOOR((seq_num - 101) / 5) + 1)
                ELSE CONCAT('显示器 宿舍外接款 ', FLOOR((seq_num - 101) / 5) + 1)
            END
        ELSE
            CASE MOD(seq_num - 151, 5)
                WHEN 0 THEN CONCAT('台灯 护眼宿舍款 ', FLOOR((seq_num - 151) / 5) + 1)
                WHEN 1 THEN CONCAT('收纳箱 搬宿舍整理款 ', FLOOR((seq_num - 151) / 5) + 1)
                WHEN 2 THEN CONCAT('折叠桌 自习备用款 ', FLOOR((seq_num - 151) / 5) + 1)
                WHEN 3 THEN CONCAT('书架 桌面整理款 ', FLOOR((seq_num - 151) / 5) + 1)
                ELSE CONCAT('电风扇 夏季静音款 ', FLOOR((seq_num - 151) / 5) + 1)
            END
    END,
    CASE
        WHEN seq_num <= 50 THEN
            CASE MOD(seq_num - 1, 5)
                WHEN 0 THEN '考研数学复习资料，包含高数、线代和概率统计重点题型，页边有我自己整理的错题标记，适合二轮到冲刺阶段复习。'
                WHEN 1 THEN '考研英语真题汇总，按年份整理，阅读和翻译部分都做了笔记，单词和长难句标注比较详细，适合想提高真题利用率的同学。'
                WHEN 2 THEN '政治复习笔记是跟着强化班整理的，重点章节和易混知识点都有颜色标记，后期背诵和查漏补缺会方便很多。'
                WHEN 3 THEN '计算机408复习资料包，包含数据结构、操作系统、组成原理和计算机网络笔记，适合报考计算机专业研究生的同学。'
                ELSE '专业课复习资料合集，包含历年题目、重点章节总结和老师划的范围，适合准备学校专业课考试或者期末复习。'
            END
        WHEN seq_num <= 100 THEN
            CASE MOD(seq_num - 51, 5)
                WHEN 0 THEN 'Java编程思想学习版，书页有核心语法和面向对象设计批注，适合准备Java基础、后端面试和课程学习。'
                WHEN 1 THEN 'Spring Boot实战项目笔记，整理了接口开发、依赖注入、配置管理和项目部署内容，适合做Java后端项目练习。'
                WHEN 2 THEN 'Python数据分析课程资料，包含NumPy、Pandas和可视化案例，适合做课程大作业、数据分析实习准备。'
                WHEN 3 THEN '算法导论重点批注本，常见排序、图论和动态规划部分标得比较细，适合算法课、竞赛和面试刷题入门。'
                ELSE 'MySQL数据库教程练习手册，附建表、索引、事务和查询优化笔记，适合数据库课程和Java后端开发学习。'
            END
        WHEN seq_num <= 150 THEN
            CASE MOD(seq_num - 101, 5)
                WHEN 0 THEN '二手笔记本电脑，适合写论文、做Java后端项目和日常办公，电池续航正常，键盘和屏幕无明显问题。'
                WHEN 1 THEN '机械键盘，回弹清脆，适合宿舍写代码、打字和做课程项目，按键完整，使用痕迹轻微。'
                WHEN 2 THEN '蓝牙耳机，续航稳定，适合自习室听网课、英语听力和通勤使用，充电盒接口正常。'
                WHEN 3 THEN '平板电脑，适合上课记笔记、看PDF和复习课件，屏幕显示正常，日常学习体验不错。'
                ELSE '显示器，适合宿舍外接笔记本写代码、做设计和双屏办公，亮度正常，无坏点。'
            END
        ELSE
            CASE MOD(seq_num - 151, 5)
                WHEN 0 THEN '护眼台灯，亮度可以调节，适合晚自习和宿舍复习考研资料，底座稳，照明范围够用。'
                WHEN 1 THEN '收纳箱，适合搬宿舍、整理衣物和杂物，容量大，轮子灵活，日常收纳很方便。'
                WHEN 2 THEN '折叠桌，适合宿舍临时自习、放平板电脑和写作业，收起来不占地方。'
                WHEN 3 THEN '桌面书架，适合放教材、考研资料和编程书，安装简单，能明显提升桌面整洁度。'
                ELSE '电风扇，夏天宿舍使用很方便，风力分档正常，噪音不大，适合学习时一直开着。'
            END
    END,
    CASE
        WHEN seq_num <= 50 THEN ROUND(18 + MOD(seq_num * 7, 35) + 0.00, 2)
        WHEN seq_num <= 100 THEN ROUND(22 + MOD(seq_num * 9, 45) + 0.00, 2)
        WHEN seq_num <= 150 THEN ROUND(180 + MOD(seq_num * 137, 4200) + 0.00, 2)
        ELSE ROUND(12 + MOD(seq_num * 5, 90) + 0.00, 2)
    END,
    NULL,
    CASE
        WHEN seq_num <= 100 THEN 'books'
        WHEN seq_num <= 150 THEN 'electronics'
        ELSE 'daily'
    END,
    'on_sale',
    20 + MOD(seq_num * 17, 320),
    DATE_SUB(NOW(), INTERVAL MOD(seq_num * 3, 120) DAY),
    DATE_SUB(NOW(), INTERVAL MOD(seq_num * 3, 120) DAY)
FROM (
    SELECT ones.n + tens.n * 10 + hundreds.n * 100 + 1 AS seq_num
    FROM
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) ones
        CROSS JOIN (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) tens
        CROSS JOIN (SELECT 0 AS n UNION ALL SELECT 1) hundreds
) seq
WHERE seq_num <= 200;

INSERT INTO job_posting (
    publisher_id, title, description, company, location, job_type, salary, requirements, contact, status, view_count, create_time, update_time
)
SELECT
    @demo_job_user_id,
    CASE
        WHEN seq_num <= 90 THEN
            CASE MOD(seq_num - 1, 6)
                WHEN 0 THEN CONCAT('Java后端实习生 ', FLOOR((seq_num - 1) / 6) + 1)
                WHEN 1 THEN CONCAT('Spring Boot开发助理 ', FLOOR((seq_num - 1) / 6) + 1)
                WHEN 2 THEN CONCAT('前端Vue实习生 ', FLOOR((seq_num - 1) / 6) + 1)
                WHEN 3 THEN CONCAT('Python数据分析实习 ', FLOOR((seq_num - 1) / 6) + 1)
                WHEN 4 THEN CONCAT('MySQL数据库助理 ', FLOOR((seq_num - 1) / 6) + 1)
                ELSE CONCAT('软件测试实习生 ', FLOOR((seq_num - 1) / 6) + 1)
            END
        WHEN seq_num <= 120 THEN
            CASE MOD(seq_num - 91, 3)
                WHEN 0 THEN CONCAT('校园新媒体运营 ', FLOOR((seq_num - 91) / 3) + 1)
                WHEN 1 THEN CONCAT('活动策划助理 ', FLOOR((seq_num - 91) / 3) + 1)
                ELSE CONCAT('社群运营实习生 ', FLOOR((seq_num - 91) / 3) + 1)
            END
        ELSE
            CASE MOD(seq_num - 121, 3)
                WHEN 0 THEN CONCAT('图书馆兼职 ', FLOOR((seq_num - 121) / 3) + 1)
                WHEN 1 THEN CONCAT('校园快递分拣 ', FLOOR((seq_num - 121) / 3) + 1)
                ELSE CONCAT('食堂兼职 ', FLOOR((seq_num - 121) / 3) + 1)
            END
    END,
    CASE
        WHEN seq_num <= 90 THEN
            CASE MOD(seq_num - 1, 6)
                WHEN 0 THEN '负责Java后端接口开发、Spring Boot项目维护和MySQL数据库基础操作，适合准备走Java后端方向的同学。'
                WHEN 1 THEN '协助老师或项目组完成Spring Boot开发、接口联调、日志排查和数据库脚本整理，能接触完整后端开发流程。'
                WHEN 2 THEN '参与Vue页面开发、组件维护和接口联调，适合对前端Vue、页面交互和校园项目感兴趣的同学。'
                WHEN 3 THEN '参与Python数据分析、报表清洗和可视化工作，需要会基础数据处理和简单脚本编写。'
                WHEN 4 THEN '主要负责MySQL数据库日常维护、SQL编写、数据整理和基础查询优化，适合数据库课程学得不错的同学。'
                ELSE '参与软件测试、接口测试和缺陷跟踪，适合做项目测试、文档整理和功能回归验证。'
            END
        WHEN seq_num <= 120 THEN
            CASE MOD(seq_num - 91, 3)
                WHEN 0 THEN '负责校园新媒体运营、选题策划、排版发布和活动宣传，需要较强文字表达和内容节奏感。'
                WHEN 1 THEN '配合完成活动策划、执行物料准备和现场流程协同，适合做学生组织或校园项目运营。'
                ELSE '维护社群运营秩序、活动通知、用户答疑和内容分发，适合沟通能力强、执行力稳定的同学。'
            END
        ELSE
            CASE MOD(seq_num - 121, 3)
                WHEN 0 THEN '负责图书馆值班、借还书辅助和自习区秩序维护，工作时间相对稳定，适合课余兼职。'
                WHEN 1 THEN '负责校园快递分拣、货架整理和高峰时段取件协助，适合想找短时兼职的同学。'
                ELSE '负责食堂窗口协助、餐品整理和高峰期服务支持，适合能适应轮班和现场服务的同学。'
            END
    END,
    CASE
        WHEN seq_num <= 90 THEN
            CASE MOD(seq_num - 1, 6)
                WHEN 0 THEN '青禾信息科技'
                WHEN 1 THEN '云川校园实验室'
                WHEN 2 THEN '星瀚数字工作室'
                WHEN 3 THEN '数析未来团队'
                WHEN 4 THEN '智联数据库中心'
                ELSE '稳测软件工作室'
            END
        WHEN seq_num <= 120 THEN
            CASE MOD(seq_num - 91, 3)
                WHEN 0 THEN '青柚校园传媒'
                WHEN 1 THEN '知行活动策划社'
                ELSE '同伴社群运营组'
            END
        ELSE
            CASE MOD(seq_num - 121, 3)
                WHEN 0 THEN '校园图书服务中心'
                WHEN 1 THEN '驿站物流服务点'
                ELSE '校园后勤饮食部'
            END
    END,
    CASE
        WHEN MOD(seq_num, 5) = 0 THEN '上海'
        WHEN MOD(seq_num, 5) = 1 THEN '杭州'
        WHEN MOD(seq_num, 5) = 2 THEN '南京'
        WHEN MOD(seq_num, 5) = 3 THEN '校内创新中心'
        ELSE '图书馆附近'
    END,
    CASE
        WHEN seq_num <= 90 THEN 'internship'
        WHEN seq_num <= 120 THEN 'part-time'
        WHEN MOD(seq_num, 2) = 0 THEN 'part-time'
        ELSE 'full-time'
    END,
    CASE
        WHEN seq_num <= 90 THEN ROUND(1800 + MOD(seq_num * 113, 2600) + 0.00, 2)
        WHEN seq_num <= 120 THEN ROUND(800 + MOD(seq_num * 43, 1400) + 0.00, 2)
        ELSE ROUND(600 + MOD(seq_num * 29, 1200) + 0.00, 2)
    END,
    CASE
        WHEN seq_num <= 90 THEN '熟悉Java、Spring Boot、MySQL、Git其一或其二，有项目经历优先，能保证每周稳定投入时间。'
        WHEN seq_num <= 120 THEN '有运营、文案、活动执行经验优先，沟通顺畅，做事细致，愿意跟进活动节奏。'
        ELSE '认真负责，守时守纪，能适应固定排班或高峰时段，服务意识较好。'
    END,
    CONCAT('demo-job-', seq_num, '@campus.test'),
    'active',
    30 + MOD(seq_num * 19, 280),
    DATE_SUB(NOW(), INTERVAL MOD(seq_num * 2, 90) DAY),
    DATE_SUB(NOW(), INTERVAL MOD(seq_num * 2, 90) DAY)
FROM (
    SELECT ones.n + tens.n * 10 + hundreds.n * 100 + 1 AS seq_num
    FROM
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) ones
        CROSS JOIN (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) tens
        CROSS JOIN (SELECT 0 AS n UNION ALL SELECT 1) hundreds
) seq
WHERE seq_num <= 150;

INSERT INTO help_request (
    requester_id, title, description, category, location, urgency, status, helper_id, view_count, create_time, update_time
)
SELECT
    @demo_help_user_id,
    CASE
        WHEN seq_num <= 50 THEN
            CASE MOD(seq_num - 1, 4)
                WHEN 0 THEN CONCAT('帮忙搬宿舍 ', FLOOR((seq_num - 1) / 4) + 1)
                WHEN 1 THEN CONCAT('搬运行李求助 ', FLOOR((seq_num - 1) / 4) + 1)
                WHEN 2 THEN CONCAT('帮忙搬书 ', FLOOR((seq_num - 1) / 4) + 1)
                ELSE CONCAT('宿舍换寝协助 ', FLOOR((seq_num - 1) / 4) + 1)
            END
        WHEN seq_num <= 100 THEN
            CASE MOD(seq_num - 51, 4)
                WHEN 0 THEN CONCAT('高数辅导 ', FLOOR((seq_num - 51) / 4) + 1)
                WHEN 1 THEN CONCAT('Java作业辅导 ', FLOOR((seq_num - 51) / 4) + 1)
                WHEN 2 THEN CONCAT('英语四六级辅导 ', FLOOR((seq_num - 51) / 4) + 1)
                ELSE CONCAT('数据库课程辅导 ', FLOOR((seq_num - 51) / 4) + 1)
            END
        ELSE
            CASE MOD(seq_num - 101, 4)
                WHEN 0 THEN CONCAT('快递代取 ', FLOOR((seq_num - 101) / 4) + 1)
                WHEN 1 THEN CONCAT('外卖代拿 ', FLOOR((seq_num - 101) / 4) + 1)
                WHEN 2 THEN CONCAT('打印资料 ', FLOOR((seq_num - 101) / 4) + 1)
                ELSE CONCAT('校园跑腿 ', FLOOR((seq_num - 101) / 4) + 1)
            END
    END,
    CASE
        WHEN seq_num <= 50 THEN
            CASE MOD(seq_num - 1, 4)
                WHEN 0 THEN '需要两位同学帮忙把行李和箱子从旧宿舍搬到新宿舍，东西不算特别多，预计半小时左右可以结束。'
                WHEN 1 THEN '周末搬运行李，主要是衣物箱和电脑桌面杂物，希望有空的同学帮忙一起抬一下。'
                WHEN 2 THEN '考研资料和教材比较多，自己一个人不好搬，想找同学帮忙把书从自习室搬回宿舍。'
                ELSE '宿舍换寝需要搭把手，主要是床上用品和两个整理箱，时间比较集中，希望晚上帮忙。'
            END
        WHEN seq_num <= 100 THEN
            CASE MOD(seq_num - 51, 4)
                WHEN 0 THEN '高数这几章积分和级数没太学明白，想找同学做一对一辅导，最好能结合期中题型讲解。'
                WHEN 1 THEN 'Java作业做到集合和接口部分卡住了，希望有人能帮我过一下思路，最好熟悉面向对象和基础语法。'
                WHEN 2 THEN '准备英语四六级，阅读和翻译部分提分比较慢，希望找同学帮忙做题讲解和单词规划。'
                ELSE '数据库课程最近在学SQL和范式，希望有同学能辅导一下查询语句、连接查询和事务部分。'
            END
        ELSE
            CASE MOD(seq_num - 101, 4)
                WHEN 0 THEN '现在人在上课，快递已经到站点，希望有空的同学帮忙代取，晚上回宿舍当面拿。'
                WHEN 1 THEN '外卖送到宿舍楼下了，我还在自习室赶作业，想请同学顺手帮忙代拿一下。'
                WHEN 2 THEN '急需打印课程汇报和简历，打印店排队时间长，希望附近同学帮忙一起带一下资料。'
                ELSE '需要一个校园跑腿帮忙送文件和取小件物品，路程不远，完成后马上转账。'
            END
    END,
    CASE
        WHEN seq_num <= 50 THEN 'life'
        WHEN seq_num <= 100 THEN 'study'
        ELSE 'life'
    END,
    CASE
        WHEN MOD(seq_num, 5) = 0 THEN '一号宿舍楼'
        WHEN MOD(seq_num, 5) = 1 THEN '图书馆'
        WHEN MOD(seq_num, 5) = 2 THEN '东区快递站'
        WHEN MOD(seq_num, 5) = 3 THEN '教学楼A区'
        ELSE '食堂门口'
    END,
    CASE
        WHEN MOD(seq_num, 3) = 0 THEN 'high'
        WHEN MOD(seq_num, 3) = 1 THEN 'medium'
        ELSE 'low'
    END,
    'pending',
    NULL,
    15 + MOD(seq_num * 23, 240),
    DATE_SUB(NOW(), INTERVAL MOD(seq_num * 4, 60) DAY),
    DATE_SUB(NOW(), INTERVAL MOD(seq_num * 4, 60) DAY)
FROM (
    SELECT ones.n + tens.n * 10 + hundreds.n * 100 + 1 AS seq_num
    FROM
        (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) ones
        CROSS JOIN (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) tens
        CROSS JOIN (SELECT 0 AS n UNION ALL SELECT 1) hundreds
) seq
WHERE seq_num <= 150;

INSERT INTO user_behavior (user_id, category, behavior_type, target_type, target_id, keyword, content_title, behavior_time, create_time)
SELECT u.id, 'job', 'SEARCH', 'job', NULL, 'Java 后端实习', NULL, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW()
FROM user u
WHERE u.username = 'student1';

INSERT INTO user_behavior (user_id, category, behavior_type, target_type, target_id, keyword, content_title, behavior_time, create_time)
SELECT u.id, 'job', 'SEARCH', 'job', NULL, 'Spring Boot 项目', NULL, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()
FROM user u
WHERE u.username = 'student1';

INSERT INTO user_behavior (user_id, category, behavior_type, target_type, target_id, keyword, content_title, behavior_time, create_time)
SELECT u.id, 'job', 'SEARCH', 'job', NULL, 'MySQL 数据库', NULL, DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()
FROM user u
WHERE u.username = 'student1';

INSERT INTO user_behavior (user_id, category, behavior_type, target_type, target_id, keyword, content_title, behavior_time, create_time)
SELECT u.id, 'product', 'VIEW', 'product', CAST(p.id AS CHAR), NULL, p.title, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()
FROM user u
JOIN second_hand_product p ON p.seller_id = @demo_product_user_id
WHERE u.username = 'student1'
  AND p.title IN ('Java编程思想 学习版 1', 'Spring Boot实战 项目笔记 1', 'MySQL数据库教程 练习手册 1');

INSERT INTO user_behavior (user_id, category, behavior_type, target_type, target_id, keyword, content_title, behavior_time, create_time)
SELECT u.id, 'job', 'VIEW', 'job', CAST(j.id AS CHAR), NULL, j.title, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()
FROM user u
JOIN job_posting j ON j.publisher_id = @demo_job_user_id
WHERE u.username = 'student1'
  AND j.title IN ('Java后端实习生 1', 'Spring Boot开发助理 1', 'MySQL数据库助理 1');

INSERT INTO user_behavior (user_id, category, behavior_type, target_type, target_id, keyword, content_title, behavior_time, create_time)
SELECT u.id, 'product', 'SEARCH', 'product', NULL, '考研资料', NULL, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW()
FROM user u
WHERE u.username = 'student2';

INSERT INTO user_behavior (user_id, category, behavior_type, target_type, target_id, keyword, content_title, behavior_time, create_time)
SELECT u.id, 'product', 'SEARCH', 'product', NULL, '研究生考试', NULL, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()
FROM user u
WHERE u.username = 'student2';

INSERT INTO user_behavior (user_id, category, behavior_type, target_type, target_id, keyword, content_title, behavior_time, create_time)
SELECT u.id, 'product', 'VIEW', 'product', CAST(p.id AS CHAR), NULL, p.title, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()
FROM user u
JOIN second_hand_product p ON p.seller_id = @demo_product_user_id
WHERE u.username = 'student2'
  AND p.title IN ('考研数学复习资料 第1套', '考研英语真题精讲 第1版', '政治复习笔记整理版 1', '计算机408复习资料包 1');
