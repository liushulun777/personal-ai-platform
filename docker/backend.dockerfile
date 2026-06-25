# 后端服务通用 Dockerfile
# 使用多阶段构建优化镜像大小

# 阶段1：构建
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# 先复制 pom.xml 利用缓存
COPY pom.xml ./
COPY common/pom.xml ./common/
COPY services/pom.xml ./services/

# 下载依赖
RUN mvn dependency:go-offline -B

# 复制源码
COPY common ./common
COPY services ./services

# 构建指定服务
ARG SERVICE_NAME
RUN mvn clean package -pl services/${SERVICE_NAME} -am -DskipTests && \
    mv services/${SERVICE_NAME}/target/*.jar app.jar

# 阶段2：运行
FROM eclipse-temurin:21-jre-alpine

# 安装必要工具
RUN apk add --no-cache tzdata curl

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建非 root 用户
RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

# 从构建阶段复制 jar
COPY --from=builder /build/app.jar app.jar

# 修改文件权限
RUN chown -R app:app /app

# 切换到非 root 用户
USER app

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# 启动命令
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
