# 权限与安全设计

## 1. 安全架构概述

### 1.1 安全目标
- **机密性**：保护敏感数据不被未授权访问
- **完整性**：确保数据在传输和存储过程中不被篡改
- **可用性**：保证系统持续可用，防止 DoS 攻击
- **可审计性**：所有关键操作可追溯

### 1.2 安全原则
- **最小权限原则**：用户只拥有完成工作所需的最小权限
- **纵深防御**：多层安全防护
- **默认拒绝**：未明确允许的请求一律拒绝
- **安全默认值**：所有配置默认采用最安全的设置

---

## 2. 认证机制（Authentication）

### 2.1 JWT Token 认证

#### 2.1.1 Token 结构

```
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "user_id",
  "username": "admin",
  "role": "ADMIN",
  "orgId": 1,
  "iat": 1712345678,
  "exp": 1712352878,
  "jti": "unique_token_id"
}

Signature:
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

#### 2.1.2 Token 类型

**Access Token**：
- 有效期：2 小时
- 用途：API 请求认证
- 存储：内存（前端）

**Refresh Token**：
- 有效期：7 天
- 用途：刷新 Access Token
- 存储：HttpOnly Cookie

#### 2.1.3 Token 刷新流程

```
1. Access Token 过期 → 2. 使用 Refresh Token 请求刷新 
→ 3. 服务端验证 Refresh Token → 4. 返回新的 Access Token
→ 5. 继续正常请求
```

**安全措施**：
- Refresh Token 绑定设备指纹
- 刷新后旧 Refresh Token 失效
- 检测异常刷新行为（频繁刷新、异地刷新）

---

### 2.2 密码安全

#### 2.2.1 密码加密

**算法**：BCrypt

**参数**：
- Rounds: 12
- Salt: 自动生成

**示例**：
```java
String hashedPassword = BCrypt.hashpw("plain_password", BCrypt.gensalt(12));
boolean matches = BCrypt.checkpw("input_password", hashedPassword);
```

#### 2.2.2 密码策略

**复杂度要求**：
- 最少 8 个字符
- 至少包含大写字母、小写字母、数字、特殊字符中的 3 种
- 不能包含用户名、邮箱等个人信息
- 不能使用常见弱密码（检查密码字典）

**历史密码**：
- 不能与最近 5 次密码相同
- 记录密码历史哈希值

**定期更换**：
- 强制每 90 天更换密码
- 到期前 7 天开始提醒

---

### 2.3 登录安全

#### 2.3.1 登录失败处理

**策略**：
- 连续失败 5 次 → 锁定账号 15 分钟
- 连续失败 10 次 → 锁定账号 1 小时
- 连续失败 20 次 → 锁定账号 24 小时，需管理员解锁

**记录**：
- 记录失败时间、IP、User-Agent
- 触发告警（短时间内大量失败）

#### 2.3.2 验证码

**触发条件**：
- 登录失败 3 次后需要验证码
- 异地登录需要验证码
- 非常用设备登录需要验证码

**验证码类型**：
- 图形验证码（简单）
- 滑块验证码（中等）
- 短信验证码（高安全场景）

---

### 2.4 会话管理

#### 2.4.1 会话超时

**策略**：
- 无操作 30 分钟自动登出
- 关闭浏览器清除 Session
- 多设备登录限制（最多 3 个设备）

#### 2.4.2 并发控制

**单点登录（可选）**：
- 同一账号只能在一个设备登录
- 新登录踢掉旧登录
- 发送通知给被踢出的设备

---

## 3. 授权机制（Authorization）

### 3.1 RBAC 模型

#### 3.1.1 角色定义

| 角色 | 代码 | 说明 |
|------|------|------|
| 员工 | EMPLOYEE | 普通员工，只能查看和操作自己的绩效 |
| 主管 | MANAGER | 团队主管，可以管理团队绩效 |
| HR | HR | 人力资源，可以配置规则和查看全公司数据 |
| 管理员 | ADMIN | 系统管理员，拥有所有权限 |

#### 3.1.2 权限粒度

**功能权限**：
- 菜单权限：能否访问某个页面
- 按钮权限：能否执行某个操作
- API 权限：能否调用某个接口

**数据权限**：
- 本人数据：只能查看自己的数据
- 本部门数据：可以查看本部门数据
- 下级部门数据：可以查看下级部门数据
- 全公司数据：可以查看所有数据

---

### 3.2 权限实现

#### 3.2.1 后端权限控制

**方法级权限**：
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long userId) {
    // 只有管理员可以删除用户
}

@PreAuthorize("hasPermission(#planId, 'performance_plan', 'edit')")
public void updatePlan(Long planId, PlanDTO dto) {
    // 检查对特定绩效计划的编辑权限
}
```

