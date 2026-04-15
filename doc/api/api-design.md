# API 接口设计

## 1. 接口规范

### 1.1 RESTful 设计原则
- **资源命名**：使用名词复数，小写字母 + 连字符（如：`/api/performance-plans`）
- **HTTP 方法**：
  - `GET`：查询资源
  - `POST`：创建资源
  - `PUT`：完整更新资源
  - `PATCH`：部分更新资源
  - `DELETE`：删除资源
- **状态码**：
  - `200`：成功
  - `201`：创建成功
  - `400`：请求参数错误
  - `401`：未认证
  - `403`：无权限
  - `404`：资源不存在
  - `500`：服务器错误

### 1.2 统一响应格式

#### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 业务数据
  },
  "timestamp": 1712345678901
}
```

#### 失败响应
```json
{
  "code": 400,
  "message": "参数验证失败",
  "errors": [
    {
      "field": "name",
      "message": "名称不能为空"
    }
  ],
  "timestamp": 1712345678901
}
```

### 1.3 分页格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100,
      "totalPages": 5
    }
  }
}
```

### 1.4 认证方式
- **Header**：`Authorization: Bearer <JWT_TOKEN>`
- **Token 刷新**：使用 Refresh Token 机制

---

## 2. 认证授权接口

### 2.1 用户登录

**接口**：`POST /api/auth/login`

**请求体**：
```json
{
  "username": "admin",
  "password": "password123"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "admin",
      "name": "管理员",
      "role": "ADMIN",
      "orgId": 1
    }
  }
}
```

---

### 2.2 刷新 Token

**接口**：`POST /api/auth/refresh`

**请求体**：
```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

**响应**：同登录接口

---

### 2.3 退出登录

**接口**：`POST /api/auth/logout`

**请求头**：`Authorization: Bearer <token>`

**响应**：
```json
{
  "code": 200,
  "message": "退出成功"
}
```

---

### 2.4 修改密码

**接口**：`PUT /api/auth/password`

**请求体**：
```json
{
  "oldPassword": "old_password",
  "newPassword": "new_password"
}
```

---

## 3. 用户管理接口

### 3.1 获取当前用户信息

**接口**：`GET /api/users/me`

**响应**：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "admin",
    "name": "管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "employeeNo": "E001",
    "orgId": 1,
    "orgName": "技术部",
    "managerId": null,
    "managerName": null,
    "role": "ADMIN",
    "avatar": "https://example.com/avatar.jpg",
    "permissions": ["dashboard:view", "performance:plan:create"]
  }
}
```

---

### 3.2 查询用户列表

**接口**：`GET /api/users`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 20 |
| keyword | String | 否 | 搜索关键词（姓名/工号） |
| orgId | Long | 否 | 组织ID |
| role | String | 否 | 角色 |
| status | String | 否 | 状态 |

**响应**：
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "username": "zhangsan",
        "name": "张三",
        "employeeNo": "E001",
        "email": "zhangsan@example.com",
        "phone": "13800138000",
        "orgId": 1,
        "orgName": "技术部",
        "position": "高级工程师",
        "managerId": 2,
        "managerName": "李四",
        "role": "EMPLOYEE",
        "status": "ACTIVE",
        "createdAt": "2026-01-01T00:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100,
      "totalPages": 5
    }
  }
}
```

---

### 3.3 创建用户

**接口**：`POST /api/users`

**请求体**：
```json
{
  "username": "wangwu",
  "password": "password123",
  "name": "王五",
  "employeeNo": "E002",
  "email": "wangwu@example.com",
  "phone": "13800138001",
  "orgId": 1,
  "positionId": 1,
  "managerId": 2,
  "role": "EMPLOYEE"
}
```

---

### 3.4 更新用户

**接口**：`PUT /api/users/{id}`

**请求体**：
```json
{
  "name": "王五",
  "email": "wangwu@example.com",
  "phone": "13800138001",
  "orgId": 1,
  "positionId": 1,
  "managerId": 2,
  "role": "EMPLOYEE"
}
```

---

### 3.5 删除用户

**接口**：`DELETE /api/users/{id}`

---

### 3.6 批量导入用户

**接口**：`POST /api/users/import`

**请求**：`multipart/form-data`
- `file`: Excel 文件

**响应**：
```json
{
  "code": 200,
  "data": {
    "successCount": 95,
    "failCount": 5,
    "errors": [
      {
        "row": 3,
        "employeeNo": "E003",
        "error": "工号已存在"
      }
    ]
  }
}
```

---

## 4. 组织管理接口

### 4.1 获取组织树

**接口**：`GET /api/orgs/tree`

**响应**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "总公司",
      "code": "HQ",
      "parentId": null,
      "level": 1,
      "leaderId": 1,
      "leaderName": "张三",
      "children": [
        {
          "id": 2,
          "name": "技术部",
          "code": "TECH",
          "parentId": 1,
          "level": 2,
          "leaderId": 2,
          "leaderName": "李四",
          "children": []
        }
      ]
    }
  ]
}
```

