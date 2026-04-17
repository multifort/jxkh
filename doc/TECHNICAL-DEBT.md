# 技术债务清单

本文档记录了项目中已知的技术债务和待改进项，按优先级排序。

---

## P0 - 高优先级（安全与核心质量）

### 1. ~~Refresh Token 安全存储~~ ✅ 已完成
**状态**: 已完成  
**完成时间**: 2026-04-15  
**描述**: Refresh Token 改用 HttpOnly Cookie 存储，防止 XSS 攻击  
**相关文件**: 
- `backend/src/main/java/com/iyunxin/jxkh/module/auth/controller/AuthController.java`
- `frontend/src/services/authService.ts`

---

### 2. ~~并发登录限制~~ ✅ 已完成
**状态**: 已完成  
**完成时间**: 2026-04-15  
**描述**: 限制同一账号最多 3 个设备同时在线，超出时踢出最早的设备  
**实现方式**: 使用 Redis ZSet 存储会话信息  
**相关文件**: 
- `backend/src/main/java/com/iyunxin/jxkh/module/auth/service/AuthService.java`
- `backend/src/main/resources/application.yml`

---

### 3. ~~数据权限隔离~~ ✅ 已完成
**状态**: 已完成  
**完成时间**: 2026-04-15  
**描述**: 实现基于组织的数据隔离，用户只能查看本组织的数据  
**实现方式**: DataPermissionService 提供数据过滤功能  
**相关文件**: 
- `backend/src/main/java/com/iyunxin/jxkh/module/user/service/DataPermissionService.java`
- `backend/src/main/java/com/iyunxin/jxkh/module/user/service/UserService.java`
- `backend/src/main/java/com/iyunxin/jxkh/common/util/SecurityUtils.java`

**已完善**:
- [x] 从 JWT Token 中获取当前用户ID（通过 SecurityUtils）
- [x] UserService 已应用数据权限过滤
- [x] 添加 org:view:all 和 org:view:cross 权限支持

**待优化**:
- [ ] 在更多 Service 方法中应用数据权限过滤（如 OrgService、PerformanceService 等）

---

### 4. ~~集成测试覆盖~~ ✅ 部分完成
**状态**: 已完成 AuthController 集成测试  
**完成时间**: 2026-04-15  
**描述**: 为核心 API 添加集成测试  

**已完成**:
- [x] AuthControllerIntegrationTest（7个测试用例）

**待补充**:
- [ ] UserControllerIntegrationTest
- [ ] RoleControllerIntegrationTest
- [ ] PermissionControllerIntegrationTest
- [ ] OrgControllerIntegrationTest

**相关文件**: 
- `backend/src/test/java/com/iyunxin/jxkh/module/auth/controller/AuthControllerIntegrationTest.java`

---

## P1 - 中优先级（功能完善）

### 5. ~~AuthServiceTest 编译错误~~ ✅ 已完成
**优先级**: P1  
**状态**: 已修复  
**完成时间**: 2026-04-15  
**描述**: AuthServiceTest 编译错误已修复，所有测试用例可正常运行  
**修复内容**:
- 修正 JwtUtil 方法签名调用
- 修正 LoginResponse 字段访问
- 添加必要的 Mock 配置

---

### 6. UserService 单元测试
**优先级**: P1  
**状态**: 未开始  
**描述**: 为 UserService 添加完整的单元测试  
**预计工作量**: 1 天

**测试用例**:
- [ ] 创建用户 - 成功
- [ ] 创建用户 - 用户名已存在
- [ ] 创建用户 - 工号已存在
- [ ] 更新用户 - 成功
- [ ] 更新用户 - 不允许修改用户名
- [ ] 删除用户 - 逻辑删除
- [ ] 启用/禁用用户
- [ ] 重置密码
- [ ] 解锁用户
- [ ] 分配用户角色

---

### 7. OrgService 单元测试
**优先级**: P1  
**状态**: 未开始  
**描述**: 为 OrgService 添加单元测试  
**预计工作量**: 1 天

