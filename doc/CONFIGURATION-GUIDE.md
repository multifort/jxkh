# 应用配置说明

本文档详细说明 `application.yml` 中所有可配置的环境变量及其默认值。

---

## 📋 快速开始

### 方式一：使用 .env 文件（推荐）

在项目根目录创建 `.env` 文件（已提供 `.env.example` 模板）：

```bash
# 复制示例文件
cp .env.example .env

# 编辑 .env 文件，修改为你实际的配置
vim .env
```

### 方式二：直接设置环境变量

```bash
# Linux/Mac
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=your_password

# Windows
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
```

### 方式三：启动时指定

```bash
java -jar jxkh-backend.jar \
  --MYSQL_HOST=localhost \
  --MYSQL_PORT=3306 \
  --MYSQL_USERNAME=root \
  --MYSQL_PASSWORD=your_password
```

---

## 🔧 配置项详解

### 1. 数据库配置（MySQL）

| 环境变量 | 默认值 | 说明 | 示例 |
|---------|--------|------|------|
| `MYSQL_HOST` | `localhost` | MySQL 主机地址 | `127.0.0.1` |
| `MYSQL_PORT` | `3306` | MySQL 端口 | `3306` |
| `MYSQL_DATABASE` | `jxkh_db` | 数据库名称 | `jxkh_db` |
| `MYSQL_USERNAME` | `root` | 数据库用户名 | `jxkh` |
| `MYSQL_PASSWORD` | `root` | 数据库密码 | `jxkh123456` |

**连接池配置（HikariCP）**：

| 环境变量 | 默认值 | 说明 | 建议值 |
|---------|--------|------|--------|
| `DB_POOL_MIN_IDLE` | `5` | 最小空闲连接数 | 开发: 5, 生产: 10 |
| `DB_POOL_MAX_SIZE` | `20` | 最大连接池大小 | 开发: 20, 生产: 50 |
| `DB_POOL_CONNECTION_TIMEOUT` | `30000` | 连接超时时间（毫秒） | 30000 |
| `DB_POOL_IDLE_TIMEOUT` | `600000` | 空闲连接超时（10分钟） | 600000 |
| `DB_POOL_MAX_LIFETIME` | `1800000` | 连接最大生命周期（30分钟） | 1800000 |
| `DB_POOL_LEAK_DETECTION` | `60000` | 连接泄漏检测阈值（60秒） | 60000 |

**JPA 配置**：

| 环境变量 | 默认值 | 说明 | 建议值 |
|---------|--------|------|--------|
| `JPA_DDL_AUTO` | `validate` | Hibernate DDL 策略 | 开发: update, 生产: validate |
| `JPA_SHOW_SQL` | `false` | 是否显示 SQL | 开发: true, 生产: false |
| `HIBERNATE_FORMAT_SQL` | `true` | 是否格式化 SQL | 开发: true, 生产: false |

---

### 2. Redis 配置

| 环境变量 | 默认值 | 说明 | 示例 |
|---------|--------|------|------|
| `REDIS_HOST` | `localhost` | Redis 主机地址 | `127.0.0.1` |
| `REDIS_PORT` | `6379` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | `` | Redis 密码（空表示无密码） | `redis123` |
| `REDIS_DATABASE` | `0` | Redis 数据库索引 | `0-15` |
| `REDIS_TIMEOUT` | `5000ms` | 连接超时时间 | `5000ms` |

**Redis 连接池配置（Lettuce）**：

| 环境变量 | 默认值 | 说明 | 建议值 |
|---------|--------|------|--------|
| `REDIS_POOL_MAX_ACTIVE` | `20` | 最大活跃连接数 | 开发: 20, 生产: 50 |
| `REDIS_POOL_MAX_IDLE` | `10` | 最大空闲连接数 | 10 |
| `REDIS_POOL_MIN_IDLE` | `5` | 最小空闲连接数 | 5 |
| `REDIS_POOL_MAX_WAIT` | `3000ms` | 获取连接最大等待时间 | 3000ms |
| `REDIS_SHUTDOWN_TIMEOUT` | `100ms` | 关闭超时时间 | 100ms |

---

### 3. 服务器配置

| 环境变量 | 默认值 | 说明 | 示例 |
|---------|--------|------|------|
| `SERVER_PORT` | `8080` | 应用端口 | `8080` |
| `SPRING_PROFILES_ACTIVE` | `dev` | 激活的配置文件 | `dev/test/prod` |

---

### 4. 日志配置

