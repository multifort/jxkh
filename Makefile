# 企业绩效考核系统 - 开发命令集

.PHONY: help backend frontend ci deploy clean test docs

# 默认目标
help:
	@echo "企业绩效考核系统- 开发命令"
	@echo ""
	@echo "## 后端命令"
	@echo "  make backend-depends          安装后端依赖"
	@echo "  make backend-run              运行后端服务"
	@echo "  make backend-test             运行后端测试"
	@echo "  make backend-clean            清理后端构建"
	@echo ""
	@echo "## 前端命令"
	@echo "  make frontend-depends         安装前端依赖"
	@echo "  make frontend-dev             启动前端开发服务器"
	@echo "  make frontend-build           构建前端应用"
	@echo "  make frontend-test            运行前端测试"
	@echo ""
	@echo "## 部署命令"
	@echo "  make local-up                 启动本地开发环境"
	@echo "  make local-down               停止本地开发环境"
	@echo "  make deploy                   部署到 K8s"
	@echo "  make rollback                 回滚到上一版本"
	@echo ""
	@echo "## 清理命令"
	@echo "  make clean                    清理构建文件"
	@echo "  make full-clean               清理所有文件"
	@echo ""
	@echo "## 文档命令"
	@echo "  make docs                     生成项目文档"
	@echo "  make api-docs                 生成 API 文档"
	@echo ""

# 后端命令
backend-depends:
	@echo ">>> 安装后端依赖..."
	cd backend && mvn dependency:resolve

backend-run:
	@echo ">>> 启动后端服务..."
	cd backend && mvn spring-boot:run

backend-test:
	@echo ">>> 运行后端测试..."
	cd backend && mvn test

backend-clean:
	@echo ">>> 清理后端构建..."
	cd backend && mvn clean

# 前端命令
frontend-depends:
	@echo ">>> 安装前端依赖..."
	cd frontend && npm install

frontend-dev:
	@echo ">>> 启动前端开发服务器..."
	cd frontend && npm run dev

frontend-build:
	@echo ">>> 构建前端应用..."
	cd frontend && npm run build

frontend-test:
	@echo ">>> 运行前端测试..."
	cd frontend && npm test

# 部署命令
local-up:
	@echo ">>> 启动本地开发环境..."
	cd doc/deployment && docker-compose up -d

local-down:
	@echo ">>> 停止本地开发环境..."
	cd doc/deployment && docker-compose down

deploy:
	@echo ">>> 部署到 Kubernetes..."
	cd doc/deployment/k8s-manifests && kubectl apply -f .

rollback:
	@echo ">>> 回滚到上一版本..."
	git revert HEAD~1
	git push origin main

# 清理命令
clean:
	@echo ">>> 清理构建文件..."
	rm -rf backend/target
	rm -rf backend/build
	rm -rf frontend/node_modules
	rm -rf frontend/dist

full-clean: clean
	@echo ">>> 清理所有文件..."
	rm -rf .claude/scheduled_tasks.json
	find . -type f -name "*.log" -delete
	find . -type d -name "node_modules" -exec rm -rf {} +

# 文档命令
docs:
	@echo ">>> 生成项目文档..."
	@echo "文档已生成在 doc/"
	@ls -la doc/

api-docs:
	@echo ">>> 生成 API 文档..."
	@echo "API 文档在 doc/api/openapi.yaml"
	@echo "在线文档：http://localhost:8080/api-docs"