**自定义权限评估器**：
```java
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    
    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, 
                                  Object permission) {
        User user = (User) auth.getPrincipal();
        
        // 本人数据权限
        if ("self".equals(permission)) {
            return ((PerformancePlan) targetDomainObject).getUserId()
                   .equals(user.getId());
        }
        
        // 部门数据权限
        if ("dept".equals(permission)) {
            return isSameDepartment(user, targetDomainObject);
        }
        
        return false;
    }
}
```

---

#### 3.2.2 前端权限控制

**路由守卫**：
```typescript
const router = createRouter({
  routes: [
    {
      path: '/performance/plans',
      component: PerformancePlans,
      meta: { 
        requiresAuth: true,
        permissions: ['performance:plan:view']
      }
    }
  ]
});

router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login');
    return;
  }
  
  if (to.meta.permissions) {
    const hasPermission = to.meta.permissions.every((perm: string) =>
      userStore.hasPermission(perm)
    );
    
    if (!hasPermission) {
      next('/403');
      return;
    }
  }
  
  next();
});
```

**组件级权限**：
```tsx
// 权限指令
<Permission code="performance:plan:create">
  <Button onClick={handleCreate}>新建计划</Button>
</Permission>

// 权限 Hook
const { hasPermission } = usePermission();
{hasPermission('performance:plan:edit') && (
  <Button onClick={handleEdit}>编辑</Button>
)}
```

---

### 3.3 数据权限实现

#### 3.3.1 SQL 层过滤

**MyBatis-Plus 拦截器**：
```java
@Component
public class DataPermissionInterceptor implements InnerInterceptor {
    
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, 
                            Object parameter, RowBounds rowBounds, 
                            ResultHandler resultHandler, BoundSql boundSql) {
        User currentUser = SecurityUtils.getCurrentUser();
        
        // 根据角色添加数据权限过滤
        if ("EMPLOYEE".equals(currentUser.getRole())) {
            // 员工只能看自己的数据
            appendWhere(boundSql, "user_id = " + currentUser.getId());
        } else if ("MANAGER".equals(currentUser.getRole())) {
            // 主管可以看本部门数据
            List<Long> deptIds = getSubordinateDeptIds(currentUser.getId());
            appendWhere(boundSql, "org_id IN (" + deptIds + ")");
        }
        // HR 和 ADMIN 不添加过滤
    }
}
```

#### 3.3.2 数据权限注解

```java
@DataScope(deptAlias = "dept", userAlias = "user")
public List<PerformancePlan> selectPlanList(PerformancePlan plan) {
    // 自动添加数据权限过滤
    return planMapper.selectPlanList(plan);
}
```

---

## 4. API 安全

### 4.1 HTTPS 加密

**要求**：
- 所有 API 请求必须使用 HTTPS
- TLS 1.2 或更高版本
- 禁用不安全的加密套件

**证书管理**：
- 使用受信任的 CA 证书
- 证书到期前 30 天告警
- 支持 Let's Encrypt 自动续期

---

### 4.2 CORS 配置

**生产环境配置**：
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://yourdomain.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

**注意**：
- 不要使用 `*` 通配符
- 明确指定允许的域名
- 生产环境禁用 `allowCredentials` 与 `*` 组合

---

### 4.3 请求签名（可选）

**适用场景**：
- 高安全要求的接口
- 外部系统集成

**签名算法**：
```
signature = HMAC-SHA256(method + url + timestamp + body, secret_key)
```

**请求头**：
```
X-API-Key: your_api_key
X-Timestamp: 1712345678
X-Signature: abc123...
```

**服务端验证**：
- 验证时间戳（5 分钟内有效）
- 验证签名
- 防止重放攻击

---