| 环境变量 | 默认值 | 说明 | 建议值 |
|---------|--------|------|--------|
| `LOG_LEVEL_ROOT` | `INFO` | 根日志级别 | INFO |
| `LOG_LEVEL_APP` | `DEBUG` | 应用日志级别 | 开发: DEBUG, 生产: INFO |
| `LOG_LEVEL_WEB` | `INFO` | Spring Web 日志级别 | INFO |
| `LOG_LEVEL_SQL` | `DEBUG` | Hibernate SQL 日志级别 | 开发: DEBUG, 生产: WARN |
| `LOG_LEVEL_SQL_BINDER` | `TRACE` | SQL 参数绑定日志 | 开发: TRACE, 生产: OFF |
| `LOG_FILE_PATH` | `logs` | 日志文件路径 | `logs` |
| `LOG_FILE_MAX_SIZE` | `10MB` | 单个日志文件最大大小 | 10MB |
| `LOG_FILE_MAX_HISTORY` | `30` | 日志文件保留天数 | 30 |

---

### 5. JWT 配置

| 环境变量 | 默认值 | 说明 | ⚠️ 注意 |
|---------|--------|------|---------|
| `JWT_SECRET` | `jxkh-secret-key...` | JWT 签名密钥 | **生产环境必须修改！** |
| `JWT_EXPIRATION` | `86400000` | Access Token 过期时间（毫秒） | 24小时 |
| `JWT_REFRESH_EXPIRATION` | `604800000` | Refresh Token 过期时间（毫秒） | 7天 |

**⚠️ 安全警告**：
- 生产环境必须使用强随机密钥（至少 256 位）
- 生成命令：`openssl rand -base64 32`

---

### 6. AI 服务配置（可选）

| 环境变量 | 默认值 | 说明 | 示例 |
|---------|--------|------|------|
| `AI_API_KEY` | `` | OpenAI API Key | `sk-xxx` |
| `AI_MODEL` | `gpt-4` | AI 模型 | `gpt-4`, `gpt-3.5-turbo` |
| `AI_BASE_URL` | `https://api.openai.com/v1` | API 基础 URL | 国内代理地址 |
| `AI_TIMEOUT` | `30000` | 请求超时时间（毫秒） | 30000 |
| `AI_ENABLED` | `false` | 是否启用 AI 功能 | `true/false` |

---

## 📊 不同环境的配置示例

### 开发环境（.env.dev）

```bash
# 数据库
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=jxkh_db
MYSQL_USERNAME=root
MYSQL_PASSWORD=root

# 连接池（较小）
DB_POOL_MIN_IDLE=2
DB_POOL_MAX_SIZE=10

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JPA（便于调试）
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
HIBERNATE_FORMAT_SQL=true

# 日志（详细）
LOG_LEVEL_APP=DEBUG
LOG_LEVEL_SQL=DEBUG
LOG_LEVEL_SQL_BINDER=TRACE

# JWT（开发密钥）
JWT_SECRET=dev-secret-key-not-for-production

# AI（可选）
AI_ENABLED=false
```

---

### 测试环境（.env.test）

```bash
# 数据库
MYSQL_HOST=test-db.example.com
MYSQL_PORT=3306
MYSQL_DATABASE=jxkh_test
MYSQL_USERNAME=jxkh_test
MYSQL_PASSWORD=test_password_123

# 连接池（中等）
DB_POOL_MIN_IDLE=5
DB_POOL_MAX_SIZE=20

# Redis
REDIS_HOST=test-redis.example.com
REDIS_PORT=6379
REDIS_PASSWORD=redis_test_pass

# JPA
JPA_DDL_AUTO=validate
JPA_SHOW_SQL=false

# 日志
LOG_LEVEL_APP=INFO
LOG_LEVEL_SQL=WARN

# JWT
JWT_SECRET=test-secret-key-change-in-production
```

---

### 生产环境（.env.prod）

