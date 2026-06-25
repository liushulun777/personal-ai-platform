# 前端 Dockerfile
# 使用多阶段构建优化镜像大小

# 阶段1：构建
FROM node:20-alpine AS builder

WORKDIR /build

# 复制 package.json 利用缓存
COPY package.json package-lock.json ./

# 安装依赖
RUN npm ci --production=false

# 复制源码
COPY . .

# 构建
RUN npm run build

# 阶段2：运行
FROM nginx:alpine

# 安装必要工具
RUN apk add --no-cache curl

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:80 || exit 1

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
