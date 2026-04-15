# 数据看板查询流程序列图

## 📋 业务场景

描述管理层查看绩效看板的流程，包括数据聚合、缓存策略、权限控制。

## 👥 参与者定义

| 参与者 | 缩写 | 说明 |
|--------|------|------|
| 管理者 | Executive | 查看者（部门经理/高管） |
| 前端应用 | FE | React 前端应用（含图表组件） |
| 看板控制器 | DashboardController | API 端点 |
| 分析服务 | AnalyticsService | 数据分析业务逻辑 |
| 缓存服务 | CacheService | Redis 缓存 |
| 数据库 | DB | MySQL / ClickHouse |

---

## 🔄 主流程：查看绩效看板

```mermaid
sequenceDiagram
    autonumber
    participant Exec as 管理者
    participant FE as 前端应用
    participant DC as DashboardController
    participant AS as AnalyticsService
    participant Cache as Redis缓存
    participant DB as 数据库

    Note over Exec,DB: 阶段1: 选择筛选条件
    Exec->>FE: 进入"绩效看板"页面
    activate FE
    
    FE->>Exec: 显示筛选器<br/>（周期、部门、指标类型）
    
    Exec->>FE: 选择"2026年Q1"、"技术部"
    FE->>FE: 构建查询参数
    
    Note over FE,Cache: 阶段2: 检查缓存
    FE->>DC: GET /api/dashboard/overview?cycleId=1&orgId=100
    activate DC
    
    DC->>AS: getOverviewData(cycleId, orgId)
    activate AS
    
    AS->>Cache: GET dashboard:overview:{cycleId}:{orgId}
    activate Cache
    
    alt 缓存命中
        Cache-->>AS: 返回缓存的 JSON 数据
        deactivate Cache
        
        AS-->>DC: 返回 cached data
        Note right of AS: X-Cache: HIT
    else 缓存未命中
        Cache-->>AS: null
        deactivate Cache
        
        Note over AS,DB: 阶段3: 查询聚合数据
        AS->>DB: SELECT COUNT(*) FROM performance_plans<br/>WHERE cycle_id = ? AND org_id = ?
        activate DB
        DB-->>AS: totalPlans
        deactivate DB
        
        AS->>DB: SELECT AVG(total_score) FROM performance_plans<br/>WHERE cycle_id = ? AND status = 'EVALUATED'
        activate DB
        DB-->>AS: avgScore
        deactivate DB
        
        AS->>DB: SELECT final_level, COUNT(*) FROM performance_plans<br/>WHERE cycle_id = ? AND org_id = ?<br/>GROUP BY final_level
        activate DB
        DB-->>AS: levelDistribution
        deactivate DB
        
        AS->>DB: SELECT u.department, AVG(pp.total_score)<br/>FROM performance_plans pp<br/>JOIN users u ON pp.user_id = u.id<br/>WHERE pp.cycle_id = ?<br/>GROUP BY u.department
        activate DB
        DB-->>AS: deptRanking
        deactivate DB
        
        Note over AS: 组装响应数据
        AS->>AS: build OverviewDTO
        
        Note over AS,Cache: 写入缓存
        AS->>Cache: SET dashboard:overview:{cycleId}:{orgId}<br/>= jsonData EX 3600
        activate Cache
        Cache-->>AS: OK
        deactivate Cache
        Note right of AS: TTL = 1小时
        
        AS-->>DC: 返回 OverviewDTO
        Note right of AS: X-Cache: MISS
    end
    deactivate AS
    
    DC-->>FE: 200 {summary, distribution, ranking}
    deactivate DC
    
    Note over FE: 阶段4: 渲染图表
    FE->>FE: 解析响应数据
    
    FE->>FE: 渲染概览卡片
    Note right of FE: ECharts 柱状图/饼图
    
    FE->>FE: 渲染等级分布饼图
    Note right of FE: A/B/C/D 占比
    
    FE->>FE: 渲染部门排名柱状图
    Note right of FE: 横向柱状图
    
    FE->>Exec: 显示完整看板
    deactivate FE
    
    Note over Exec,DB: 阶段5: 下钻查看详情
    Exec->>FE: 点击"技术部"柱状图
    FE->>FE: 提取部门ID
    
    FE->>DC: GET /api/dashboard/dept-detail?deptId=100&cycleId=1
    activate DC
    DC->>AS: getDeptDetail(deptId, cycleId)
    activate AS
    
    AS->>Cache: GET dashboard:dept:{deptId}:{cycleId}
    activate Cache
    
    alt 缓存命中
        Cache-->>AS: 返回缓存数据
        deactivate Cache
    else 缓存未命中
        Cache-->>AS: null
        deactivate Cache
        
        AS->>DB: SELECT u.name, pp.total_score, pp.final_level<br/>FROM performance_plans pp<br/>JOIN users u ON pp.user_id = u.id<br/>WHERE u.department_id = ? AND pp.cycle_id = ?<br/>ORDER BY pp.total_score DESC
        activate DB
        DB-->>AS: 返回员工列表
        deactivate DB
        
        AS->>Cache: SET dashboard:dept:{deptId}:{cycleId}<br/>= jsonData EX 3600
        activate Cache
        Cache-->>AS: OK
        deactivate Cache
    end
    deactivate AS
    
    DC-->>FE: 200 {employees}
    deactivate DC
    
    FE->>FE: 渲染员工明细表格
    FE->>Exec: 显示技术部所有员工的绩效详情
```

