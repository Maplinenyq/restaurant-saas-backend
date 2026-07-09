📖 项目简介
苍穹外卖 是一款专为餐饮企业（餐厅、饭店）定制的外卖订餐系统。项目采用前后端分离的开发模式，包含系统管理后台和小程序用户端两部分：

管理端：供餐饮企业内部员工使用，进行菜品、套餐、订单、员工管理及数据统计

用户端：供消费者使用，通过微信小程序在线浏览菜品、添加购物车、下单支付

🧱 项目架构
项目采用三层架构设计，按功能划分为三个核心模块：

模块	说明	职责
sky-common	公共模块	常量类、上下文、枚举、异常、工具类、统一响应格式
sky-pojo	数据对象模块	Entity（表映射）、DTO（数据传输）、VO（视图对象）
sky-server	服务模块	Controller、Service、Mapper、配置类、拦截器、启动类
数据封装规范
项目中通过四层数据对象实现前后端交互的标准化与安全性：

类型	包路径	说明	使用场景
Entity	com.sky.entity	与数据库表字段一一映射	仅后端内部使用，ORM 操作
DTO	com.sky.dto	数据传输对象，按前端需求定义	Controller 层接收前端请求参数
VO	com.sky.vo	视图对象，按前端展示需求定义	Controller 层返回给前端的响应数据
POJO	com.sky.pojo	通用数据对象	部分扩展场景使用
设计原则：不直接将 Entity 暴露给前端，通过 DTO 限制传入字段（防止过度提交），通过 VO 精准返回所需字段（减少网络传输），兼顾安全性与性能。

🗂️ 核心功能模块
模块	功能点	技术方案
员工管理	登录/登出、新增/禁用/编辑/查询员工	JWT + BCrypt 密码加密
分类管理	菜品分类/套餐分类的增删改查	统一状态枚举管理
菜品管理	新增/批量删除/起售停售/修改/分页查询	Redis + Spring Cache 缓存加速
套餐管理	新增/删除/起售停售/修改/分页查询	多表关联操作，事务保证一致性
购物车	添加/删除菜品/套餐、清空购物车	Redis 缓存购物车数据
订单管理	用户下单/订单支付/取消/查看，管理端接单/拒单/派送/完成	Spring Task 定时处理超时订单
来单提醒	用户端下单后管理端实时语音播报	WebSocket 长连接推送
数据统计	营业额/用户/订单/销量 Top10 统计	多维度数据聚合查询
报表导出	营业数据 Excel 报表导出	POI 生成多维度报表
配送管理	配送范围校验	百度地图 API 地址解析 + 距离计算
🛠️ 技术栈
类别	技术	版本	说明
核心框架	Spring Boot	2.7.x	基础容器框架
Spring MVC	-	Web 层框架
ORM 框架	MyBatis	-	持久层框架
MyBatis-Spring	-	Spring 整合
数据库	MySQL	8.0+	关系型数据库
Redis	6.0+	缓存 + 分布式 Session
中间件	WebSocket	-	实时双向通信
Spring Task	-	定时任务调度
安全框架	JWT	0.9.x	身份认证
自定义拦截器	-	权限隔离（管理端/用户端）
文档工具	Knife4j	3.0.x	接口文档 + 在线调试
工具库	POI	3.1.x	Excel 报表生成
阿里云 OSS	-	菜品图片云端存储
百度地图 API	-	地址解析 + 距离计算
JSON 处理	Jackson	-	JSON 序列化/反序列化
日志	Logback	-	日志记录
🔄 业务流程概览
text
┌────────────────────────────────────────────────────────────────────┐
│                        用户端（微信小程序）                         │
│  浏览菜品 → 加入购物车 → 下单 → 微信支付（模拟） → 查看订单状态      │
└────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────┐
│                    管理端（Web 后台）                              │
│  接单提醒（WebSocket） → 接单/拒单 → 派送 → 完成 → 数据统计报表    │
│  菜品管理 | 套餐管理 | 分类管理 | 员工管理                         │
└────────────────────────────────────────────────────────────────────┘
⚙️ 关键技术亮点
1. 三端统一认证与权限隔离
基于 JWT 实现 Token 认证，管理端与用户端分别使用不同密钥加密

自定义拦截器按请求路径自动识别端类型，实现权限隔离

管理端 Token 有效期 2 小时，用户端 Token 有效期 7 天

2. 菜品缓存优化
使用 Spring Cache 结合 Redis 缓存菜品数据

缓存 Key 设计：dish_{categoryId}_{status}，查询优先走缓存

增删改操作自动删除缓存，保证数据一致性

3. 公共字段自动填充（AOP）
自定义 @AutoFill 注解，标记需要自动填充的实体字段

通过 AOP 切面统一拦截 insert/update 操作，自动注入：

create_time / update_time

create_user / update_user

结合 ThreadLocal 存储当前登录用户信息，实现无侵入式填充

