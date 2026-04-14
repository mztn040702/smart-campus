# 快速启动指南

## 第一步：准备数据库

1. 确保 MySQL 已安装并运行
2. 打开 MySQL 命令行或客户端工具
3. 执行以下命令创建数据库：

```sql
CREATE DATABASE campus_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

4. 执行初始化脚本（在项目根目录执行）：

```bash
mysql -u root -p campus_system < database/init.sql
```

或者直接在 MySQL 客户端中执行 `database/init.sql` 文件的内容。

## 第二步：配置数据库连接

编辑 `src/main/resources/application.properties` 文件，修改数据库连接信息：

```properties
spring.datasource.username=root
spring.datasource.password=你的MySQL密码
```

## 第三步：启动后端

在项目根目录执行：

```bash
mvn spring-boot:run
```

等待后端启动完成，看到类似以下信息表示启动成功：
```
Started CampusSystemApplication in X.XXX seconds
```

后端服务运行在：`http://localhost:8080`

## 第四步：启动前端

打开新的终端窗口，进入前端目录：

```bash
cd frontend
```

安装依赖（首次运行需要）：

```bash
npm install
```

启动开发服务器：

```bash
npm run dev
```

前端服务运行在：`http://localhost:3000`

## 第五步：访问系统

打开浏览器访问：`http://localhost:3000`

### 测试账号

系统已预置以下测试账号：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| student1 | 123456 | 学生 |
| student2 | 123456 | 学生 |
| student3 | 123456 | 学生 |

## 功能测试

1. **登录系统**：使用测试账号登录
2. **查看推荐**：首页会显示智能推荐的内容
3. **发布商品**：进入"二手交易"页面，点击"发布商品"
4. **发布职位**：进入"求职招聘"页面，点击"发布职位"
5. **发布求助**：进入"互助"页面，点击"发布求助"
6. **发送消息**：进入"消息"页面，选择联系人发送消息

## 常见问题

### 1. 后端启动失败

- 检查 MySQL 是否运行
- 检查数据库连接配置是否正确
- 检查端口 8080 是否被占用

### 2. 前端启动失败

- 确保已安装 Node.js（版本 16+）
- 删除 `frontend/node_modules` 文件夹，重新执行 `npm install`
- 检查端口 3000 是否被占用

### 3. 数据库连接错误

- 检查 MySQL 服务是否启动
- 检查用户名和密码是否正确
- 检查数据库 `campus_system` 是否已创建

### 4. 前端无法访问后端

- 检查后端是否正常启动
- 检查 `frontend/vite.config.js` 中的代理配置
- 检查浏览器控制台是否有错误信息

## 下一步

- 查看 `README.md` 了解详细的项目说明
- 查看 API 接口文档了解后端接口
- 根据需要修改和扩展功能

