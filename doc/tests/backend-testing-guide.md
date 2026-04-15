# 后端测试指南

## 📋 概述

本文档提供 Spring Boot 后端项目的测试规范和最佳实践，包括单元测试、集成测试和性能测试。

---

## 🏗️ 测试结构

```
backend/
├── src/
│   ├── main/java/
│   └── test/java/
│       └── com/jxkh/
│           ├── service/          # Service 层测试
│           │   ├── UserServiceTest.java
│           │   └── PlanServiceTest.java
│           ├── controller/       # Controller 层测试
│           │   └── PlanControllerIntegrationTest.java
│           ├── repository/       # Repository 层测试
│           │   └── PlanRepositoryTest.java
│           └── util/             # 工具类测试
│               └── JwtUtilTest.java
└── pom.xml
```

---

## 🔧 依赖配置

### pom.xml

```xml
<dependencies>
    <!-- 测试核心依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- MockMvc 测试 Web 层 -->
    <dependency>
        <groupId>org.springframework.restdocs</groupId>
        <artifactId>spring-restdocs-mockmvc</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- TestContainers 集成测试 -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>mysql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## ✅ 单元测试规范

### 1. Service 层测试

**原则**：
- Mock 所有外部依赖（Repository、其他 Service）
- 只测试当前 Service 的业务逻辑
- 覆盖正常流程和异常流程

**示例**：

```java
@ExtendWith(MockitoExtension.class)
class PlanServiceTest {
    
    @Mock
    private PlanRepository planRepository;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private PlanService planService;
    
    @Test
    void should_createPlan_when_validRequest() {
        // Arrange
        CreatePlanRequest request = createValidRequest();
        User user = new User(1L, "张三", 100L);
        
        when(userService.findById(1L)).thenReturn(user);
        when(planRepository.save(any(PerformancePlan.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        PerformancePlan plan = planService.createPlan(request);
        
        // Assert
        assertNotNull(plan.getId());
        assertEquals("Q1绩效计划", plan.getName());
        verify(planRepository).save(any(PerformancePlan.class));
    }
    
    @Test
    void should_throwException_when_userNotFound() {
        // Arrange
        CreatePlanRequest request = createValidRequest();
        when(userService.findById(999L)).thenReturn(null);
        
        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            planService.createPlan(request);
        });
    }
}
```

**命名规范**：
```java
// 格式：should_期望结果_when_条件
@Test
void should_returnUser_when_validUsernameProvided()
@Test
void should_throwException_when_invalidEmailFormat()
@Test
void should_updateStatus_when_planSubmitted()
```

---

### 2. Repository 层测试

**原则**：
- 使用 H2 内存数据库或 TestContainers
- 验证 JPA Query 的正确性
- 测试复杂查询和关联查询

**示例**：

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
class PlanRepositoryTest {
    
    @Autowired
    private PlanRepository planRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    void should_findPlansByUserId() {
        // Arrange
        PerformancePlan plan = new PerformancePlan();
        plan.setUserId(1L);
        plan.setName("Q1计划");
        entityManager.persist(plan);
        entityManager.flush();
        
        // Act
        List<PerformancePlan> plans = planRepository.findByUserId(1L);
        
        // Assert
        assertThat(plans).hasSize(1);
        assertThat(plans.get(0).getName()).isEqualTo("Q1计划");
    }
    
    @Test
    void should_calculateAvgScore() {
        // Arrange
        // 插入测试数据
        
        // Act
        BigDecimal avgScore = planRepository.calculateAvgScore(1L);
        
        // Assert
        assertThat(avgScore).isEqualByComparingTo(new BigDecimal("85.5"));
    }
}
```

---

### 3. Utility 工具类测试

**原则**：
- 纯函数测试，无副作用
- 覆盖边界条件和异常情况
- 无需 Mock

**示例**：

```java
class ScoreCalculatorTest {
    
    @Test
    void should_calculateWeightedScore() {
        // Arrange
        List<IndicatorScore> scores = List.of(
            new IndicatorScore(new BigDecimal("90"), new BigDecimal("60")),
            new IndicatorScore(new BigDecimal("80"), new BigDecimal("40"))
        );
        
        // Act
        BigDecimal totalScore = ScoreCalculator.calculate(scores);
        
        // Assert
        assertThat(totalScore).isEqualByComparingTo(new BigDecimal("86.0"));
    }
    
    @Test
    void should_returnZero_when_emptyList() {
        // Act
        BigDecimal score = ScoreCalculator.calculate(Collections.emptyList());
        
        // Assert
        assertThat(score).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
```

---

## 🔗 集成测试规范

### 1. Controller 层测试

**原则**：
- 使用 `@SpringBootTest` 启动完整上下文
- 使用 MockMvc 模拟 HTTP 请求
- 测试完整的请求-响应流程

**示例**：

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional  // 测试后自动回滚
class PlanControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private PlanRepository planRepository;
    
    @Test
    void should_createPlan_when_validRequest() throws Exception {
        // Arrange
        CreatePlanRequest request = new CreatePlanRequest();
        request.setName("Q1绩效计划");
        request.setUserId(1L);
        
        // Act & Assert
        mockMvc.perform(post("/api/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.planId").exists())
            .andExpect(jsonPath("$.name").value("Q1绩效计划"));
    }
    
