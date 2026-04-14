# 智慧校园系统

一个基于 Spring Boot + Vue 3 + MySQL 的智慧校园系统，包含点对点聊天、二手交易、求职招聘、互助和智能推荐等功能。

## 功能模块

1. **用户管理**：注册、登录、用户信息管理
2. **点对点聊天**：类似微信的即时消息功能
3. **二手交易**：发布、浏览、搜索二手商品
4. **求职招聘**：发布职位、浏览职位信息
5. **互助**：发布求助、接受帮助、完成帮助
6. **智能推荐**：基于用户偏好的个性化推荐

## 技术栈

### 后端
- Spring Boot 3.5.7
- Spring Data JPA
- MySQL 8.0
- Maven

### 前端
- Vue 3
- Vue Router
- Element Plus
- Axios
- Vite

## 项目结构

```
campus-system/
├── src/                          # 后端代码
│   └── main/
│       ├── java/
│       │   └── com/campus/campus_system/
│       │       ├── config/       # 配置类
│       │       ├── controller/   # 控制器
│       │       ├── entity/       # 实体类
│       │       ├── repository/   # 数据访问层
│       │       └── service/      # 业务逻辑层
│       └── resources/
│           └── application.properties
├── frontend/                     # 前端代码
│   ├── src/
│   │   ├── views/               # 页面组件
│   │   ├── router/              # 路由配置
│   │   └── utils/               # 工具类
│   ├── package.json
│   └── vite.config.js
└── database/                     # 数据库脚本
    └── init.sql
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+

### 2. 数据库配置

1. 创建 MySQL 数据库：
```sql
CREATE DATABASE campus_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p campus_system < database/init.sql
```

3. 修改 `src/main/resources/application.properties` 中的数据库连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/campus_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=你的MySQL密码
```

### 3. 启动后端

```bash
# 在项目根目录执行
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 4. 启动前端

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

### 5. 访问系统

打开浏览器访问：`http://localhost:3000`

**测试账号：**
- 用户名：`student1`，密码：`123456`
- 用户名：`student2`，密码：`123456`
- 用户名：`admin`，密码：`admin123`

## API 接口说明

### 用户相关
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/{id}` - 获取用户信息
- `GET /api/user/list` - 获取所有用户

### 消息相关
- `POST /api/message/send` - 发送消息
- `GET /api/message/conversation` - 获取对话记录
- `GET /api/message/contacts/{userId}` - 获取联系人列表
- `GET /api/message/unread/{userId}` - 获取未读消息

### 二手商品相关
- `POST /api/product/publish` - 发布商品
- `GET /api/product/list` - 获取商品列表
- `GET /api/product/search` - 搜索商品
- `GET /api/product/{id}` - 获取商品详情

### 求职相关
- `POST /api/job/publish` - 发布职位
- `GET /api/job/list` - 获取职位列表
- `GET /api/job/search` - 搜索职位
- `GET /api/job/{id}` - 获取职位详情

### 互助相关
- `POST /api/help/publish` - 发布求助
- `GET /api/help/list` - 获取求助列表
- `POST /api/help/accept` - 接受帮助
- `POST /api/help/complete/{id}` - 完成帮助

### 推荐相关
- `GET /api/recommend/products/{userId}` - 获取推荐商品
- `GET /api/recommend/jobs/{userId}` - 获取推荐职位
- `GET /api/recommend/helps/{userId}` - 获取推荐求助

## 开发说明

### 后端开发
- 使用 JPA 进行数据库操作
- 实体类使用 `@Entity` 注解
- Repository 继承 `JpaRepository`
- Service 层处理业务逻辑
- Controller 层提供 RESTful API

### 前端开发
- 使用 Vue 3 Composition API
- 使用 Element Plus 组件库
- 使用 Axios 进行 HTTP 请求
- 路由使用 Vue Router

## 注意事项

1. 确保 MySQL 服务已启动
2. 确保端口 8080 和 3000 未被占用
3. 首次运行需要执行数据库初始化脚本
4. 密码未加密，生产环境请使用加密存储

## 后续优化建议

1. 添加 JWT 认证
2. 实现 WebSocket 实时消息推送
3. 添加文件上传功能
4. 实现更复杂的推荐算法
5. 添加管理员后台
6. 优化 UI/UX 设计
7. 添加单元测试和集成测试

## 许可证

MIT License