### 4.4 接口限流

#### 4.4.1 限流策略

**基于 IP**：
- 普通接口：100 次/分钟
- 登录接口：10 次/分钟
- 导出接口：5 次/分钟

**基于用户**：
- 普通用户：200 次/分钟
- VIP 用户：500 次/分钟

**基于接口**：
- 核心接口：更高配额
- 资源密集型接口：更低配额

#### 4.4.2 实现方案

**Redis + Lua 脚本**：
```lua
local key = "rate_limit:" .. KEYS[1]
local limit = tonumber(ARGV[1])
local expire = tonumber(ARGV[2])

local current = redis.call('INCR', key)
if current == 1 then
    redis.call('EXPIRE', key, expire)
end

if current > limit then
    return 0
else
    return 1
end
```

**Spring AOP 实现**：
```java
@RateLimit(key = "#userId", limit = 100, period = 60)
public List<PerformancePlan> getPlans(Long userId) {
    // ...
}
```

---

### 4.5 输入验证

#### 4.5.1 参数校验

**JSR-303 注解**：
```java
public class CreatePlanRequest {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotBlank(message = "指标名称不能为空")
    @Size(min = 1, max = 100, message = "指标名称长度必须在1-100之间")
    private String indicatorName;
    
    @Min(value = 0, message = "权重不能小于0")
    @Max(value = 100, message = "权重不能大于100")
    private BigDecimal weight;
    
    @Email(message = "邮箱格式不正确")
    private String email;
}
```

**全局异常处理**：
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result> handleValidationException(
            MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors();
        return ResponseEntity.badRequest()
            .body(Result.error(400, "参数验证失败", errors));
    }
}
```

#### 4.5.2 SQL 注入防护

**措施**：
- 使用预编译语句（PreparedStatement）
- MyBatis 使用 `#{}` 而非 `${}`
- 禁止拼接 SQL

**示例**：
```java
// ✅ 正确
@Select("SELECT * FROM users WHERE username = #{username}")
User findByUsername(@Param("username") String username);

// ❌ 错误
@Select("SELECT * FROM users WHERE username = '${username}'")
User findByUsername(@Param("username") String username);
```

#### 4.5.3 XSS 防护

**前端**：
- React 自动转义输出
- 富文本使用白名单过滤

**后端**：
```java
@Component
public class XssFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        XssHttpServletRequestWrapper xssRequest = 
            new XssHttpServletRequestWrapper(httpRequest);
        chain.doFilter(xssRequest, response);
    }
}

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return value != null ? Jsoup.clean(value, Whitelist.none()) : null;
    }
}
```

---

## 5. 数据安全

### 5.1 敏感数据加密

#### 5.1.1 加密字段

**需要加密的字段**：
- 身份证号
- 手机号
- 银行卡号
- 薪资信息

**加密算法**：AES-256-GCM

**实现**：
```java
@Component
public class DataEncryptionService {
    
    private static final String SECRET_KEY = "your-secret-key";
    
    public String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(
                SECRET_KEY.getBytes(), "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, 
                new byte[12]); // IV
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }
    
    public String decrypt(String ciphertext) {
        // 解密逻辑
    }
}
```

**数据库层面**：
```sql
-- 存储加密后的数据
ALTER TABLE users ADD COLUMN phone_encrypted VARBINARY(255);
```

---

#### 5.1.2 数据脱敏

**脱敏规则**：
- 手机号：138****8000
- 身份证：110101********0001
- 邮箱：z***@example.com
- 姓名：张*

**实现**：
```java
@JsonSerialize(using = PhoneSerializer.class)
private String phone;

public class PhoneSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String phone, JsonGenerator gen, 
                          SerializerProvider serializers) 
            throws IOException {
        if (phone != null && phone.length() == 11) {
            String masked = phone.substring(0, 3) + "****" + 
                           phone.substring(7);
            gen.writeString(masked);
        } else {
            gen.writeString(phone);
        }
    }
}
```

---

### 5.2 数据备份

#### 5.2.1 备份策略

**全量备份**：
- 频率：每天凌晨 2:00
- 保留：30 天
- 存储：异地备份服务器

**增量备份**：
- 频率：每小时
- 保留：7 天

**Binlog 备份**：
- 实时同步
- 用于时间点恢复

