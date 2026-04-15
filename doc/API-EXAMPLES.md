# API 接口示例与错误码

本文档提供常用 API 的请求/响应示例和完整错误码列表。

---

## 1. 认证授权接口

### 1.1 用户登录

**请求**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin@123456"
}
```

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "admin",
      "name": "系统管理员",
      "employeeNo": "EMP001",
      "avatar": "https://example.com/avatar.jpg",
      "roles": ["ADMIN"],
      "permissions": ["dashboard:view", "performance:plan:view", "..."]
    }
  }
}
```

**失败响应 (401)**
```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}
```

---

### 1.2 刷新 Token

**请求**
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400000
  }
}
```

---

### 1.3 退出登录

**请求**
```http
POST /api/v1/auth/logout
Authorization: Bearer {accessToken}
```

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 2. 绩效计划接口

### 2.1 创建绩效计划

**请求**
```http
POST /api/v1/plans
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "cycleId": 1,
  "indicators": [
    {
      "indicatorId": 101,
      "name": "销售额增长",
      "type": "KPI",
      "weight": 40.00,
      "targetValue": 1000000.00,
      "unit": "元"
    },
    {
      "indicatorId": 102,
      "name": "客户满意度",
      "type": "KPI",
      "weight": 30.00,
      "targetValue": 95.00,
      "unit": "%"
    },
    {
      "indicatorId": 201,
      "name": "完成产品迭代",
      "type": "OKR",
      "weight": 30.00,
      "targetValue": 3.00,
      "unit": "次"
    }
  ]
}
```

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "success",
  "data": 1001
}
```

**失败响应 (400) - 权重总和不等于100%**
```json
{
  "code": 400,
  "message": "指标权重总和必须为100%，当前为100.00%",
  "data": null
}
```

---

### 2.2 查询绩效计划列表

**请求**
```http
GET /api/v1/plans?cycleId=1&status=IN_PROGRESS&page=0&size=10
Authorization: Bearer {accessToken}
```

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1001,
        "userId": 10,
        "userName": "张三",
        "employeeNo": "EMP010",
        "orgName": "销售部",
        "cycleName": "2026年Q1",
        "status": "IN_PROGRESS",
        "totalScore": null,
        "finalLevel": null,
        "submittedAt": "2026-01-05T10:00:00",
        "approvedAt": "2026-01-08T14:30:00",
        "createdAt": "2026-01-03T09:00:00"
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "number": 0,
    "size": 10
  }
}
```

---

### 2.3 提交绩效计划审批

**请求**
```http
POST /api/v1/plans/1001/submit
Authorization: Bearer {accessToken}
```

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**失败响应 (400) - 状态不允许提交**
```json
{
  "code": 400,
  "message": "计划状态不允许提交，当前状态：DRAFT",
  "data": null
}
```

---

### 2.4 审批绩效计划

**请求**
```http
POST /api/v1/plans/1001/approve?approved=true&comment=计划合理，同意执行
Authorization: Bearer {accessToken}
```

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 3. 进度记录接口

### 3.1 创建周报

**请求**
```http
POST /api/v1/records
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "planId": 1001,
  "type": "WEEKLY_REPORT",
  "content": "<p>本周完成了以下工作：</p><ul><li>拜访了5个重要客户</li><li>签订了2个新合同</li></ul>",
  "progress": 35.50,
  "recordDate": "2026-01-15",
  "attachments": [
    "https://oss.example.com/reports/week1.pdf",
    "https://oss.example.com/reports/contracts.xlsx"
  ]
}
```

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "success",
  "data": 5001
}
```

---

### 3.2 AI 智能总结

