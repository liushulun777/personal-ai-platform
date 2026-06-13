#!/bin/bash

# 开发环境启动脚本

echo "=========================================="
echo "Personal AI Platform - 开发环境启动"
echo "=========================================="

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "错误: Docker 未运行，请先启动 Docker"
    exit 1
fi

# 启动基础设施
echo "启动基础设施..."
docker-compose -f docker-compose.dev.yml up -d

# 等待服务启动
echo "等待服务启动..."
sleep 10

# 检查服务状态
echo "检查服务状态..."
docker-compose -f docker-compose.dev.yml ps

echo ""
echo "=========================================="
echo "基础设施启动完成！"
echo ""
echo "服务地址："
echo "  PostgreSQL: localhost:5432"
echo "  Redis: localhost:16379"
echo "  MinIO: http://localhost:19001"
echo "  Nacos: http://localhost:8848"
echo "  ElasticSearch: http://localhost:9200"
echo "  Kafka: localhost:9092"
echo ""
echo "默认账号："
echo "  PostgreSQL: postgres/postgres"
echo "  MinIO: minioadmin/minioadmin"
echo "  Nacos: nacos/nacos"
echo "=========================================="