4. 超时订单自动取消
使用 Spring Task 定时任务，每分钟扫描一次

查询下单超 15 分钟且未支付的订单，自动取消并恢复菜品/套餐库存

事务控制保证订单取消与库存恢复的一致性

5. 实时来单提醒
用户下单后，管理端通过 WebSocket 接收实时推送

管理端页面触发语音播报（音频自动播放），避免漏单

支持 WebSocket 心跳保活，保证连接稳定性

6. 配送范围校验
调用 百度地图 API 将用户地址转换为经纬度坐标

计算用户位置与门店中心点的球面距离

超出设定配送半径时提示用户更换地址

7. 营业报表导出
使用 POI 生成 Excel 报表，包含：

每日营业额概览

用户增长趋势

订单数量统计

销量 Top10 菜品/套餐

支持按日期范围导出，管理端一键下载

8. 文件云端管理
整合阿里云 OSS 存储菜品图片

统一文件上传接口，支持批量上传

返回 OSS 访问 URL，前端直接展示

📁 项目结构
text
sky-take-out/
├── sky-common/                      # 公共模块
│   ├── src/main/java/com/sky/
│   │   ├── constant/                # 常量类（JWT、Redis Key 等）
│   │   ├── context/                 # ThreadLocal 上下文（用户信息）
│   │   ├── enums/                   # 枚举（状态码、操作类型等）
│   │   ├── exception/               # 自定义异常
│   │   ├── json/                    # JSON 序列化处理
│   │   ├── properties/              # 配置属性类（JWT、OSS 等）
│   │   ├── result/                  # 统一响应格式（Result）
│   │   └── utils/                   # 工具类（JWT、阿里云 OSS、百度地图等）
│
├── sky-pojo/                        # 数据对象模块
│   ├── src/main/java/com/sky/
│   │   ├── entity/                  # 表映射实体
│   │   ├── dto/                     # 数据传输对象（请求参数）
│   │   └── vo/                      # 视图对象（响应数据）
│
└── sky-server/                      # 服务模块（核心业务）
    └── src/main/java/com/sky/
        ├── controller/              # 控制器层
        │   ├── admin/               # 管理端 Controller
        │   └── user/                # 用户端 Controller
        ├── service/                 # 业务层
        │   ├── admin/               # 管理端 Service
        │   └── user/                # 用户端 Service
        ├── mapper/                  # 数据访问层（MyBatis Mapper）
        ├── annotation/              # 自定义注解（@AutoFill 等）
        ├── aspect/                  # AOP 切面（自动填充切面）
        ├── config/                  # 配置类（WebMVC、WebSocket、Redis、OSS）
        ├── interceptor/             # 拦截器（JWT 认证拦截器）
        ├── task/                    # 定时任务（超时订单处理）
        ├── websocket/               # WebSocket 服务端
        └── SkyApplication.java      # 启动类
🚀 快速运行
环境要求
组件	版本要求
JDK	1.8+
MySQL	8.0+
Redis	6.0+
Maven	3.6+
运行步骤
克隆项目

bash
git clone https://github.com/your-username/sky-take-out.git
cd sky-take-out
导入 SQL 文件

bash
# 创建数据库
CREATE DATABASE sky_take_out CHARACTER SET utf8mb4;

# 导入 SQL 文件
mysql -u root -p sky_take_out < sky-server/src/main/resources/db/sky.sql
修改配置文件

编辑 sky-server/src/main/resources/application.yml：

yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sky_take_out?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

  # Redis 配置
  redis:
    host: localhost
    port: 6379
    password:  # 无密码则留空

# JWT 配置
sky:
  jwt:
    admin-secret-key: your_admin_secret_key
    user-secret-key: your_user_secret_key

  # 阿里云 OSS 配置（可选，如需上传图片）
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: your_access_key_id
    access-key-secret: your_access_key_secret
    bucket-name: your_bucket_name

  # 百度地图 API 配置（可选，如需配送校验）
  baidu:
    map:
      ak: your_baidu_map_ak
启动 Redis 服务

bash
# macOS / Linux
redis-server

# Windows
redis-server.exe
启动应用

在 IDE 中运行 SkyApplication.java，或使用 Maven：

bash
mvn clean package
java -jar sky-server/target/sky-server-*.jar
访问接口文档

启动成功后，访问 Knife4j 在线接口文档：

text
http://localhost:8080/doc.html
默认登录账号/密码：admin / 123456