---

### 4.2 创建组织

**接口**：`POST /api/orgs`

**请求体**：
```json
{
  "name": "产品部",
  "code": "PRODUCT",
  "parentId": 1,
  "leaderId": 3,
  "sort": 1,
  "description": "产品研发部门"
}
```

---

### 4.3 更新组织

**接口**：`PUT /api/orgs/{id}`

---

### 4.4 删除组织

**接口**：`DELETE /api/orgs/{id}`

**注意**：需检查是否有子组织或员工

---

## 5. 绩效周期接口

### 5.1 创建绩效周期

**接口**：`POST /api/cycles`

**请求体**：
```json
{
  "name": "2026年Q1",
  "type": "QUARTERLY",
  "startDate": "2026-01-01",
  "endDate": "2026-03-31",
  "remark": "第一季度绩效考核"
}
```

**响应**：
```json
{
  "code": 201,
  "data": {
    "id": 1,
    "name": "2026年Q1",
    "type": "QUARTERLY",
    "startDate": "2026-01-01",
    "endDate": "2026-03-31",
    "status": "NOT_STARTED",
    "createdAt": "2026-01-01T00:00:00Z"
  }
}
```

---

### 5.2 查询周期列表

**接口**：`GET /api/cycles`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 否 | 状态筛选 |
| type | String | 否 | 类型筛选 |
| year | Integer | 否 | 年份筛选 |

**响应**：
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "name": "2026年Q1",
        "type": "QUARTERLY",
        "startDate": "2026-01-01",
        "endDate": "2026-03-31",
        "status": "IN_PROGRESS",
        "progress": 65,
        "totalPlans": 100,
        "completedPlans": 65
      }
    ]
  }
}
```

---

### 5.3 启动周期

**接口**：`POST /api/cycles/{id}/start`

---

### 5.4 结束周期

**接口**：`POST /api/cycles/{id}/end`

---

## 6. 绩效计划接口

### 6.1 创建绩效计划

**接口**：`POST /api/plans`

**请求体**：
```json
{
  "userId": 1,
  "cycleId": 1,
  "indicators": [
    {
      "indicatorId": 1,
      "name": "销售额",
      "type": "KPI",
      "weight": 40,
      "targetValue": 1000000,
      "unit": "元"
    },
    {
      "indicatorId": 2,
      "name": "客户满意度",
      "type": "KPI",
      "weight": 30,
      "targetValue": 90,
      "unit": "分"
    },
    {
      "indicatorId": 3,
      "name": "新产品上线",
      "type": "OKR",
      "weight": 30,
      "targetValue": 3,
      "unit": "个"
    }
  ]
}
```

---

### 6.2 查询绩效计划列表

**接口**：`GET /api/plans`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cycleId | Long | 是 | 周期ID |
| userId | Long | 否 | 用户ID |
| orgId | Long | 否 | 组织ID |
| status | String | 否 | 状态筛选 |
| keyword | String | 否 | 搜索关键词 |
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应**：
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "userId": 1,
        "userName": "张三",
        "orgId": 1,
        "orgName": "技术部",
        "cycleId": 1,
        "cycleName": "2026年Q1",
        "status": "IN_PROGRESS",
        "totalScore": 85.5,
        "finalLevel": "B",
        "progress": 75,
        "indicatorCount": 5,
        "submittedAt": "2026-01-15T10:00:00Z",
        "createdAt": "2026-01-10T09:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100,
      "totalPages": 5
    }
  }
}
```

---

### 6.3 查询绩效计划详情

**接口**：`GET /api/plans/{id}`

