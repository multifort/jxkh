# 测试数据管理

## 📋 概述

本文档定义测试数据的生成、管理和清理策略，确保测试的独立性、可重复性和可维护性。

---

## 🎯 核心原则

### 1. 数据隔离

- ✅ 每个测试用例使用独立的数据
- ✅ 测试间互不影响
- ✅ 支持并行执行

### 2. 数据真实性

- ✅ 使用 Faker 生成真实感数据
- ✅ 符合业务规则和数据约束
- ✅ 覆盖边界情况

### 3. 数据可维护性

- ✅ 集中管理测试数据工厂
- ✅ 版本控制测试数据集
- ✅ 易于理解和修改

---

## 🔧 后端测试数据

### 1. TestDataFactory 模式

**示例**：

```java
@Component
public class TestDataFactory {
    
    private static final Faker faker = new Faker(Locale.CHINA);
    
    /**
     * 创建测试用户
     */
    public User createTestUser() {
        return User.builder()
            .username("test_user_" + UUID.randomUUID().toString().substring(0, 8))
            .email(faker.internet().emailAddress())
            .name(faker.name().fullName())
            .phone(faker.phoneNumber().cellPhone())
            .role(Role.EMPLOYEE)
            .departmentId(1L)
            .managerId(null)
            .status(UserStatus.ACTIVE)
            .build();
    }
    
    /**
     * 创建测试绩效周期
     */
    public PerformanceCycle createTestCycle() {
        LocalDate startDate = LocalDate.now().withDayOfYear(1);
        LocalDate endDate = startDate.plusMonths(3).minusDays(1);
        
        return PerformanceCycle.builder()
            .name(faker.company().industry() + " " + startDate.getYear() + " Q1")
            .type(CycleType.QUARTERLY)
            .startDate(startDate)
            .endDate(endDate)
            .status(CycleStatus.IN_PROGRESS)
            .build();
    }
    
    /**
     * 创建测试绩效计划
     */
    public PerformancePlan createTestPlan(User user, PerformanceCycle cycle) {
        return PerformancePlan.builder()
            .user(user)
            .cycle(cycle)
            .name(faker.lorem().sentence(5))
            .status(PlanStatus.DRAFT)
            .submittedAt(null)
            .approvedAt(null)
            .build();
    }
    
    /**
     * 创建测试指标
     */
    public IndicatorInstance createTestIndicator(PerformancePlan plan) {
        return IndicatorInstance.builder()
            .plan(plan)
            .name(faker.company().profession())
            .type(IndicatorType.KPI)
            .weight(new BigDecimal(faker.number().numberBetween(10, 50)))
            .targetValue(new BigDecimal(faker.number().numberBetween(100000, 1000000)))
            .currentValue(BigDecimal.ZERO)
            .unit("元")
            .build();
    }
}
```

---

### 2. 在测试中使用 Factory

**示例**：

```java
@SpringBootTest
class PlanServiceTest {
    
    @Autowired
    private TestDataFactory dataFactory;
    
    @Autowired
    private PlanService planService;
    
    @Test
    void should_createPlan() {
        // Arrange
        User user = dataFactory.createTestUser();
        PerformanceCycle cycle = dataFactory.createTestCycle();
        
        CreatePlanRequest request = new CreatePlanRequest();
        request.setUserId(user.getId());
        request.setCycleId(cycle.getId());
        request.setName("测试计划");
        
        // Act
        PerformancePlan plan = planService.createPlan(request);
        
        // Assert
        assertNotNull(plan.getId());
    }
}
```

---

### 3. Fixture 文件（JSON/YAML）

**示例** - `src/test/resources/fixtures/users.json`：

```json
[
  {
    "username": "employee01",
    "name": "张三",
    "email": "zhangsan@example.com",
    "role": "EMPLOYEE",
    "department": "技术部"
  },
  {
    "username": "manager01",
    "name": "李四",
    "email": "lisi@example.com",
    "role": "MANAGER",
    "department": "技术部"
  }
]
```

**加载 Fixture**：

```java
@Component
public class FixtureLoader {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public <T> List<T> loadFixture(String filename, Class<T> type) {
        try {
            InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("fixtures/" + filename);
            
            return objectMapper.readValue(
                is,
                objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, type)
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load fixture: " + filename, e);
        }
    }
}
```

---

## 🌐 前端测试数据

### 1. Mock Data 工厂

**示例** - `src/__tests__/mocks/dataFactory.ts`：

```typescript
import { faker } from '@faker-js/faker/locale/zh_CN';

export const createUser = (overrides?: Partial<User>): User => ({
  id: faker.string.uuid(),
  username: faker.internet.userName(),
  name: faker.person.fullName(),
  email: faker.internet.email(),
  role: 'EMPLOYEE',
  department: '技术部',
  ...overrides,
});

export const createPerformancePlan = (
  overrides?: Partial<PerformancePlan>
): PerformancePlan => ({
  id: faker.string.uuid(),
  name: faker.lorem.sentence(3),
  userId: faker.string.uuid(),
  cycleId: faker.string.uuid(),
  status: 'DRAFT',
  indicators: [],
  createdAt: faker.date.recent().toISOString(),
  ...overrides,
});

export const createIndicator = (
  overrides?: Partial<Indicator>
): Indicator => ({
  id: faker.string.uuid(),
  name: faker.company.catchPhrase(),
  type: 'KPI',
  weight: faker.number.int({ min: 10, max: 50 }),
  targetValue: faker.number.int({ min: 100000, max: 1000000 }),
  currentValue: 0,
  unit: '元',
  ...overrides,
});

// 批量生成
export const createUserList = (count: number): User[] => {
  return Array.from({ length: count }, () => createUser());
};
```

