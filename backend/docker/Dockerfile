# ============================================================
# 绩效考核系统后端 - Docker 构建文件
# ============================================================

# 构建阶段
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# 复制 Maven 配置
COPY pom.xml .

# 下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests -B

# ============================================================
# 运行阶段
# ============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 安装 curl 用于健康检查
RUN apk add --no-cache curl

# 从构建阶段复制 JAR 包
COPY --from=builder /build/target/*.jar app.jar

# 创建非 root 用户（安全最佳实践）
RUN addgroup -g 1000 -S appgroup && \
    adduser -u 1000 -S appuser -G appgroup

# 设置文件权限
RUN chown -R appuser:appgroup /app
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl --fail http://localhost:8080/actuator/health || exit 1

# JVM 参数优化
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动应用
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
