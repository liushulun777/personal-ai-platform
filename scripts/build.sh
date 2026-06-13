#!/bin/bash

# 构建所有服务

echo "=========================================="
echo "Personal AI Platform - 构建所有服务"
echo "=========================================="

# 检查 Maven 是否可用
if ! command -v mvn &> /dev/null; then
    echo "错误: Maven 未安装"
    exit 1
fi

# 构建后端服务
echo "构建后端服务..."
mvn clean package -DskipTests

# 检查构建结果
if [ $? -ne 0 ]; then
    echo "错误: 后端服务构建失败"
    exit 1
fi

echo "后端服务构建完成！"

# 构建前端
echo "构建前端..."
cd frontend/platform-web

# 检查 npm 是否可用
if ! command -v npm &> /dev/null; then
    echo "错误: npm 未安装"
    exit 1
fi

# 安装依赖
npm ci

# 构建生产版本
npm run build

# 检查构建结果
if [ $? -ne 0 ]; then
    echo "错误: 前端构建失败"
    exit 1
fi

cd ../..

echo ""
echo "=========================================="
echo "所有服务构建完成！"
echo "=========================================="