**响应**：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "userId": 1,
    "userName": "张三",
    "orgId": 1,
    "orgName": "技术部",
    "cycleId": 1,
    "cycleName": "2026年Q1",
    "status": "IN_PROGRESS",
    "totalScore": 85.5,
    "finalLevel": "B",
    "evaluatorId": 2,
    "evaluatorName": "李四",
    "comment": "表现良好",
    "indicators": [
      {
        "id": 1,
        "indicatorId": 1,
        "name": "销售额",
        "type": "KPI",
        "weight": 40,
        "targetValue": 1000000,
        "currentValue": 850000,
        "progress": 85,
        "status": "IN_PROGRESS",
        "score": 85,
        "unit": "元"
      }
    ],
    "records": [
      {
        "id": 1,
        "type": "WEEKLY_REPORT",
        "content": "本周完成...",
        "progress": 80,
        "recordDate": "2026-03-01",
        "createdAt": "2026-03-01T18:00:00Z"
      }
    ],
    "scores": [
      {
        "id": 1,
        "scoreType": "SELF",
        "scoreValue": 85,
        "comment": "自评",
        "createdAt": "2026-03-15T10:00:00Z"
      }
    ],
    "submittedAt": "2026-01-15T10:00:00Z",
    "createdAt": "2026-01-10T09:00:00Z"
  }
}
```

---

### 6.4 提交绩效计划

**接口**：`POST /api/plans/{id}/submit`

**响应**：
```json
{
  "code": 200,
  "message": "提交成功"
}
```

---

### 6.5 审批绩效计划

**接口**：`POST /api/plans/{id}/approve`

**请求体**：
```json
{
  "approved": true,
  "comment": "同意"
}
```

---

### 6.6 更新绩效计划

**接口**：`PUT /api/plans/{id}`

**注意**：只有草稿状态可修改

---

### 6.7 删除绩效计划

**接口**：`DELETE /api/plans/{id}`

**注意**：只有草稿状态可删除

---

## 7. 指标管理接口

### 7.1 创建指标模板

**接口**：`POST /api/indicators`

**请求体**：
```json
{
  "name": "销售额",
  "type": "KPI",
  "category": "FINANCIAL",
  "description": "季度销售目标",
  "calculationRule": "实际销售额 / 目标销售额 × 100",
  "calcType": "AUTO",
  "dataSource": "ERP",
  "defaultWeight": 40,
  "unit": "元"
}
```

---

### 7.2 查询指标列表

**接口**：`GET /api/indicators`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 否 | 指标类型 |
| category | String | 否 | 指标分类 |
| enabled | Boolean | 否 | 是否启用 |

---

### 7.3 更新指标

**接口**：`PUT /api/indicators/{id}`

---

### 7.4 删除指标

**接口**：`DELETE /api/indicators/{id}`

**注意**：需检查是否被引用

---

## 8. 进度跟踪接口

### 8.1 创建执行记录

**接口**：`POST /api/records`

**请求体**：
```json
{
  "planId": 1,
  "type": "WEEKLY_REPORT",
  "content": "本周完成了以下工作：\n1. 完成需求分析\n2. 开始开发...",
  "progress": 75,
  "attachments": ["https://example.com/file1.pdf"],
  "recordDate": "2026-03-01"
}
```

---

### 8.2 查询执行记录列表

**接口**：`GET /api/records`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| planId | Long | 是 | 计划ID |
| type | String | 否 | 记录类型 |
| startDate | Date | 否 | 开始日期 |
| endDate | Date | 否 | 结束日期 |

---

### 8.3 更新指标进度

**接口**：`PATCH /api/indicator-instances/{id}/progress`

**请求体**：
```json
{
  "currentValue": 850000
}
```

**响应**：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "currentValue": 850000,
    "progress": 85,
    "status": "IN_PROGRESS"
  }
}
```

---

## 9. 绩效评估接口

### 9.1 提交评分

**接口**：`POST /api/scores`

**请求体**：
```json
{
  "planId": 1,
  "scoreType": "MANAGER",
  "scoreValue": 88,
  "comment": "工作表现优秀，超额完成任务",
  "dimensions": [
    {
      "dimension": "工作质量",
      "weight": 40,
      "score": 90,
      "comment": "质量很高"
    },
    {
      "dimension": "工作效率",
      "weight": 30,
      "score": 85,
      "comment": "效率良好"
    },
    {
      "dimension": "团队协作",
      "weight": 30,
      "score": 88,
      "comment": "协作能力强"
    }
  ]
}
```