**测试用例**:
- [ ] 创建组织 - 成功
- [ ] 获取组织树
- [ ] 多层级组织树查询
- [ ] 删除组织 - 子组织处理
- [ ] 更新组织

---

### 17. 绩效模块 Service 层代码重复 【Sprint2】
**优先级**: P1  
**状态**: 待优化  
**发现时间**: 2026-04-17  
**描述**: CycleService、IndicatorService、WeightSchemeService、IndicatorCategoryService 中存在大量重复的数据权限控制代码  
**影响范围**: 
- `CycleService.java` (334行)
- `IndicatorService.java` (277行)
- `WeightSchemeService.java` (399行)
- `IndicatorCategoryService.java` (278行)

**重复代码**:
```java
// 每个 Service 都有相同的 getCurrentUser() 方法
// 每个 Service 都有相同的 getSubOrgIds() 递归方法
// 每个 Service 都有相同的 applyDataPermission() 和 checkDataPermission() 逻辑
```

**建议方案**:
1. 提取基类 `BaseDataService`，封装通用的数据权限逻辑
2. 使用 AOP 切面统一处理数据权限校验
3. 创建 `DataPermissionHelper` 工具类

**预计工作量**: 2-3 天  
**风险评估**: 低（重构不影响功能）

---

### 18. 周期时间冲突检测性能问题 【Sprint2】
**优先级**: P1  
**状态**: 待优化  
**发现时间**: 2026-04-17  
**描述**: `CycleService.checkDateConflict()` 方法在检测到冲突后，使用 stream 遍历所有周期进行二次确认，性能较差  
**位置**: `CycleService.java:268-272`

**当前实现**:
```java
PerformanceCycle conflictingCycle = cycleRepository.findByIsDeletedFalse().stream()
    .filter(c -> !c.getId().equals(excludeId))
    .filter(c -> !c.getStartDate().isAfter(endDate) && !c.getEndDate().isBefore(startDate))
    .findFirst()
    .orElse(null);
```

**问题**:
- 加载所有周期到内存
- O(n) 时间复杂度
- 大数据量时性能差

**建议方案**:
1. 在 Repository 层添加精确查询方法
2. 使用数据库层面的时间范围重叠判断
3. 添加索引优化查询

**预计工作量**: 0.5 天  
**风险评估**: 低

---

### 19. 指标分类层级计算缺少循环引用检测 【Sprint2】
**优先级**: P1  
**状态**: 待修复  
**发现时间**: 2026-04-17  
**描述**: `IndicatorCategoryService` 在更新父分类时，没有检测循环引用（如 A->B->A）  
**位置**: `IndicatorCategoryService.java:157-165`

**风险场景**:
```
分类A的parentId = B
分类B的parentId = A  // 形成循环引用
```

**建议方案**:
1. 在设置 parentId 前，检查是否会导致循环引用
2. 使用 DFS/BFS 算法检测环路
3. 添加数据库约束或触发器

**预计工作量**: 1 天  
**风险评估**: 中（可能导致数据不一致）

---

### 20. 权重方案复制时使用时间戳作为编码后缀 【Sprint2】
**优先级**: P1  
**状态**: 待优化  
**发现时间**: 2026-04-17  
**描述**: `WeightSchemeService.copyScheme()` 使用 `System.currentTimeMillis()` 生成编码后缀，可读性差且可能冲突  
**位置**: `WeightSchemeService.java:274`

**当前实现**:
```java
newScheme.setCode(source.getCode() + "_COPY_" + System.currentTimeMillis());
```

**问题**:
- 编码不友好（如：WS001_COPY_1713340800000）
- 高并发下可能产生相同时间戳
- 不利于人工识别和管理

**建议方案**:
1. 使用 UUID 短格式（8位）
2. 或使用自增序号（WS001_COPY_1, WS001_COPY_2）
3. 或允许用户自定义复制后的编码

**预计工作量**: 0.5 天  
**风险评估**: 低

---

## P2 - 低优先级（体验优化）

