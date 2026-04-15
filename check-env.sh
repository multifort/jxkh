#!/bin/bash

# 企业绩效考核系统 - 环境检查脚本
# 用途：验证本地开发环境是否满足要求

echo "======================================"
echo "  企业绩效考核系统 - 环境检查"
echo "======================================"
echo ""

ERRORS=0
WARNINGS=0

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查函数
check_command() {
    local cmd=$1
    local name=$2
    local required_version=$3
    
    if command -v $cmd &> /dev/null; then
        version=$($cmd --version 2>&1 | head -n 1)
        echo -e "${GREEN}✓${NC} $name 已安装: $version"
        return 0
    else
        echo -e "${RED}✗${NC} $name 未安装 (需要: $required_version)"
        ERRORS=$((ERRORS + 1))
        return 1
    fi
}

check_version() {
    local cmd=$1
    local name=$2
    local min_version=$3
    local current_version=$4
    
    # 简单的版本比较（仅适用于主要版本号）
    if [[ "$current_version" =~ ^([0-9]+) ]]; then
        major=${BASH_REMATCH[1]}
        if [[ "$min_version" =~ ^([0-9]+) ]]; then
            min_major=${BASH_REMATCH[1]}
            if [ $major -ge $min_major ]; then
                echo -e "${GREEN}✓${NC} $name 版本符合要求 (当前: $current_version, 最低: $min_version)"
                return 0
            else
                echo -e "${RED}✗${NC} $name 版本过低 (当前: $current_version, 需要: ≥$min_version)"
                ERRORS=$((ERRORS + 1))
                return 1
            fi
        fi
    fi
    echo -e "${YELLOW}⚠${NC} $name 版本无法自动验证 (当前: $current_version, 需要: ≥$min_version)"
    WARNINGS=$((WARNINGS + 1))
    return 0
}

echo "1. Java 环境检查"
echo "--------------------------------------"
if check_command "java" "Java" "JDK 21"; then
    java_version=$(java -version 2>&1 | grep -oP 'version "\K[0-9]+' | head -1)
    check_version "java" "JDK" "21" "$java_version"
fi
echo ""

echo "2. Maven 环境检查"
echo "--------------------------------------"
if check_command "mvn" "Maven" "3.9.x"; then
    mvn_version=$(mvn --version 2>&1 | grep -oP 'Apache Maven \K[0-9.]+' | head -1)
    check_version "mvn" "Maven" "3.9" "$mvn_version"
fi
echo ""

echo "3. Node.js 环境检查"
echo "--------------------------------------"
if check_command "node" "Node.js" "18+"; then
    node_version=$(node -v | grep -oP '\K[0-9]+' | head -1)
    check_version "node" "Node.js" "18" "$node_version"
fi
echo ""

echo "4. npm 环境检查"
echo "--------------------------------------"
check_command "npm" "npm" "最新稳定版"
echo ""

echo "5. Docker 环境检查"
echo "--------------------------------------"
if check_command "docker" "Docker" "24.x+"; then
    docker_version=$(docker --version | grep -oP '\K[0-9.]+' | head -1)
    check_version "docker" "Docker" "20" "$docker_version"
fi
echo ""

echo "6. Docker Compose 环境检查"
echo "--------------------------------------"
if check_command "docker-compose" "Docker Compose" "2.x"; then
    compose_version=$(docker-compose --version | grep -oP '\K[0-9.]+' | head -1)
    check_version "docker-compose" "Docker Compose" "2" "$compose_version"
elif check_command "docker" "Docker Compose (plugin)" "2.x"; then
    compose_version=$(docker compose version 2>&1 | grep -oP '\K[0-9.]+' | head -1)
    if [ -n "$compose_version" ]; then
        echo -e "${GREEN}✓${NC} Docker Compose (plugin) 已安装: v$compose_version"
    fi
fi
echo ""

echo "7. MySQL 客户端检查（可选）"
echo "--------------------------------------"
if command -v mysql &> /dev/null; then
    mysql_version=$(mysql --version | grep -oP '\K[0-9.]+' | head -1)
    echo -e "${GREEN}✓${NC} MySQL 客户端已安装: v$mysql_version"
else
    echo -e "${YELLOW}⚠${NC} MySQL 客户端未安装（可通过 Docker 使用）"
    WARNINGS=$((WARNINGS + 1))
fi
echo ""

echo "8. Git 环境检查"
echo "--------------------------------------"
check_command "git" "Git" "任意版本"
echo ""

echo "======================================"
echo "  检查结果汇总"
echo "======================================"
echo ""

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✓ 所有环境检查通过！可以开始开发了。${NC}"
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠ 发现 $WARNINGS 个警告，但不影响开发。${NC}"
else
    echo -e "${RED}✗ 发现 $ERRORS 个错误，请先解决后再开始开发。${NC}"
    echo ""
    echo "建议操作："
    echo "  1. 安装缺失的软件"
    echo "  2. 升级版本过低的软件"
    echo "  3. 配置环境变量"
fi

echo ""
echo "======================================"
echo "  项目配置检查"
echo "======================================"
echo ""

# 检查 .env 文件
if [ -f ".env" ]; then
    echo -e "${GREEN}✓${NC} .env 文件存在"
else
    echo -e "${RED}✗${NC} .env 文件不存在，请从 .env.example 复制并配置"
    ERRORS=$((ERRORS + 1))
fi

# 检查后端 pom.xml
if [ -f "backend/pom.xml" ]; then
    spring_version=$(grep -oP '<version>\K[0-9.]+' backend/pom.xml | head -1)
    if [[ "$spring_version" == 3.5* ]]; then
        echo -e "${GREEN}✓${NC} Spring Boot 版本: $spring_version"
    else
        echo -e "${RED}✗${NC} Spring Boot 版本不正确: $spring_version (需要 3.5.x)"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo -e "${RED}✗${NC} backend/pom.xml 不存在"
    ERRORS=$((ERRORS + 1))
fi

# 检查前端 package.json
if [ -f "frontend/package.json" ]; then
    if grep -q '"antd"' frontend/package.json; then
        echo -e "${GREEN}✓${NC} Ant Design 依赖已配置"
    else
        echo -e "${RED}✗${NC} Ant Design 依赖缺失"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo -e "${RED}✗${NC} frontend/package.json 不存在"
    ERRORS=$((ERRORS + 1))
fi

echo ""
echo "======================================"

exit $ERRORS