---

### 2. 在测试中使用

**示例**：

```typescript
import { createUser, createPerformancePlan } from '../mocks/dataFactory';

test('should display user info', () => {
  const user = createUser({ name: '张三' });
  
  render(<UserProfile user={user} />);
  
  expect(screen.getByText('张三')).toBeInTheDocument();
});

test('should render plan list', () => {
  const plans = [
    createPerformancePlan({ name: 'Q1计划' }),
    createPerformancePlan({ name: 'Q2计划' }),
  ];
  
  render(<PlanList plans={plans} />);
  
  expect(screen.getByText('Q1计划')).toBeInTheDocument();
  expect(screen.getByText('Q2计划')).toBeInTheDocument();
});
```

---

## 🗄️ 数据库测试数据

### 1. SQL 初始化脚本

**示例** - `src/test/resources/schema-test.sql`：

```sql
-- 基础数据
INSERT INTO departments (id, name, parent_id) VALUES
(1, '总公司', NULL),
(2, '技术部', 1),
(3, '销售部', 1);

INSERT INTO roles (id, name, code) VALUES
(1, '员工', 'EMPLOYEE'),
(2, '主管', 'MANAGER'),
(3, 'HR', 'HR');
```

---

### 2. TestContainers 数据初始化

**示例**：

```java
@Testcontainers
@SpringBootTest
class DatabaseIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withInitScript("schema-test.sql");  // 自动执行初始化脚本
    
    @Test
    void should_loadInitialData() {
        // 验证初始数据已加载
        List<Department> depts = departmentRepository.findAll();
        assertThat(depts).hasSize(3);
    }
}
```

---

## 🔄 数据清理策略

### 1. 事务回滚（推荐）

```java
@SpringBootTest
@Transactional  // 测试结束后自动回滚
class PlanServiceTest {
    
    @Test
    void should_createPlan() {
        // 测试中插入的数据会在测试后自动回滚
    }
}
```

---

### 2. @AfterEach 手动清理

```java
@SpringBootTest
class ManualCleanupTest {
    
    @Autowired
    private PlanRepository planRepository;
    
    @AfterEach
    void cleanup() {
        planRepository.deleteAll();
    }
    
    @Test
    void test1() {
        // 插入数据
    }
    
    @Test
    void test2() {
        // 数据已被清理，干净的环境
    }
}
```

---

### 3. 唯一标识符避免冲突

```java
@Test
void should_createUniqueUser() {
    // 使用时间戳或 UUID 确保唯一性
    String uniqueUsername = "test_user_" + System.currentTimeMillis();
    
    User user = new User(uniqueUsername, "test@example.com");
    userService.createUser(user);
    
    // 即使测试失败也不会影响其他测试
}
```

---

## 📦 测试数据集管理

### 1. 常见测试场景数据集

**正常数据**：
```java
User validUser = createUser();
PerformancePlan validPlan = createPlan(validUser);
```

**边界数据**：
```java
// 空值
User userWithNullName = createUser().withName(null);

// 极值
Indicator highWeightIndicator = createIndicator().withWeight(new BigDecimal("100"));

// 特殊字符
User userWithSpecialChars = createUser().withName("张三@#$%");
```

**异常数据**：
```java
// 无效邮箱
User invalidUser = createUser().withEmail("not-an-email");

// 负数权重
Indicator negativeWeight = createIndicator().withWeight(new BigDecimal("-10"));
```

---

### 2. 数据版本控制

**目录结构**：
```
src/test/resources/
└── fixtures/
    ├── v1.0/
    │   ├── users.json
    │   └── plans.json
    ├── v1.1/
    │   ├── users.json  # 新增字段
    │   └── plans.json
    └── latest -> v1.1/  # 符号链接指向最新版本
```

---

## 🎯 最佳实践

### 1. 使用 Builder 模式

```java
// 灵活构建测试数据
User user = UserBuilder.aUser()
    .withName("张三")
    .withRole(Role.MANAGER)
    .withDepartment("技术部")
    .build();
```

---

### 2. 避免共享可变状态

```java
// ❌ 错误：共享可变对象
private static User sharedUser = createUser();

@Test
void test1() {
    sharedUser.setName("Modified");  // 影响其他测试
}

// ✅ 正确：每次创建新对象
@Test
void test1() {
    User user = createUser();  // 独立对象
}
```

---

### 3. 数据生成性能优化

```java
// ❌ 慢：每次都创建 Faker 实例
Faker faker = new Faker();

// ✅ 快：复用单例
private static final Faker FAKER = new Faker();
```

---

## 📚 相关文档

- [测试策略总纲](./testing-strategy.md)
- [后端测试指南](./backend-testing-guide.md)
- [前端测试指南](./frontend-testing-guide.md)

---

**文档版本**: V1.0  
**最后更新**: 2026-04-14  
**维护者**: QA 团队