    @Test
    void should_return404_when_planNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/plans/999"))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void should_return400_when_invalidRequest() throws Exception {
        // Arrange
        String invalidJson = "{\"name\": \"\"}";  // 名称为空
        
        // Act & Assert
        mockMvc.perform(post("/api/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());
    }
}
```

---

### 2. 数据库集成测试（TestContainers）

**原则**：
- 使用真实的 MySQL 容器
- 测试复杂的 SQL 查询
- 验证事务行为

**示例**：

```java
@Testcontainers
@SpringBootTest
class PlanRepositoryIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
    
    @Autowired
    private PlanRepository planRepository;
    
    @Test
    void should_findPlansWithComplexQuery() {
        // Arrange
        // 插入测试数据
        
        // Act
        List<PlanSummary> summaries = planRepository.findPlanSummaries(1L);
        
        // Assert
        assertThat(summaries).isNotEmpty();
    }
}
```

---

### 3. Redis 集成测试

**示例**：

```java
@SpringBootTest
class CacheServiceIntegrationTest {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private DashboardCacheService cacheService;
    
    @Test
    void should_cacheAndRetrieveData() {
        // Arrange
        OverviewDTO expected = createTestData();
        
        // Act
        cacheService.putOverview(1L, 100L, expected);
        OverviewDTO actual = cacheService.getOverview(1L, 100L);
        
        // Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
    
    @AfterEach
    void cleanup() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }
}
```

---

## ⚡ 性能测试

### JMH 基准测试

**示例**：

```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class ScoreCalculationBenchmark {
    
    private List<IndicatorScore> scores;
    
    @Setup
    public void setup() {
        scores = generateLargeDataset(10000);
    }
    
    @Benchmark
    public BigDecimal benchmarkCalculate() {
        return ScoreCalculator.calculate(scores);
    }
}
```

**运行命令**：
```bash
mvn clean install -Pbenchmark
```

---

## 🎯 测试最佳实践

### 1. AAA 模式

```java
@Test
void example() {
    // Arrange - 准备测试数据
    User user = new User("test@example.com");
    
    // Act - 执行被测方法
    String result = userService.generateToken(user);
    
    // Assert - 验证结果
    assertNotNull(result);
    assertTrue(JwtUtil.isValid(result));
}
```

### 2. 单一职责

```java
// ❌ 错误：一个测试验证多个场景
@Test
void testUserService() {
    createUser();
    updateUser();
    deleteUser();
}

// ✅ 正确：每个测试只验证一个场景
@Test
void should_createUser() { ... }

@Test
void should_updateUser() { ... }

@Test
void should_deleteUser() { ... }
```

### 3. 避免测试私有方法

```java
// ❌ 不要这样做
@Test
void testPrivateMethod() throws Exception {
    Method method = Service.class.getDeclaredMethod("privateMethod");
    method.setAccessible(true);
    method.invoke(service);
}

// ✅ 通过公共方法间接测试
@Test
void should_callPrivateMethodIndirectly() {
    // 调用公共方法，它会内部调用私有方法
    service.publicMethod();
    // 验证最终结果
}
```

### 4. 使用 AssertJ 流式断言

```java
// ❌ 传统断言
assertEquals(3, list.size());
assertEquals("John", list.get(0).getName());

// ✅ AssertJ 流式断言（更清晰）
assertThat(list)
    .hasSize(3)
    .extracting(User::getName)
    .containsExactly("John", "Jane", "Bob");
```

---

## 🚫 常见陷阱

### 1. 过度 Mock

```java
// ❌ Mock 了太多依赖，测试失去意义
@Mock
private ServiceA serviceA;
@Mock
private ServiceB serviceB;
@Mock
private ServiceC serviceC;

// ✅ 只 Mock 外部依赖，保留核心逻辑
@InjectMocks
private CoreService coreService;  // 真实对象
@Mock
private ExternalApi externalApi;  // 外部依赖
```

### 2. 测试间相互依赖

```java
// ❌ 测试 B 依赖测试 A 的结果
@Test
void testA() {
    createUser();
}

@Test
void testB() {
    // 假设用户已存在
    updateUser();
}

// ✅ 每个测试独立 setup
@BeforeEach
void setup() {
    createUser();  // 每个测试都创建自己的数据
}
```

### 3. 硬编码测试数据

```java
// ❌ 硬编码
User user = new User("john@example.com");

// ✅ 使用工厂方法
User user = TestDataFactory.createUser();
```

---

## 📊 测试覆盖率报告

### 生成报告

```bash
# 生成 Jacoco 报告
mvn clean test jacoco:report

# 查看报告
open target/site/jacoco/index.html
```

### 配置质量门禁

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
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
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## 📚 相关文档

- [测试策略总纲](./testing-strategy.md)
- [测试数据管理](./test-data-management.md)
- [CI/CD 集成配置](./ci-cd-integration.md)

---

**文档版本**: V1.0  
**最后更新**: 2026-04-14  
**维护者**: 后端开发团队
