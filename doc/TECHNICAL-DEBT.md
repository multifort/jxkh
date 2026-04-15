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
| P1 | 3 | 1 | 0 | 2 |
| P2 | 5 | 0 | 0 | 5 |
| P3 | 4 | 0 | 0 | 4 |
| **总计** | **16** | **5** | **0** | **11** |

**完成率**: 31.25% (5/16)

---

## 📝 更新记录

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