**请求**
```http
POST /api/v1/ai/weekly-summary
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "content": "本周完成了以下工作：\n1. 拜访了5个重要客户\n2. 签订了2个新合同，总金额50万\n3. 参加了销售技能培训\n4. 协助团队完成了季度目标"
}
```

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "summary": "本周工作重点突出，客户拜访和新合同签订成果显著，同时注重个人能力提升和团队协作。",
    "achievements": [
      "拜访5个重要客户",
      "签订2个新合同（50万）",
      "参加销售技能培训",
      "协助团队完成季度目标"
    ],
    "risks": [],
    "suggestions": [
      "建议跟进已拜访客户的后续需求",
      "可以将培训所学应用到实际工作中"
    ]
  }
}
```

---

## 4. 绩效评估接口

### 4.1 提交自评

**请求**
```http
POST /api/v1/scores
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "planId": 1001,
  "scoreType": "SELF",
  "scoreValue": 88.50,
  "comment": "本季度整体表现良好，销售额超额完成15%，客户满意度达到96%。但在团队协作方面还有提升空间。",
  "dimensions": {
    "业绩达成": 90,
    "工作态度": 85,
    "团队协作": 80,
    "创新能力": 85
  }
}
```

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 4.2 查询待评估列表

**请求**
```http
GET /api/v1/scores/pending?evaluatorId=5&page=0&size=10
Authorization: Bearer {accessToken}
```

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "planId": 1001,
        "employeeName": "张三",
        "employeeNo": "EMP010",
        "orgName": "销售部",
        "cycleName": "2026年Q1",
        "selfScore": 88.50,
        "status": "PENDING_EVAL"
      }
    ],
    "totalElements": 8,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

---

## 5. 数据分析接口

### 5.1 查询绩效看板数据

**请求**
```http
GET /api/v1/dashboard/overview?cycleId=1&orgId=10
Authorization: Bearer {accessToken}
```

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalEmployees": 50,
    "completedEmployees": 45,
    "completionRate": 90.00,
    "averageScore": 82.35,
    "levelDistribution": {
      "A": 10,
      "B": 25,
      "C": 8,
      "D": 2
    },
    "riskCount": 5,
    "topPerformers": [
      {
        "userId": 15,
        "userName": "李四",
        "score": 95.50,
        "level": "A"
      }
    ]
  }
}
```

---

### 5.2 查询部门排名

