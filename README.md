# 智慧餐饮SaaS平台（后端）

面向连锁餐饮品牌的一站式数字化解决方案后端服务，提供菜品管理、在线点餐、订单履约、经营数据统计等核心业务接口，支持多门店统一管理。

![Java](https://img.shields.io/badge/Java-17-blue) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-green) ![MySQL](https://img.shields.io/badge/MySQL-8.0-orange) ![Redis](https://img.shields.io/badge/Redis-6.0-red)

---

## 项目简介

专为餐饮企业定制的外卖订餐系统，采用前后端分离开发模式：

- **管理端** — 供餐饮企业员工使用：菜品管理、套餐管理、订单处理、员工管理、数据统计
- **用户端** — 供消费者使用：微信小程序在线浏览菜品、购物车、下单支付

## 项目架构

三层架构，按功能划分为三个核心模块：

| 模块 | 说明 |
|------|------|
| **sky-common** | 公共模块 — 常量、上下文、枚举、异常、工具类、统一响应格式 |
| **sky-pojo** | 数据对象模块 — Entity（表映射）、DTO（数据传输）、VO（视图对象） |
| **sky-server** | 服务模块 — Controller、Service、Mapper、配置、拦截器、启动类 |

### 数据封装规范

| 类型 | 用途 |
|------|------|
| **Entity** | 与数据库表字段一一映射，仅后端内部 ORM 操作使用 |
| **DTO** | 接收前端请求参数，限制传入字段，防止过度提交 |
| **VO** | 返回前端响应数据，精准返回所需字段，减少网络传输 |

## 功能模块

| 模块 | 功能点 | 技术方案 |
|------|--------|----------|
| 员工管理 | 登录/登出、新增/禁用/编辑/查询 | JWT + BCrypt 密码加密 |
| 分类管理 | 菜品分类/套餐分类的增删改查 | 统一状态枚举管理 |
| 菜品管理 | 新增/批量删除/起售停售/修改/分页查询 | Redis + Spring Cache 缓存加速 |
| 套餐管理 | 新增/删除/起售停售/修改/分页查询 | 多表关联 + 事务保证一致性 |
| 购物车 | 添加/删除菜品/套餐、清空 | Redis 缓存购物车数据 |
| 订单管理 | 下单/支付/取消/查看，管理端接单/拒单/派送/完成 | Spring Task 定时处理超时订单 |
| 来单提醒 | 下单后管理端实时语音播报 | WebSocket 长连接推送 |
| 数据统计 | 营业额/用户/订单/销量 Top10 | 多维度数据聚合查询 |
| 报表导出 | 营业数据 Excel 报表导出 | POI 生成多维度报表 |
| 配送管理 | 配送范围校验 | 百度地图 API 地址解析 + 距离计算 |

## 技术栈

| 类别 | 技术 |
|------|------|
| 核心框架 | Spring Boot 2.7 + Spring MVC |
| ORM | MyBatis + MyBatis-Spring |
| 数据库 | MySQL 8.0 + Redis 6.0 |
| 安全 | JWT + 自定义拦截器（管理端/用户端权限隔离）|
| 中间件 | WebSocket（实时推送）+ Spring Task（定时任务）|
| 文档 | Knife4j (Swagger) 在线接口调试 |
| 工具 | POI（报表）、阿里云 OSS（图片存储）、百度地图 API（地理编码）|

## 关键技术亮点

### 三端统一认证与权限隔离
基于 JWT 实现 Token 认证，管理端与用户端分别使用不同密钥加密；自定义拦截器按请求路径自动识别端类型，实现权限隔离。

### 菜品缓存优化
Spring Cache + Redis 缓存菜品数据，查询优先走缓存；增删改操作自动清理缓存，保证数据一致性。

### 公共字段自动填充（AOP）
自定义 `@AutoFill` 注解，AOP 切面统一拦截 insert/update 操作，自动注入 `create_time`、`update_time`、`create_user`、`update_user`，结合 ThreadLocal 实现无侵入式填充。

### 超时订单自动取消
Spring Task 每分钟扫描一次，取消超 15 分钟未支付订单，自动恢复菜品/套餐库存，事务保证一致性。

### 实时来单提醒
用户下单后 WebSocket 推送至管理端，触发语音播报，避免漏单。

### 配送范围校验
百度地图 API 将用户地址转为经纬度，计算球面距离，超出配送半径时提示用户更换地址。

## 快速开始

### 环境要求
- JDK 1.8+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 1. 创建数据库

```sql
CREATE DATABASE sky_take_out CHARACTER SET utf8mb4;
```

导入项目 SQL 文件：

```bash
mysql -u root -p sky_take_out < sky-server/src/main/resources/db/sky.sql
```

### 2. 修改配置

编辑 `sky-server/src/main/resources/application.yml`，配置数据库、Redis、JWT：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sky_take_out
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
```

### 3. 启动 Redis

```bash
redis-server
```

### 4. 启动应用

```bash
mvn clean package
java -jar sky-server/target/sky-server-*.jar
```

或直接在 IDE 中运行 `SkyApplication.java`。

### 5. 访问接口文档

启动后访问 Knife4j 在线文档：http://localhost:8080/doc.html

> 默认管理员账号：admin / 123456

## 项目结构

```
sky-take-out/
├── sky-common/               # 公共模块
│   └── src/main/java/com/sky/
│       ├── constant/         # 常量类
│       ├── context/          # 线程上下文（BaseContext）
│       ├── exception/        # 自定义异常
│       ├── properties/       # 配置属性（OSS、JWT、微信）
│       ├── result/           # 统一响应格式
│       └── utils/            # 工具类（JWT、OSS、HTTP、微信支付）
├── sky-pojo/                 # 数据对象模块
│   └── src/main/java/com/sky/
│       ├── dto/              # 数据传输对象
│       ├── entity/           # 数据实体
│       └── vo/               # 视图对象
├── sky-server/               # 服务模块
│   └── src/main/java/com/sky/
│       ├── controller/       # 接口（admin/ 管理端, user/ 用户端）
│       ├── service/          # 业务逻辑层
│       ├── mapper/           # 数据访问层
│       ├── config/           # 配置（WebMVC、Redis、OSS、WebSocket）
│       ├── interceptor/      # JWT 拦截器（管理端/用户端）
│       ├── handler/          # 全局异常处理
│       ├── aspect/           # AOP 切面（自动填充）
│       ├── task/             # 定时任务（超时订单处理）
│       └── websocket/        # WebSocket 实时通信
└── pom.xml
```

## 部署建议

| 组件 | 生产推荐方案 |
|------|-------------|
| 应用服务 | Docker 容器化 + 多实例集群 |
| MySQL | 主从复制 + 读写分离 |
| Redis | 哨兵模式或集群模式 |
| 静态资源 | Nginx 反向代理 + 负载均衡 |
| 图片存储 | 阿里云 OSS（已集成）|

## 注意事项

- 微信支付功能目前为模拟流程，生产环境需替换为真实支付接口并配置商户证书
- 阿里云 OSS 和百度地图 API 为可选配置，不影响核心功能运行

---

> 个人学习项目，通过本项目实践了企业级 Spring Boot 项目开发全流程，涵盖接口设计、缓存优化、定时任务、WebSocket 实时通信、第三方 API 集成等核心技术。