```bash
# 数据库
MYSQL_HOST=prod-db.example.com
MYSQL_PORT=3306
MYSQL_DATABASE=jxkh_prod
MYSQL_USERNAME=jxkh_prod
MYSQL_PASSWORD=<STRONG_RANDOM_PASSWORD>

# 连接池（较大）
DB_POOL_MIN_IDLE=10
DB_POOL_MAX_SIZE=50
DB_POOL_CONNECTION_TIMEOUT=30000
DB_POOL_LEAK_DETECTION=60000

# Redis
REDIS_HOST=prod-redis.example.com
REDIS_PORT=6379
REDIS_PASSWORD=<STRONG_RANDOM_PASSWORD>
REDIS_DATABASE=0

# Redis 连接池
REDIS_POOL_MAX_ACTIVE=50
REDIS_POOL_MAX_IDLE=20
REDIS_POOL_MIN_IDLE=10

# JPA（严格验证）
JPA_DDL_AUTO=validate
JPA_SHOW_SQL=false
HIBERNATE_FORMAT_SQL=false

# 日志（精简）
LOG_LEVEL_ROOT=WARN
LOG_LEVEL_APP=INFO
LOG_LEVEL_SQL=WARN
LOG_FILE_PATH=/var/log/jxkh
LOG_FILE_MAX_SIZE=50MB
LOG_FILE_MAX_HISTORY=90

# JWT（强密钥）
JWT_SECRET=<GENERATED_BY_OPENSSL_RAND_BASE64_32>
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# 服务器
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# AI（生产启用）
AI_ENABLED=true
AI_API_KEY=<YOUR_OPENAI_API_KEY>
AI_MODEL=gpt-4
AI_BASE_URL=https://api.openai.com/v1
```

---

## 🔍 配置验证

### 检查配置是否生效

启动应用后，访问 Actuator 端点查看配置：

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 环境信息
curl http://localhost:8080/actuator/env | jq '.propertySources[] | select(.name == "systemEnvironment")'

# 数据源信息
curl http://localhost:8080/actuator/metrics/hikaricp.connections
```

### 常见问题排查

#### 1. 数据库连接失败

```bash
# 检查环境变量是否设置
echo $MYSQL_HOST
echo $MYSQL_USERNAME
echo $MYSQL_PASSWORD

# 测试数据库连接
mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USERNAME -p$MYSQL_PASSWORD

# 查看应用日志
tail -f logs/application.log | grep "DataSource"
```

#### 2. Redis 连接失败

```bash
# 测试 Redis 连接
redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD ping

# 查看应用日志
tail -f logs/application.log | grep "Redis"
```

#### 3. 连接池耗尽

```bash
# 监控连接池使用情况
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
curl http://localhost:8080/actuator/metrics/hikaricp.connections.idle

# 如果 active 接近 max-size，需要增大连接池
export DB_POOL_MAX_SIZE=50
```

---

## 📝 最佳实践

### 1. 敏感信息管理

✅ **推荐做法**：
```bash
# 使用 .env 文件（不要提交到 Git）
MYSQL_PASSWORD=your_password

# 或使用密钥管理服务（KMS）
aws secretsmanager get-secret-value --secret-id jxkh/db-password
```

❌ **禁止做法**：
```yaml
# 不要在代码中硬编码密码
password: Zhikeyunxin123$  # ❌
```

### 2. 环境变量优先级

Spring Boot 加载配置的优先级（从高到低）：
1. 命令行参数
2. JVM 系统属性
3. 操作系统环境变量
4. `.env` 文件（通过 Spring Cloud Bootstrap）
5. `application-{profile}.yml`
6. `application.yml`

### 3. 生产环境检查清单

- [ ] JWT_SECRET 使用强随机密钥
- [ ] 数据库密码使用强密码
- [ ] Redis 密码不为空
- [ ] JPA_DDL_AUTO=validate（禁止自动建表）
- [ ] JPA_SHOW_SQL=false（避免性能影响）
- [ ] 日志级别调整为 INFO 或 WARN
- [ ] 连接池大小根据实际负载调整
- [ ] 启用 HTTPS
- [ ] 配置监控告警

---

## 🔄 配置变更流程

### 修改配置步骤

1. **修改 .env 文件**
   ```bash
   vim .env
   ```

2. **重启应用**
   ```bash
   # 停止应用
   pkill -f jxkh-backend
   
   # 重新启动
   cd backend
   mvn spring-boot:run
   ```

3. **验证配置生效**
   ```bash
   curl http://localhost:8080/actuator/env | jq '.propertySources[] | select(.name == "systemEnvironment")'
   ```

### 动态刷新配置（无需重启）

对于部分配置，可以使用 Spring Cloud Config + Actuator 实现动态刷新：

```bash
# 暴露 refresh 端点
curl -X POST http://localhost:8080/actuator/refresh
```

---

## 📚 相关文档

- [Spring Boot 外部化配置](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [HikariCP 配置说明](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [Lettuce Redis 客户端](https://lettuce.io/core/release/reference/)
- [Actuator 监控端点](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

---

**文档版本**: V1.0  
**最后更新**: 2026-04-15  
**维护者**: JXKH Team