**请求**
```http
GET /api/v1/dashboard/dept-ranking?cycleId=1
Authorization: Bearer {accessToken}
```

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "orgId": 10,
      "orgName": "销售部",
      "avgScore": 85.20,
      "employeeCount": 20,
      "rank": 1
    },
    {
      "orgId": 11,
      "orgName": "技术部",
      "avgScore": 83.50,
      "employeeCount": 15,
      "rank": 2
    }
  ]
}
```

---

## 6. 错误码完整列表

### 6.1 通用错误码

| 错误码 | HTTP状态 | 说明 | 处理建议 |
|--------|---------|------|----------|
| 200 | 200 | 成功 | - |
| 400 | 400 | 请求参数错误 | 检查请求参数格式和值 |
| 401 | 401 | 未认证或Token过期 | 重新登录或刷新Token |
| 403 | 403 | 无权限访问 | 联系管理员获取权限 |
| 404 | 404 | 资源不存在 | 检查资源ID是否正确 |
| 409 | 409 | 资源冲突 | 检查是否有重复数据 |
| 422 | 422 | 业务校验失败 | 查看错误消息了解具体原因 |
| 429 | 429 | 请求过于频繁 | 稍后重试 |
| 500 | 500 | 服务器内部错误 | 联系技术支持 |
| 503 | 503 | 服务不可用 | 稍后重试 |

---

### 6.2 业务错误码

#### 认证授权

| 错误码 | 消息 | 说明 |
|--------|------|------|
| AUTH_INVALID_CREDENTIALS | 用户名或密码错误 | 登录凭证错误 |
| AUTH_ACCOUNT_LOCKED | 账号已被锁定 | 登录失败次数过多 |
| AUTH_TOKEN_EXPIRED | Token已过期 | Access Token过期 |
| AUTH_TOKEN_INVALID | Token无效 | Token被篡改或格式错误 |
| AUTH_REFRESH_TOKEN_EXPIRED | Refresh Token已过期 | 需要重新登录 |

#### 用户管理

| 错误码 | 消息 | 说明 |
|--------|------|------|
| USER_NOT_FOUND | 用户不存在 | 用户ID不存在 |
| USER_USERNAME_EXISTS | 用户名已存在 | 用户名重复 |
| USER_EMPLOYEE_NO_EXISTS | 工号已存在 | 工号重复 |
| USER_EMAIL_EXISTS | 邮箱已被使用 | 邮箱重复 |

#### 组织管理

| 错误码 | 消息 | 说明 |
|--------|------|------|
| ORG_NOT_FOUND | 组织不存在 | 组织ID不存在 |
| ORG_CODE_EXISTS | 组织编码已存在 | 组织编码重复 |
| ORG_HAS_CHILDREN | 组织下存在子组织 | 无法删除 |
| ORG_HAS_USERS | 组织下存在用户 | 无法删除 |

#### 绩效计划

| 错误码 | 消息 | 说明 |
|--------|------|------|
| PLAN_NOT_FOUND | 绩效计划不存在 | 计划ID不存在 |
| PLAN_WEIGHT_INVALID | 指标权重总和必须为100% | 权重校验失败 |
| PLAN_STATUS_INVALID | 计划状态不允许此操作 | 状态机校验失败 |
| PLAN_DUPLICATE | 该周期已存在绩效计划 | 同一用户同一周期只能有一个计划 |
| PLAN_INDICATOR_NOT_FOUND | 指标不存在 | 指标ID不存在 |
| PLAN_INDICATOR_DISABLED | 指标已禁用 | 不能使用禁用的指标 |

#### 绩效评估

| 错误码 | 消息 | 说明 |
|--------|------|------|
| SCORE_NOT_FOUND | 评分记录不存在 | 评分ID不存在 |
| SCORE_DUPLICATE | 已提交过该类型的评分 | 不允许重复评分 |
| SCORE_VALUE_INVALID | 评分必须在0-100之间 | 评分范围校验 |
| SCORE_NOT_SELF | 不能为自己评分 | 自评限制 |
| SCORE_NOT_MANAGER | 不是直属上级，无法评价 | 权限校验 |

#### 绩效校准

| 错误码 | 消息 | 说明 |
|--------|------|------|
| CALIBRATION_DISTRIBUTION_INVALID | 不符合强制分布要求 | A级占比超过20%等 |
| CALIBRATION_REASON_REQUIRED | 必须填写校准原因 | 调整等级时需要原因 |

#### 进度记录

| 错误码 | 消息 | 说明 |
|--------|------|------|
| RECORD_NOT_FOUND | 进度记录不存在 | 记录ID不存在 |
| RECORD_DATE_INVALID | 记录日期不在周期范围内 | 日期校验 |
| RECORD_TYPE_INVALID | 记录类型不正确 | 只能是WEEKLY/MONTHLY等 |

#### 指标管理

| 错误码 | 消息 | 说明 |
|--------|------|------|
| INDICATOR_NOT_FOUND | 指标不存在 | 指标ID不存在 |
| INDICATOR_CODE_EXISTS | 指标编码已存在 | 编码重复 |
| INDICATOR_IN_USE | 指标正在使用中 | 无法删除或禁用 |

#### 绩效周期

| 错误码 | 消息 | 说明 |
|--------|------|------|
| CYCLE_NOT_FOUND | 绩效周期不存在 | 周期ID不存在 |
| CYCLE_DATE_OVERLAP | 周期时间与其他周期重叠 | 时间冲突 |
| CYCLE_STATUS_INVALID | 周期状态不允许此操作 | 状态机校验 |
| CYCLE_HAS_PLANS | 周期下存在绩效计划 | 无法删除 |

#### AI 服务

| 错误码 | 消息 | 说明 |
|--------|------|------|
| AI_SERVICE_UNAVAILABLE | AI服务暂时不可用 | 服务故障或配额耗尽 |
| AI_TIMEOUT | AI服务响应超时 | 网络问题或服务繁忙 |
| AI_INVALID_RESPONSE | AI返回结果格式错误 | 解析失败 |

---

### 6.3 错误响应格式

所有错误响应统一采用以下格式：

```json
{
  "code": 400,
  "message": "具体的错误描述信息",
  "data": null,
  "timestamp": "2026-04-15T10:30:00",
  "path": "/api/v1/plans"
}
```

**字段说明**：
- `code`: 业务错误码
- `message`: 人类可读的错误描述
- `data`: 错误详情（可选，通常为null）
- `timestamp`: 错误发生时间（ISO 8601格式）
- `path`: 请求路径

---

## 7. API 限流规则

| 接口类型 | 限流策略 | 说明 |
|---------|---------|------|
| 登录接口 | 5次/分钟/IP | 防止暴力破解 |
| 普通查询 | 100次/分钟/IP | 正常业务请求 |
| 写操作 | 30次/分钟/IP | 创建、更新、删除 |
| AI接口 | 10次/分钟/用户 | AI服务成本较高 |
| 导出接口 | 5次/分钟/用户 | 大数据量导出 |

**限流响应 (429)**
```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后重试",
  "data": null,
  "retryAfter": 30
}
```

---

## 8. 版本管理

### 8.1 URL 版本控制

所有 API 使用 URL 路径版本控制：

```
/api/v1/resource
/api/v2/resource  (未来版本)
```

### 8.2 向后兼容原则

- ✅ 新增字段不影响旧客户端
- ✅ 废弃字段标记 `@Deprecated`，保留至少2个版本
- ❌ 不删除已有字段
- ❌ 不修改字段类型
- ❌ 不修改接口路径

---

**文档版本**: V1.0  
**最后更新**: 2026-04-15  
**维护者**: JXKH Team
