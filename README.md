# 智慧餐饮SaaS平台（后端）

## 项目简介

面向连锁餐饮品牌的一站式数字化解决方案后端服务，提供菜品管理、在线点餐、订单履约、经营数据统计等核心业务接口，支持多门店统一管理。

## 技术栈

- **核心框架**：Spring Boot、Spring MVC、MyBatis
- **数据库**：MySQL、Redis
- **中间件**：WebSocket、Spring Task
- **安全**：JWT、自定义拦截器
- **文档**：Knife4j（Swagger）
- **工具**：POI、阿里云OSS、百度地图API

## 核心功能

| 模块 | 技术方案 |
|------|----------|
| 三端统一认证 | JWT + 自定义拦截器，管理端与用户端权限隔离 |
| 菜品缓存 | Redis + Spring Cache，降低数据库查询压力 |
| 公共字段填充 | AOP + 自定义注解，自动注入创建/更新时间及操作人 |
| 订单管理 | Spring Task定时扫描超时订单，自动取消并恢复库存 |
| 实时来单提醒 | WebSocket长连接推送，管理端语音播报 |
| 配送范围校验 | 百度地图API，地址转经纬度计算距离 |
| 营业报表导出 | POI生成多维度Excel报表 |
| 文件存储 | 阿里云OSS菜品图片云端管理 |
| 接口文档 | Knife4j在线调试，前后端协作提效 |

## 快速运行

1. 导入 SQL 文件至 MySQL
2. 修改 `application.yml` 中数据库、Redis 连接配置
3. 启动本地 Redis 服务
4. 运行 Spring Boot 主类
5. 访问 `http://localhost:8080/doc.html` 查看接口文档

## 项目结构