### 8. E2E 测试
**优先级**: P2  
**状态**: 未开始  
**描述**: 添加端到端测试，验证完整业务流程  
**建议工具**: Cypress 或 Playwright  
**预计工作量**: 2-3 天

**测试场景**:
- [ ] 完整登录流程
- [ ] 用户管理完整流程（创建、编辑、删除）
- [ ] 角色权限分配流程
- [ ] 组织架构管理流程

---

### 9. 前端性能优化
**优先级**: P2  
**状态**: 未开始  
**描述**: 优化前端加载性能和运行时性能  
**预计工作量**: 1-2 天

**优化项**:
- [ ] 路由懒加载
- [ ] 组件代码分割
- [ ] 图片懒加载
- [ ] API 请求缓存
- [ ] 虚拟滚动（大数据列表）

---

### 10. 后端性能优化
**优先级**: P2  
**状态**: 未开始  
**描述**: 优化后端查询性能和响应速度  
**预计工作量**: 2-3 天

**优化项**:
- [ ] N+1 查询问题修复
- [ ] 数据库索引优化
- [ ] Redis 缓存策略优化
- [ ] 分页查询优化
- [ ] 批量操作优化

---

### 11. 日志和监控增强
**优先级**: P2  
**状态**: 部分完成  
**描述**: 完善日志记录和系统监控  
**预计工作量**: 1 天

**待完成**:
- [ ] 关键业务操作审计日志
- [ ] 异常告警机制
- [ ] 性能指标监控（QPS、响应时间）
- [ ] 慢查询日志

---

### 12. API 文档完善
**优先级**: P2  
**状态**: 自动生成了 Swagger 文档  
**描述**: 补充更详细的 API 文档和示例  
**预计工作量**: 0.5 天

**待完成**:
- [ ] 所有 API 的请求/响应示例
- [ ] 错误码说明
- [ ] 认证流程图解
- [ ] Postman Collection 导出

---

### 21. 前端页面缺少错误边界处理 【Sprint2】
**优先级**: P2  
**状态**: 待优化  
**发现时间**: 2026-04-17  
**描述**: CycleManagePage、IndicatorManagePage、WeightSchemeManagePage 缺少 React Error Boundary，组件错误会导致整个页面白屏  
**影响范围**: 
- `frontend/src/pages/performance/CycleManagePage.tsx`
- `frontend/src/pages/performance/IndicatorManagePage.tsx`
- `frontend/src/pages/performance/WeightSchemeManagePage.tsx`

**建议方案**:
1. 创建通用 ErrorBoundary 组件
2. 在每个页面外层包裹 ErrorBoundary
3. 显示友好的错误提示和重试按钮

**预计工作量**: 0.5 天  
**风险评估**: 低

---

### 22. 前端表格分页参数不一致 【Sprint2】
**优先级**: P2  
**状态**: 待优化  
**发现时间**: 2026-04-17  
**描述**: 前端分页从 1 开始，后端分页从 0 开始，每次请求都需要手动转换，容易出错  
**位置**: `CycleManagePage.tsx:39`, `cycleService.ts:18`

**当前实现**:
```typescript
// 前端
const response = await cycleService.getCycles(page - 1, size, ...);

// 后端
public Page<PerformanceCycle> getCycles(int page, int size, ...) {
    Pageable pageable = PageRequest.of(page, size, ...); // page 从 0 开始
}
```

**建议方案**:
1. 统一前后端分页规范（建议都从 0 开始）
2. 或在 API Client 层自动转换
3. 或创建统一的 usePagination Hook

**预计工作量**: 0.5 天  
**风险评估**: 中（需要修改多个页面）

---

### 23. 数据库迁移脚本缺少回滚脚本 【Sprint2】
**优先级**: P2  
**状态**: 待补充  
**发现时间**: 2026-04-17  
**描述**: V5-V8 迁移脚本只有正向迁移，没有对应的 U* 回滚脚本  
**影响范围**: 
- `V5__create_performance_cycles.sql`
- `V6__create_indicators.sql`
- `V7__create_weight_schemes.sql`
- `V8__insert_sprint2_test_data.sql`