---

## 🔀 异常流程：大数据量优化

```mermaid
sequenceDiagram
    autonumber
    participant AS as AnalyticsService
    participant DB as MySQL
    participant CH as ClickHouse

    Note over AS: 场景: 全公司年度数据查询<br/>数据量 > 10万条
    
    AS->>DB: SELECT ... FROM performance_plans<br/>WHERE cycle_id IN (SELECT id FROM cycles WHERE year = 2025)
    activate DB
    
    alt 查询超时 (> 5秒)
        DB--xAS: Query timeout
        deactivate DB
        
        Note over AS,CH: 降级到 ClickHouse
        AS->>CH: SELECT ... FROM perf_plans_2025<br/>PREWHERE ... SETTINGS max_execution_time = 10
        activate CH
        Note right of CH: ClickHouse 列式存储<br/>适合大规模聚合查询
        CH-->>AS: 返回聚合结果
        deactivate CH
        
        AS->>AS: 记录慢查询日志
    else 查询成功
        DB-->>AS: 返回结果
        deactivate DB
    end
```

---

## 💡 技术实现要点

### 缓存策略

**多级缓存设计**：
```java
@Service
public class DashboardCacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private CaffeineCache localCache; // L1 本地缓存
    
    public OverviewDTO getOverview(Long cycleId, Long orgId) {
        String cacheKey = String.format("dashboard:overview:%d:%d", cycleId, orgId);
        
        // L1: 本地缓存 (TTL 5分钟)
        OverviewDTO cached = localCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // L2: Redis 缓存 (TTL 1小时)
        cached = (OverviewDTO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            localCache.put(cacheKey, cached); // 回填 L1
            return cached;
        }
        
        return null; // 缓存未命中，查询数据库
    }
    
    public void invalidateCache(Long cycleId, Long orgId) {
        String pattern = String.format("dashboard:*:%d:%d", cycleId, orgId);
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        localCache.invalidateAll();
    }
}
```

### 数据分析服务

```java
@Service
public class AnalyticsService {
    
    @Autowired
    private PlanRepository planRepository;
    
    @Autowired
    private DashboardCacheService cacheService;
    
    public OverviewDTO getOverviewData(Long cycleId, Long orgId) {
        // 检查缓存
        OverviewDTO cached = cacheService.getOverview(cycleId, orgId);
        if (cached != null) {
            return cached;
        }
        
        // 查询数据库
        long totalPlans = planRepository.countByCycleAndOrg(cycleId, orgId);
        BigDecimal avgScore = planRepository.avgScoreByCycleAndOrg(cycleId, orgId);
        Map<PerformanceLevel, Long> distribution = 
            planRepository.levelDistribution(cycleId, orgId);
        List<DeptRanking> deptRanking = 
            planRepository.deptRanking(cycleId);
        
        // 组装 DTO
        OverviewDTO overview = new OverviewDTO();
        overview.setTotalPlans(totalPlans);
        overview.setAvgScore(avgScore);
        overview.setLevelDistribution(distribution);
        overview.setDeptRanking(deptRanking);
        
        // 写入缓存
        cacheService.putOverview(cycleId, orgId, overview);
        
        return overview;
    }
}
```

### 前端图表组件

```tsx
import ReactECharts from 'echarts-for-react';

const LevelDistributionChart = ({ data }) => {
  const option = {
    title: { text: '绩效等级分布' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '60%',
      data: [
        { value: data.A, name: 'A级 (优秀)', itemStyle: { color: '#52c41a' } },
        { value: data.B, name: 'B级 (良好)', itemStyle: { color: '#1890ff' } },
        { value: data.C, name: 'C级 (合格)', itemStyle: { color: '#faad14' } },
        { value: data.D, name: 'D级 (待改进)', itemStyle: { color: '#f5222d' } },
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)',
        },
      },
    }],
  };
  
  return <ReactECharts option={option} style={{ height: '400px' }} />;
};
```

---

## 📊 性能优化建议

1. **预计算聚合数据**
   - 使用定时任务每小时预计算看板数据
   - 存储到汇总表或物化视图
   - 查询时直接读取，无需实时聚合

2. **分页加载**
   - 员工明细列表采用虚拟滚动
   - 每次加载 50 条，滚动到底部自动加载

3. **ClickHouse 加速**
   - 历史数据同步到 ClickHouse
   - 复杂聚合查询走 ClickHouse
   - MySQL 仅保留最近 1 年热数据

4. **前端防抖**
   - 筛选器变化使用防抖（debounce 500ms）
   - 避免频繁请求

---

## 🔗 相关文档

- [API 接口设计 - 数据分析](../../api/api-design.md#11-数据分析接口)
- [领域模型设计 - 分析域](../domain-model-detail.md#39-分析域analytics)

---

**文档版本**: V1.0  
**最后更新**: 2026-04-14  
**维护者**: 架构团队