---

### 9.2 查询评分列表

**接口**：`GET /api/scores`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| planId | Long | 是 | 计划ID |

**响应**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "planId": 1,
      "evaluatorId": 2,
      "evaluatorName": "李四",
      "scoreType": "SELF",
      "scoreValue": 85,
      "comment": "自评",
      "dimensions": [],
      "createdAt": "2026-03-15T10:00:00Z"
    },
    {
      "id": 2,
      "planId": 1,
      "evaluatorId": 2,
      "evaluatorName": "李四",
      "scoreType": "MANAGER",
      "scoreValue": 88,
      "comment": "上级评价",
      "dimensions": [...],
      "createdAt": "2026-03-20T14:00:00Z"
    }
  ]
}
```

---

### 9.3 获取待评估列表

**接口**：`GET /api/scores/pending`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| scoreType | String | 否 | 评分类型 |

---

## 10. 绩效校准接口

### 10.1 执行校准

**接口**：`POST /api/calibrations`

**请求体**：
```json
{
  "cycleId": 1,
  "orgId": 1,
  "adjustments": [
    {
      "planId": 1,
      "afterScore": 90,
      "afterLevel": "A",
      "adjustReason": "考虑到项目难度较大，适当上调"
    }
  ]
}
```

---

### 10.2 查询校准记录

**接口**：`GET /api/calibrations`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cycleId | Long | 是 | 周期ID |
| orgId | Long | 否 | 组织ID |

---

### 10.3 获取校准建议

**接口**：`GET /api/calibrations/suggestions`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cycleId | Long | 是 | 周期ID |
| orgId | Long | 是 | 组织ID |

**响应**：
```json
{
  "code": 200,
  "data": {
    "currentDistribution": {
      "A": 15,
      "B": 60,
      "C": 20,
      "D": 5
    },
    "targetDistribution": {
      "A": 20,
      "B": 70,
      "C": 10,
      "D": 0
    },
    "suggestions": [
      {
        "planId": 1,
        "userName": "张三",
        "currentScore": 88,
        "currentLevel": "B",
        "suggestedScore": 90,
        "suggestedLevel": "A",
        "reason": "接近A级分数线，建议上调"
      }
    ]
  }
}
```

---

## 11. 数据分析接口

### 11.1 获取绩效看板数据

**接口**：`GET /api/dashboard`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cycleId | Long | 否 | 周期ID，默认当前周期 |

**响应**：
```json
{
  "code": 200,
  "data": {
    "cycleInfo": {
      "id": 1,
      "name": "2026年Q1",
      "status": "IN_PROGRESS",
      "progress": 65
    },
    "overview": {
      "completionRate": 82.5,
      "averageScore": 83.2,
      "riskCount": 5,
      "totalEmployees": 100,
      "completedEmployees": 82
    },
    "levelDistribution": {
      "A": 15,
      "B": 50,
      "C": 30,
      "D": 5
    },
    "deptPerformances": [
      {
        "orgId": 1,
        "orgName": "技术部",
        "averageScore": 85.5,
        "employeeCount": 30
      }
    ],
    "topPerformers": [
      {
        "userId": 1,
        "userName": "张三",
        "orgName": "技术部",
        "score": 95,
        "level": "A"
      }
    ],
    "trendData": {
      "labels": ["2025Q1", "2025Q2", "2025Q3", "2025Q4", "2026Q1"],
      "scores": [80, 82, 81, 83, 83.2]
    }
  }
}
```

---

### 11.2 部门绩效对比

**接口**：`GET /api/analytics/dept-comparison`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cycleId | Long | 是 | 周期ID |

---

### 11.3 绩效趋势分析

**接口**：`GET /api/analytics/trend`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 否 | 用户ID，不传则为全公司 |
| periods | Integer | 否 | 周期数，默认 4 |

---

### 11.4 导出绩效报表

**接口**：`GET /api/analytics/export`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cycleId | Long | 是 | 周期ID |
| format | String | 否 | 格式：EXCEL/PDF，默认 EXCEL |

**响应**：文件下载

---

## 12. 通知接口

### 12.1 获取通知列表

**接口**：`GET /api/notifications`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| isRead | Boolean | 否 | 是否已读 |
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

---

### 12.2 标记为已读

**接口**：`PUT /api/notifications/{id}/read`

---

### 12.3 批量标记已读

**接口**：`PUT /api/notifications/read-all`

---

### 12.4 获取未读数量

**接口**：`GET /api/notifications/unread-count`

**响应**：
```json
{
  "code": 200,
  "data": {
    "count": 5
  }
}
```

---

## 13. 系统配置接口

### 13.1 获取配置列表

**接口**：`GET /api/configs`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | String | 否 | 配置分类 |

---

### 13.2 更新配置

**接口**：`PUT /api/configs/{key}`

**请求体**：
```json
{
  "value": "new_value"
}
```

---

### 13.3 获取评分模型列表

**接口**：`GET /api/scoring-models`

---

### 13.4 创建评分模型

**接口**：`POST /api/scoring-models`

**请求体**：
```json
{
  "name": "销售岗位模型",
  "code": "SALES_MODEL",
  "kpiWeight": 70,
  "okrWeight": 20,
  "peerWeight": 10,
  "applicableRoles": ["SALES"],
  "levelMappings": [
    {
      "level": "A",
      "minScore": 90,
      "maxScore": 100,
      "description": "优秀",
      "distributionRatio": 20
    }
  ]
}
```

---

## 14. AI 功能接口

### 14.1 AI 周报总结

**接口**：`POST /api/ai/weekly-summary`

**请求体**：
```json
{
  "planId": 1,
  "weekRecords": [
    {
      "date": "2026-03-01",
      "content": "本周工作内容..."
    }
  ]
}
```

**响应**：
```json
{
  "code": 200,
  "data": {
    "summary": "本周主要完成了需求分析和系统设计...",
    "highlights": ["完成需求文档", "确定技术方案"],
    "risks": ["进度略有滞后"],
    "suggestions": ["建议增加人手", "优化工作流程"]
  }
}
```

---

### 14.2 AI 评分建议

**接口**：`POST /api/ai/score-suggestion`

**请求体**：
```json
{
  "planId": 1
}
```

**响应**：
```json
{
  "code": 200,
  "data": {
    "suggestedScore": 85,
    "suggestedLevel": "B",
    "reasoning": "根据KPI完成率85%，OKR进度80%，结合同岗位平均水平，建议评分为85分",
    "factors": [
      {
        "factor": "KPI完成率",
        "value": 85,
        "impact": "positive"
      },
      {
        "factor": "OKR进度",
        "value": 80,
        "impact": "neutral"
      }
    ]
  }
}
```

---

### 14.3 AI 风险预警

**接口**：`GET /api/ai/risk-alerts`

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| planId | Long | 否 | 计划ID，不传则返回所有风险 |

**响应**：
```json
{
  "code": 200,
  "data": [
    {
      "planId": 1,
      "userName": "张三",
      "riskType": "PROGRESS_DELAY",
      "severity": "HIGH",
      "description": "进度滞后，当前进度60%，时间已过70%",
      "suggestions": ["增加工作时间", "寻求团队支持"]
    }
  ]
}
```

---

## 15. 错误码定义

### 15.1 通用错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或 Token 过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 422 | 业务逻辑错误 |
| 500 | 服务器内部错误 |

### 15.2 业务错误码

| 错误码 | 说明 |
|--------|------|
| 1001 | 用户名或密码错误 |
| 1002 | 账号已被锁定 |
| 2001 | 绩效周期不存在 |
| 2002 | 绩效计划已提交，无法修改 |
| 2003 | 指标权重总和不等于100% |
| 2004 | 评分已完成，无法重复评分 |
| 2005 | 不在评分时间内 |
| 3001 | 组织下存在子组织或员工，无法删除 |

---

## 16. API 版本管理

### 16.1 版本策略
- URL 路径包含版本号：`/api/v1/...`
- 向后兼容至少 2 个版本
- 废弃的 API 保留 6 个月

### 16.2 版本演进
```
v1: 初始版本（当前）
v2: 计划支持多租户
v3: 引入微服务架构
```

---

## 17. 接口限流

### 17.1 限流规则
- **普通接口**：100 次/分钟/IP
- **登录接口**：10 次/分钟/IP
- **导出接口**：5 次/分钟/用户

### 17.2 限流响应
```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后重试",
  "retryAfter": 30
}
```

---

**文档版本**: V1.0  
**最后更新**: 2026-04-14  
**维护者**: API 团队