📊 数据模型（核心表结构）
表名	说明	关键字段
employee	员工表	id, name, username, password, phone, status
category	菜品/套餐分类表	id, name, type, sort, status
dish	菜品表	id, name, category_id, price, image, status
dish_flavor	菜品口味表	id, dish_id, name, value
setmeal	套餐表	id, name, category_id, price, status
setmeal_dish	套餐菜品关联表	id, setmeal_id, dish_id, copies
shopping_cart	购物车表	id, user_id, dish_id, setmeal_id, number, amount
orders	订单表	id, number, user_id, amount, status, pay_status
order_detail	订单明细表	id, order_id, dish_id, setmeal_id, number, amount
user	用户表	id, openid, name, phone, address
📌 接口清单（按模块）
管理端接口
模块	接口	Method	路径
员工	登录	POST	/admin/employee/login
退出	POST	/admin/employee/logout
新增员工	POST	/admin/employee
员工分页查询	GET	/admin/employee/page
启用/禁用员工	POST	/admin/employee/status/{status}
编辑员工	PUT	/admin/employee
根据ID查询员工	GET	/admin/employee/{id}
分类	新增分类	POST	/admin/category
分类分页查询	GET	/admin/category/page
删除分类	DELETE	/admin/category
修改分类	PUT	/admin/category
启用/禁用分类	POST	/admin/category/status/{status}
根据类型查询分类	GET	/admin/category/list
菜品	新增菜品	POST	/admin/dish
菜品分页查询	GET	/admin/dish/page
删除菜品	DELETE	/admin/dish
修改菜品	PUT	/admin/dish
根据ID查询菜品	GET	/admin/dish/{id}
起售/停售菜品	POST	/admin/dish/status/{status}
根据分类ID查询菜品	GET	/admin/dish/list
套餐	新增套餐	POST	/admin/setmeal
套餐分页查询	GET	/admin/setmeal/page
删除套餐	DELETE	/admin/setmeal
修改套餐	PUT	/admin/setmeal
根据ID查询套餐	GET	/admin/setmeal/{id}
起售/停售套餐	POST	/admin/setmeal/status/{status}
根据分类ID查询套餐	GET	/admin/setmeal/list
订单	订单分页查询	GET	/admin/order/page
查询订单详情	GET	/admin/order/details/{id}
接单	PUT	/admin/order/confirm
拒单	PUT	/admin/order/rejection
取消订单	PUT	/admin/order/cancel
派送订单	PUT	/admin/order/delivery/{id}
完成订单	PUT	/admin/order/complete/{id}
数据统计	营业额统计	GET	/admin/statistics/turnover
用户统计	GET	/admin/statistics/user
订单统计	GET	/admin/statistics/order
销量TOP10	GET	/admin/statistics/top10
报表导出	GET	/admin/statistics/export
用户端接口
模块	接口	Method	路径
用户	微信登录	POST	/user/user/login
购物车	添加购物车	POST	/user/shoppingCart/add
删除购物车	POST	/user/shoppingCart/sub
查询购物车	GET	/user/shoppingCart/list
清空购物车	DELETE	/user/shoppingCart/clean
订单	下单	POST	/user/order/submit
订单支付	PUT	/user/order/payment
查询订单详情	GET	/user/order/details/{id}
查询历史订单	GET	/user/order/page
取消订单	PUT	/user/order/cancel/{id}
再来一单	POST	/user/order/repetition/{id}
菜品	条件查询菜品	GET	/user/dish/list
套餐	条件查询套餐	GET	/user/setmeal/list
地址	添加收货地址	POST	/user/address
查询地址列表	GET	/user/address/list
🧪 接口调试
推荐使用 Knife4j 进行在线调试：

启动项目后访问 http://localhost:8080/doc.html

在顶部「接口文档」->「全局参数」中添加 Token

输入管理端登录接口返回的 token 值，即可调试需要认证的接口

注意：管理端与用户端 Token 相互隔离，调用用户端接口时需使用用户端登录获取的 Token。

📦 部署建议
生产环境推荐配置
组件	推荐部署方式
应用服务	Docker 容器化 + 多实例集群
MySQL	主从复制 + 读写分离
Redis	哨兵模式或集群模式
OSS	阿里云 OSS 对象存储（已集成）
Nginx	反向代理 + 负载均衡 + 静态资源服务
Docker 部署示例（单机）
dockerfile
# Dockerfile
FROM openjdk:8-jre-alpine
COPY sky-server/target/sky-server-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
bash
# 构建并运行
docker build -t sky-take-out .
docker run -d -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/sky_take_out \
  -e SPRING_REDIS_HOST=host.docker.internal \
  --name sky-app sky-take-out
👤 作者
个人学习项目，独立完成后端核心接口开发。
通过本项目实践了企业级 Spring Boot 项目开发全流程，包括接口设计、缓存优化、定时任务、WebSocket 实时通信、第三方 API 集成等核心技术。

📄 开源协议
本项目仅供学习参考使用，请勿用于商业用途。

🙏 致谢
Spring Boot

MyBatis

Knife4j

阿里云 OSS

百度地图开放平台

注意：微信支付功能在实际生产环境中需要配置商户证书和回调接口，本项目中仅为模拟支付流程，实际开发需替换为真实支付接口。
