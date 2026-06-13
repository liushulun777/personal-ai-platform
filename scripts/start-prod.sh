#!/bin/bash

# 生产环境启动脚本

echo "=========================================="
echo "Personal AI Platform - 生产环境启动"
echo "=========================================="

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "错误: Docker 未运行，请先启动 Docker"
    exit 1
fi

# 检查环境变量文件
if [ ! -f .env ]; then
    echo "警告: .env 文件不存在，正在从示例文件创建..."
    cp .env.example .env
    echo "请编辑 .env 文件配置正确的环境变量"
fi

# 构建所有服务
echo "构建所有服务..."
./scripts/build.sh

# 检查构建结果
if [ $? -ne 0 ]; then
    echo "错误: 服务构建失败"
    exit 1
fi

# 启动所有服务
echo "启动所有服务..."
docker-compose up -d

# 等待服务启动
echo "等待服务启动..."
sleep 30

# 检查服务状态
echo "检查服务状态..."
docker-compose ps

echo ""
echo "=========================================="
echo "所有服务启动完成！"
echo ""
echo "服务地址："
echo "  前端: http://localhost"
echo "  网关: http://localhost:8080"
echo "  Nacos: http://localhost:8848"
echo "  MinIO: http://localhost:19001"
echo ""
echo "微服务端口："
echo "  Auth Service: 1081"
echo "  System Service: 1082"
echo "  Blog Service: 1083"
echo "  File Service: 1084"
echo "  Search Service: 1085"
echo "  AI Service: 1086"
echo "=========================================="