**风险**:
- 生产环境部署失败时无法快速回滚
- 测试环境数据污染后难以清理

**建议方案**:
1. 为每个 V* 脚本创建对应的 U* 回滚脚本
2. 或使用 Flyway Undo 功能
3. 或在部署前备份数据

**预计工作量**: 1 天  
**风险评估**: 中

---

### 24. 指标表缺少外键约束 【Sprint2】
**优先级**: P2  
**状态**: 待优化  
**发现时间**: 2026-04-17  
**描述**: `indicators.category_id` 字段没有外键约束，可能导致引用不存在的分类  
**位置**: `V6__create_indicators.sql:27`

**当前表结构**:
```sql
CREATE TABLE `indicators` (
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  -- 缺少 FOREIGN KEY 约束
)
```

**风险**:
- 数据完整性无法保证
- 可能产生孤儿记录
- 查询时可能 JOIN 失败

**建议方案**:
1. 添加外键约束：`FOREIGN KEY (category_id) REFERENCES indicator_categories(id)`
2. 或在应用层加强校验
3. 或定期运行数据一致性检查任务

**预计工作量**: 0.5 天  
**风险评估**: 中（需要检查现有数据）

---

## P3 - 长期改进

### 13. 微服务架构改造
**优先级**: P3  
**状态**: 规划中  
**描述**: 将单体应用拆分为微服务  
**预计工作量**: 2-4 周

**拆分方案**:
- auth-service（认证服务）
- user-service（用户服务）
- org-service（组织服务）
- performance-service（绩效服务）

---

### 14. CI/CD 流水线完善
**优先级**: P3  
**状态**: 基础配置已完成  
**描述**: 完善自动化构建、测试、部署流程  
**预计工作量**: 1-2 天

**待完成**:
- [ ] 自动化单元测试执行
- [ ] 代码质量检查（SonarQube）
- [ ] 自动化部署到测试环境
- [ ] 灰度发布支持

---

### 15. 国际化支持
**优先级**: P3  
**状态**: 未开始  
**描述**: 支持多语言界面  
**预计工作量**: 3-5 天

**待完成**:
- [ ] 前端 i18n 配置
- [ ] 后端消息国际化
- [ ] 中英文翻译
- [ ] 语言切换功能

---

### 16. 移动端适配
**优先级**: P3  
**状态**: 未开始  
**描述**: 适配移动端浏览器  
**预计工作量**: 1-2 周

**待完成**:
- [ ] 响应式布局优化
- [ ] 触摸交互优化
- [ ] 移动端专用组件
- [ ] PWA 支持

---

## 📊 统计信息

| 优先级 | 数量 | 已完成 | 进行中 | 未开始 |
|--------|------|--------|--------|--------|
| P0 | 4 | 4 | 0 | 0 |
| P1 | 7 | 1 | 0 | 6 |
| P2 | 9 | 0 | 0 | 9 |
| P3 | 4 | 0 | 0 | 4 |
| **总计** | **24** | **5** | **0** | **19** |

**完成率**: 20.83% (5/24)

**Sprint2 新增**: 8 项技术债务（P1: 4项, P2: 4项）

---

## 📝 更新记录

- **2026-04-17**: Sprint2 Code Review 完成，新增 8 项技术债务（Sprint2 模块）
- **2026-04-15**: 创建文档，标记 Sprint 1 完成的4个P0任务
- **2026-04-15**: 记录剩余的技术债务项
- **2026-04-15**: 修复 AuthServiceTest 编译错误，完善数据权限获取用户ID

---

## 💡 处理建议

1. **每个 Sprint 预留 20% 时间**用于处理技术债务
2. **优先处理 P1 级别**的测试覆盖问题
3. **定期审查**技术债务清单，评估优先级变化
4. **新功能开发时**避免引入新的技术债务

---

**最后更新**: 2026-04-15  
**维护人**: 开发团队
