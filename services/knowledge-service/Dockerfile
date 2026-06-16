# 基础镜像
FROM eclipse-temurin:21-jre-alpine

# 设置工作目录
WORKDIR /app

# 复制 jar 包
COPY target/*.jar app.jar

# 暴露端口
EXPOSE 1087

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
