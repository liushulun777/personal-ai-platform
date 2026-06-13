#!/bin/bash

# Kubernetes 部署脚本

echo "=========================================="
echo "Personal AI Platform - Kubernetes 部署"
echo "=========================================="

# 检查 kubectl 是否可用
if ! command -v kubectl &> /dev/null; then
    echo "错误: kubectl 未安装"
    exit 1
fi

# 检查 kustomize 是否可用
if ! command -v kustomize &> /dev/null; then
    echo "警告: kustomize 未安装，将使用 kubectl apply -k"
fi

# 创建命名空间
echo "创建命名空间..."
kubectl apply -f k8s/base/namespace.yaml

# 创建 ConfigMap 和 Secret
echo "创建 ConfigMap 和 Secret..."
kubectl apply -f k8s/base/configmap.yaml
kubectl apply -f k8s/base/secret.yaml

# 创建 PostgreSQL init SQL ConfigMap
echo "创建 PostgreSQL init SQL ConfigMap..."
kubectl create configmap postgres-init-sql \
    --from-file=init.sql=sql/init.sql \
    -n platform \
    --dry-run=client -o yaml | kubectl apply -f -

# 部署基础设施
echo "部署基础设施..."
kubectl apply -f k8s/base/postgres.yaml
kubectl apply -f k8s/base/redis.yaml
kubectl apply -f k8s/base/elasticsearch.yaml
kubectl apply -f k8s/base/kafka.yaml
kubectl apply -f k8s/base/minio.yaml
kubectl apply -f k8s/base/nacos.yaml

# 等待基础设施就绪
echo "等待基础设施就绪..."
sleep 30

# 部署微服务
echo "部署微服务..."
kubectl apply -f k8s/base/gateway-service.yaml
kubectl apply -f k8s/base/auth-service.yaml
kubectl apply -f k8s/base/blog-service.yaml
kubectl apply -f k8s/base/search-service.yaml
kubectl apply -f k8s/base/ai-service.yaml

# 等待微服务就绪
echo "等待微服务就绪..."
sleep 30

# 检查部署状态
echo "检查部署状态..."
kubectl get pods -n platform
kubectl get services -n platform

echo ""
echo "=========================================="
echo "部署完成！"
echo ""
echo "查看状态："
echo "  kubectl get pods -n platform"
echo "  kubectl get services -n platform"
echo ""
echo "查看日志："
echo "  kubectl logs -f <pod-name> -n platform"
echo ""
echo "访问服务："
echo "  kubectl port-forward svc/gateway-service 8080:8080 -n platform"
echo "=========================================="
