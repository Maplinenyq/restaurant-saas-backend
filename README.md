
智慧餐饮SaaS平台（后端）

项目简介
面向连锁餐饮品牌的SaaS管理后台后端服务，提供菜品、订单、员工管理及数据统计接口。

技术栈
- Spring Boot、MyBatis、MySQL
- Redis（缓存、分布式锁）
- WebSocket、Spring Task
- JWT、Knife4j

核心功能
- 用户认证：JWT + 拦截器
- 菜品缓存：Redis + Spring Cache
- 订单处理：WebSocket实时推送 + 定时取消
- 报表导出：POI

运行方式
1. 导入SQL文件
2. 修改数据库、Redis配置
3. 启动Redis
4. 运行Spring Boot主类
5. 访问 `/doc.html`

备注
个人项目，独立完成后端核心接口。
