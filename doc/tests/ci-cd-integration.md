# CI/CD 集成配置

## 📋 概述

本文档定义测试在 CI/CD 流水线中的集成配置，包括自动化测试执行、覆盖率报告和质最门禁。

---

## 🚀 GitHub Actions 配置

### 1. 完整测试流水线

**.github/workflows/test.yml**：

```yaml
name: Test Suite

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  # 后端测试
  backend-test:
    name: Backend Tests
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: testdb
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3
      
      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: >-
          --health-cmd="redis-cli ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      
      - name: Run Unit Tests
        run: cd backend && mvn test -Dtest='!*IntegrationTest'
      
      - name: Run Integration Tests
        run: cd backend && mvn test -Dtest='*IntegrationTest'
        env:
          SPRING_DATASOURCE_URL: jdbc:mysql://localhost:3306/testdb
          SPRING_REDIS_HOST: localhost
      
      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./backend/target/site/jacoco/jacoco.xml
          flags: backend
          name: backend-coverage
      
      - name: SonarQube Scan
        if: github.event_name == 'push'
        uses: SonarSource/sonarqube-scan-action@v3
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        with:
          projectBaseDir: ./backend

  # 前端测试
  frontend-test:
    name: Frontend Tests
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      
      - name: Install Dependencies
        run: cd frontend && npm ci
      
      - name: Run Unit Tests
        run: cd frontend && npm test -- --run
      
      - name: Run E2E Tests
        run: cd frontend && npx playwright install && npx playwright test
      
      - name: Upload Coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./frontend/coverage/lcov.info
          flags: frontend
          name: frontend-coverage

  # 代码质量检查
  code-quality:
    name: Code Quality
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Run ESLint
        run: cd frontend && npm run lint
      
      - name: Run Prettier Check
        run: cd frontend && npx prettier --check .
      
      - name: Run Spotless Check
        run: cd backend && mvn spotless:check

  # 构建验证
  build:
    name: Build Validation
    runs-on: ubuntu-latest
    needs: [backend-test, frontend-test, code-quality]
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Build Backend
        run: cd backend && mvn clean package -DskipTests
      
      - name: Build Frontend
        run: cd frontend && npm run build
      
      - name: Upload Build Artifacts
        uses: actions/upload-artifact@v4
        with:
          name: build-artifacts
          path: |
            backend/target/*.jar
            frontend/dist/
```

---

### 2. 每日夜间回归测试

**.github/workflows/nightly-regression.yml**：

```yaml
name: Nightly Regression Tests

on:
  schedule:
    - cron: '0 2 * * *'  # 每天凌晨 2 点执行
  workflow_dispatch:  # 支持手动触发

jobs:
  full-regression:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Run Full Test Suite
        run: |
          cd backend && mvn clean test
          cd ../frontend && npm test -- --run
      
      - name: Run Performance Tests
        run: cd backend && mvn test -Pbenchmark
      
      - name: Generate Allure Report
        uses: simple-elf/allure-report-action@master
        if: always()
        with:
          allure_results: backend/target/allure-results
      
      - name: Notify on Failure
        if: failure()
        uses: actions/github-script@v7
        with:
          script: |
            github.rest.issues.create({
              owner: context.repo.owner,
              repo: context.repo.repo,
              title: `🚨 夜间回归测试失败 - ${new Date().toLocaleDateString()}`,
              body: '请查看工作流详情：' + context.serverUrl + '/' + context.repo.owner + '/' + context.repo.repo + '/actions/runs/' + context.runId,
              labels: ['bug', 'test-failure']
            })
```

---

## 🔧 Maven 配置

### pom.xml - 测试相关配置

```xml
<build>
    <plugins>
        <!-- Surefire Plugin - 单元测试 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.5</version>
            <configuration>
                <parallel>methods</parallel>
                <threadCount>4</threadCount>
                <excludes>
                    <exclude>**/*IntegrationTest.java</exclude>
                </excludes>
            </configuration>
        </plugin>
        
        <!-- Failsafe Plugin - 集成测试 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>3.2.5</version>
            <executions>
                <execution>
                    <goals>
                        <goal>integration-test</goal>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        
        <!-- Jacoco - 代码覆盖率 -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
                <execution>
                    <id>check</id>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <rule>
                                <element>BUNDLE</element>
                                <limits>
                                    <limit>
                                        <counter>LINE</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.70</minimum>
                                    </limit>
                                    <limit>
                                        <counter>BRANCH</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.60</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        
        <!-- Spotless - 代码格式化检查 -->
        <plugin>
            <groupId>com.diffplug.spotless</groupId>
            <artifactId>spotless-maven-plugin</artifactId>
            <version>2.43.0</version>
            <configuration>
                <java>
                    <googleJavaFormat/>
                    <removeUnusedImports/>
                </java>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## 📊 测试报告配置

### 1. Allure 报告

**pom.xml**：

```xml
<dependencies>
    <dependency>
        <groupId>io.qameta.allure</groupId>
        <artifactId>allure-junit5</artifactId>
        <version>2.25.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>io.qameta.allure</groupId>
            <artifactId>allure-maven</artifactId>
            <version>2.12.0</version>
        </plugin>
    </plugins>
