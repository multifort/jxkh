# 部署文档

## 📋 概述

本目录包含部署相关的配置文件和指南。

## 📂 文件说明

### docker-compose.yml

本地开发环境的 Docker Compose 配置，包含：

- backend 服务（Spring Boot）
- frontend 服务（React + Vite）
- MySQL 8.0 数据库
- Redis 7.x 缓存

### k8s-manifests/

Kubernetes 部署配置：

| 文件 | 说明 |
|------|------|
| [backend-deployment.yaml](./k8s-manifests/backend-deployment.yaml) | 后端 Deployment 配置 |
| [frontend-deployment.yaml](./k8s-manifests/frontend-deployment.yaml) | 前端 Deployment 配置 |
| [service.yaml](./k8s-manifests/service.yaml) | Service 配置 |
| [ingress.yaml](./k8s-manifests/ingress.yaml) | Ingress 配置 |
| [configmap.yaml](./k8s-manifests/configmap.yaml) | ConfigMap 配置 |

## 🚀 本地开发环境

### 启动服务

```bash
cd doc/deployment
docker-compose up -d
```

### 查看日志

```bash
docker-compose logs -f backend
docker-compose logs -f frontend
```

### 停止服务

```bash
docker-compose down
```

## ☸️ Kubernetes 部署

### 前置条件

- Kubernetes 集群 >= 1.20
- Helm 3.x (可选)

### 部署步骤

1. **创建命名空间**

```bash
kubectl create namespace enterprise-guardian
```

2. **创建 ConfigMap**

```bash
kubectl apply -f k8s-manifests/configmap.yaml -n enterprise-guardian
```

3. **部署后端**

```bash
kubectl apply -f k8s-manifests/backend-deployment.yaml -n enterprise-guardian
```

4. **部署前端**

```bash
kubectl apply -f k8s-manifests/frontend-deployment.yaml -n enterprise-guardian
```

5. **部署服务**

```bash
kubectl apply -f k8s-manifests/service.yaml -n enterprise-guardian
```

6. **配置 Ingress**

```bash
kubectl apply -f k8s-manifests/ingress.yaml -n enterprise-guardian
```

### 验证部署

```bash
kubectl get pods -n enterprise-guardian
kubectl get svc -n enterprise-guardian
```

## 🔧 环境变量

### 后端环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SPRING_PROFILES_ACTIVE | 环境配置 | dev |
| SPRING_DATASOURCE_URL | 数据库 URL | - |
| SPRING_DATASOURCE_USERNAME | 数据库用户名 | - |
| SPRING_DATASOURCE_PASSWORD | 数据库密码 | - |
| REDIS_HOST | Redis 主机 | localhost |
| REDIS_PORT | Redis 端口 | 6379 |
| MQ_HOST | MQ 主机 | localhost |
| MQ_PORT | MQ 端口 | 5672 |

### 前端环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| VITE_API_BASE_URL | API 基础地址 | http://localhost:8080/api |
| VITE_API_VERSION | API 版本 | v1 |

## 📊 监控配置

### Prometheus

- 启用 Micrometer 集成
- 暴露 metrics 端点
- 配置 Grafana 仪表盘

### 健康检查

```bash
# 后端健康检查
kubectl exec -n enterprise-guardian <pod-name> -- curl http://localhost:8080/actuator/health

# 前端健康检查
kubectl exec -n enterprise-guardian <pod-name> -- curl http://localhost:3000/health
```

## 🔍 故障排查

### 查看日志

```bash
# 后端日志
kubectl logs -n enterprise-guardian <pod-name> -f

# 前端日志
kubectl logs -n enterprise-guardian <pod-name> -f
```

### 进入容器

```bash
kubectl exec -it -n enterprise-guardian <pod-name> -- bash
```

---

[回到文档根目录](../README.md)
