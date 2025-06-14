# 新加坡Pc28数据开奖数据采集

## 项目概述

本项目是一个基于Spring Boot的Web应用，用于获取新加坡Pc28数据开奖数据。项目结构设计遵循良好的解耦性和颗粒化原则，便于维护和扩展。

## 技术栈

- Java 24
- Spring Boot 3.4.5
- Apache HttpComponents
- Springdoc-OpenAPI 2.5.0
- Lombok
- SLF4J & Logback

## 项目结构

项目采用多层架构设计，各组件职责明确，解耦性好：

```
src/main/java/org/pc28/
├── constants/          # 常量类
│   └── WebConstants.java
├── exception/          # 异常处理类
│   └── WebDownloadException.java
├── utils/              # 工具类
│   ├── HttpUtils.java
│   └── FileUtils.java
├── model/              # 模型类
│   └── WebDownloadResult.java
├── service/            # 服务层接口
│   ├── WebDownloadService.java
│   └── impl/
│       └── WebDownloadServiceImpl.java
├── config/             # 配置类
│   ├── HttpClientConfig.java
│   └── OpenApiConfig.java
├── controller/         # 控制器
│   └── WebDownloadController.java
└── Pc28Application.java # 程序主入口
```

## 功能特点

- 获取开奖载网页内容
- 自动保存到本地文件
- 提供RESTful API接口
- 详细的API文档（Swagger UI）
- 完善的异常处理
- 良好的日志记录

## 快速开始

### 环境要求

- JDK 24+
- Maven 3.6+

### 构建与运行

1. 克隆项目

```bash
git clone https://github.com/username/redtaotao-downloader.git
cd redtaotao-downloader
```

2. 构建项目

```bash
mvn clean package
```

3. 运行项目

```bash
java -jar target/pc28-0.0.1-SNAPSHOT.jar
```

或者使用Maven运行：

```bash
mvn spring-boot:run
```

4. 访问API

- API接口：http://localhost:8080/api/download/webpage
- API文档：http://localhost:8080/swagger-ui.html
- 自定义API文档：http://localhost:8080/custom-swagger.html

## API文档

详细的API文档可以通过以下方式访问：

1. Swagger UI：运行应用后访问 http://localhost:8080/swagger-ui.html
2. 自定义API文档：运行应用后访问 http://localhost:8080/custom-swagger.html
3. 项目中的Markdown文档：[API-Documentation.md](API-Documentation.md)

## 配置说明

项目配置通过 `application.properties`文件管理，主要配置项：

```properties
# 服务器端口
server.port=8080

# API文档路径
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

# 日志级别
logging.level.org.pc28=INFO

# HTTP客户端配置
httpclient.max-total-connections=100
httpclient.max-per-route=10
```

## 常量配置

业务相关的常量配置在 `WebConstants`类中：

- `TARGET_URL`: 目标网页URL
- `SAVE_PATH`: 保存目录
- `FILE_NAME`: 文件名
- `CONNECTION_TIMEOUT`: 连接超时时间
- `READ_TIMEOUT`: 读取超时时间
- `USER_AGENT`: 请求头信息

## 贡献指南

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送分支 (`git push origin feature/amazing-feature`)
5. 创建Pull Request

## 许可证

本项目采用Apache 2.0许可证 - 查看[LICENSE](LICENSE)文件了解详情