</build>
```

**生成报告**：
```bash
mvn clean test allure:report
open target/site/allure-maven-plugin/index.html
```

---

### 2. 测试标签分类

```java
@Tag("unit")
@Tag("fast")
class UserServiceTest { ... }

@Tag("integration")
@Tag("slow")
class PlanControllerIntegrationTest { ... }

@Tag("regression")
class CriticalFlowTest { ... }
```

**运行指定标签**：
```bash
# 只运行快速测试
mvn test -Dgroups=fast

# 跳过集成测试
mvn test -DexcludedGroups=integration
```

---

## 🎯 质量门禁配置

### SonarQube 配置

**sonar-project.properties**：

```properties
sonar.projectKey=jxkh-performance-system
sonar.projectName=绩效管理系统
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.test.exclusions=**/*IntegrationTest.java

# 质量门禁阈值
sonar.qualitygate.wait=true
```

**门禁规则**：
- 新增代码覆盖率 ≥ 80%
- 整体代码覆盖率 ≥ 70%
- 0 个 Blocker 级别问题
- 0 个 Critical 级别漏洞
- 重复代码率 ≤ 3%

---

## 🔐 环境变量管理

### GitHub Secrets 配置

在 GitHub Repository Settings → Secrets 中配置：

| Secret | 说明 | 示例 |
|--------|------|------|
| `SONAR_TOKEN` | SonarQube 认证令牌 | sqp_xxx |
| `CODECOV_TOKEN` | Codecov 上传令牌 | xxx-xxx |
| `DB_PASSWORD` | 测试数据库密码 | test123 |
| `REDIS_PASSWORD` | Redis 密码 | redis123 |

**在工作流中使用**：

```yaml
env:
  SPRING_DATASOURCE_PASSWORD: ${{ secrets.DB_PASSWORD }}
  REDIS_PASSWORD: ${{ secrets.REDIS_PASSWORD }}
```

---

## ⚡ 性能优化

### 1. 缓存依赖

```yaml
- name: Cache Maven dependencies
  uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
    restore-keys: |
      ${{ runner.os }}-maven-

- name: Cache Node modules
  uses: actions/cache@v4
  with:
    path: frontend/node_modules
    key: ${{ runner.os }}-node-${{ hashFiles('frontend/package-lock.json') }}
```

---

### 2. 并行执行测试

```yaml
strategy:
  matrix:
    test-group: [unit, integration, e2e]
  fail-fast: false

steps:
  - name: Run ${{ matrix.test-group }} tests
    run: |
      if [ "${{ matrix.test-group }}" == "unit" ]; then
        mvn test -Dgroups=unit
      elif [ "${{ matrix.test-group }}" == "integration" ]; then
        mvn verify -Dgroups=integration
      fi
```

---

### 3. 超时控制

```yaml
jobs:
  backend-test:
    timeout-minutes: 15  # 15 分钟超时
  
  frontend-test:
    timeout-minutes: 10  # 10 分钟超时
```

---

## 📈 监控和告警

### 1. 测试趋势分析

使用 Codecov 或 SonarQube 查看：
- 覆盖率趋势图
- 测试执行时间趋势
- 缺陷密度变化

---

### 2. Slack 通知

```yaml
- name: Notify Slack
  if: always()
  uses: slackapi/slack-github-action@v1.25.0
  with:
    payload: |
      {
        "text": "测试完成状态: ${{ job.status }}",
        "blocks": [
          {
            "type": "section",
            "text": {
              "type": "mrkdwn",
              "text": "*测试执行结果*\n状态: ${{ job.status }}\n分支: ${{ github.ref }}\n提交: ${{ github.sha }}"
            }
          }
        ]
      }
  env:
    SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

---

## 📚 相关文档

- [测试策略总纲](./testing-strategy.md)
- [后端测试指南](./backend-testing-guide.md)
- [前端测试指南](./frontend-testing-guide.md)

---

**文档版本**: V1.0  
**最后更新**: 2026-04-14  
**维护者**: DevOps 团队