#### 5.2.2 备份验证

**定期恢复测试**：
- 每月进行一次恢复演练
- 验证备份文件完整性
- 记录恢复时间（RTO）

---

### 5.3 数据归档

**归档策略**：
- 2 年以上的绩效数据归档
- 归档数据移到历史表
- 生产库只保留最近 2 年数据

**归档流程**：
```sql
-- 1. 创建归档表
CREATE TABLE performance_plans_archive LIKE performance_plans;

-- 2. 迁移数据
INSERT INTO performance_plans_archive
SELECT * FROM performance_plans
WHERE updated_at < DATE_SUB(NOW(), INTERVAL 2 YEAR);

-- 3. 删除已归档数据
DELETE FROM performance_plans
WHERE updated_at < DATE_SUB(NOW(), INTERVAL 2 YEAR);
```

---

## 6. 日志与审计

### 6.1 操作日志

#### 6.1.1 日志内容

**记录信息**：
- 操作用户
- 操作时间
- 操作类型（增删改查）
- 操作模块
- 请求参数
- 响应结果
- IP 地址
- User-Agent
- 耗时

**示例**：
```json
{
  "userId": 1,
  "username": "admin",
  "operation": "UPDATE",
  "module": "performance_plan",
  "method": "PUT",
  "url": "/api/plans/123",
  "requestParams": {"totalScore": 85},
  "responseCode": 200,
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "duration": 150,
  "timestamp": "2026-04-14T10:30:00Z"
}
```

#### 6.1.2 日志采集

**AOP 切面**：
```java
@Aspect
@Component
public class OperationLogAspect {
    
    @Autowired
    private OperationLogService logService;
    
    @AfterReturning("@annotation(operationLog)")
    public void logOperation(JoinPoint joinPoint, OperationLog operationLog) {
        OperationLogEntity log = new OperationLogEntity();
        log.setUserId(SecurityUtils.getCurrentUserId());
        log.setOperation(operationLog.value());
        log.setModule(operationLog.module());
        log.setRequestParams(JSON.toJSONString(joinPoint.getArgs()));
        log.setIpAddress(getIpAddress());
        log.setTimestamp(LocalDateTime.now());
        
        logService.save(log);
    }
}
```

---

### 6.2 安全事件日志

**记录事件**：
- 登录成功/失败
- 权限变更
- 密码修改
- 敏感数据访问
- 异常操作

**告警规则**：
- 短时间多次登录失败
- 异地登录
- 批量导出数据
- 非工作时间访问敏感数据

---

### 6.3 日志存储与分析

**技术方案**：
- ELK Stack（Elasticsearch + Logstash + Kibana）
- 日志 retention：90 天
- 热数据：7 天（SSD）
- 冷数据：83 天（HDD）

**查询功能**：
- 按用户查询
- 按时间范围查询
- 按操作类型查询
- 关键字搜索

---

## 7. 网络安全

### 7.1 防火墙配置

**入站规则**：
- 允许 443（HTTPS）
- 允许 80（HTTP，重定向到 HTTPS）
- 允许 22（SSH，仅限堡垒机 IP）
- 拒绝其他所有端口

**出站规则**：
- 允许 DNS（53）
- 允许 NTP（123）
- 允许 HTTPS（443）
- 允许 SMTP（587，邮件服务）

---

### 7.2 DDoS 防护

**防护措施**：
- CDN 加速（Cloudflare / 阿里云 CDN）
- Web 应用防火墙（WAF）
- 速率限制
- IP 黑名单

**监控指标**：
- QPS 突增
- 带宽使用率
- 连接数
- 错误率

---

### 7.3 内网安全

**网络隔离**：
- DMZ 区：Nginx、负载均衡
- 应用区：Spring Boot 应用
- 数据区：MySQL、Redis
- 各区域通过防火墙隔离

**访问控制**：
- 应用服务器只能访问数据库服务器的 3306 端口
- 数据库服务器不允许直接访问互联网
- 使用 VPN 访问内网

---

## 8. 安全测试

### 8.1 渗透测试

**测试频率**：
- 每季度一次全面渗透测试
- 每次重大更新后进行专项测试

**测试内容**：
- OWASP Top 10 漏洞扫描
- 业务逻辑漏洞测试
- 权限绕过测试
- SQL 注入测试
- XSS 测试
- CSRF 测试

**工具**：
- Burp Suite
- OWASP ZAP
- SQLMap
- Nessus

---

### 8.2 代码安全扫描

**静态分析**：
- SonarQube 代码质量扫描
- Fortify 安全扫描
- Checkmarx SAST

**依赖扫描**：
- OWASP Dependency-Check
- Snyk
- GitHub Dependabot

**集成 CI/CD**：
```yaml
# GitHub Actions
- name: Security Scan
  run: |
    npm audit
    mvn dependency-check:check
    sonar-scanner
```

---

### 8.3 安全编码规范

**Java 安全规范**：
- 不使用反序列化不可信数据
- 不使用 eval() 执行动态代码
- 文件上传验证类型和大小
- 路径遍历防护

**前端安全规范**：
- 不使用 innerHTML
- CSP 头配置
- Cookie 设置 HttpOnly 和 Secure
- 敏感信息不存储在 localStorage

---

## 9. 应急响应

### 9.1 安全事件分级

| 级别 | 描述 | 响应时间 |
|------|------|---------|
| P0 | 核心数据泄露、系统被入侵 | 15 分钟 |
| P1 | 高危漏洞、大规模服务中断 | 1 小时 |
| P2 | 中危漏洞、部分功能异常 | 4 小时 |
| P3 | 低危漏洞、轻微影响 | 24 小时 |

---

### 9.2 应急响应流程

```
发现安全事件 → 初步评估 → 启动应急预案 → 隔离受影响系统 
→ 调查原因 → 修复漏洞 → 恢复服务 → 事后复盘
```

**应急联系人**：
- 安全负责人：张三（电话）
- 技术负责人：李四（电话）
- 公关负责人：王五（电话）

---

### 9.3 灾难恢复

**RTO（恢复时间目标）**：
- 核心业务：4 小时
- 非核心业务：24 小时

**RPO（恢复点目标）**：
- 核心数据：1 小时
- 非核心数据：24 小时

**恢复步骤**：
1. 评估损害程度
2. 切换到备用系统
3. 从备份恢复数据
4. 验证系统完整性
5. 逐步恢复服务
6. 通知用户

---

## 10. 合规性

### 10.1 数据隐私合规

**遵循法规**：
- 《中华人民共和国网络安全法》
- 《个人信息保护法》
- GDPR（如有欧盟用户）

**合规措施**：
- 用户同意机制
- 隐私政策公示
- 数据最小化原则
- 用户权利保障（查询、删除、导出）

---

### 10.2 审计合规

**审计要求**：
- 保留操作日志至少 6 个月
- 定期内部审计
- 第三方安全审计（每年）

**审计报告**：
- 安全漏洞报告
- 渗透测试报告
- 合规性评估报告

---

## 11. 安全意识培训

### 11.1 培训内容

**开发人员**：
- 安全编码规范
- 常见漏洞及防范
- 代码审查要点

**运维人员**：
- 安全配置最佳实践
- 应急响应流程
- 日志分析方法

**全体员工**：
- 密码安全
- 钓鱼邮件识别
- 社会工程学防范

---

### 11.2 培训频率

- 新员工入职培训
- 每季度安全分享会
- 每年安全知识竞赛

---

## 12. 安全检查清单

### 12.1 上线前检查

- [ ] HTTPS 配置正确
- [ ] 所有 API 有权限控制
- [ ] 输入验证完整
- [ ] SQL 注入防护
- [ ] XSS 防护
- [ ] CSRF 防护
- [ ] 敏感数据加密
- [ ] 日志记录完整
- [ ] 错误信息不泄露敏感数据
- [ ] 依赖库无已知漏洞
- [ ] 压力测试通过
- [ ] 备份恢复测试通过

---

### 12.2 定期检查

- [ ] 安全补丁更新
- [ ] 证书有效期检查
- [ ] 权限审查
- [ ] 日志审计
- [ ] 备份验证
- [ ] 漏洞扫描
- [ ] 性能监控
- [ ] 异常行为检测

---

**文档版本**: V1.0  
**最后更新**: 2026-04-14  
**维护者**: 安全团队
