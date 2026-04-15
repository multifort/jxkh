# 后端代码模板规范

本文档提供标准的 Controller、Service、Repository 代码模板，确保代码风格统一。

---

## 1. Controller 标准模板

### 1.1 基础 CRUD Controller

```java
package com.iyunxin.jxkh.module.{module}.controller;

import com.iyunxin.jxkh.common.response.PageResult;
import com.iyunxin.jxkh.common.response.Result;
import com.iyunxin.jxkh.module.{module}.dto.{Entity}CreateRequest;
import com.iyunxin.jxkh.module.{module}.dto.{Entity}UpdateRequest;
import com.iyunxin.jxkh.module.{module}.dto.{Entity}VO;
import com.iyunxin.jxkh.module.{module}.service.{Entity}Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * {Entity中文名称}控制器
 * 
 * @author JXKH Team
 * @since 2026-04-15
 */
@Tag(name = "{Entity中文名称}管理", description = "{Entity中文名称}的增删改查接口")
@RestController
@RequestMapping("/api/v1/{entities}")
@RequiredArgsConstructor
public class {Entity}Controller {

    private final {Entity}Service {entity}Service;

    /**
     * 分页查询{Entity中文名称}列表
     */
    @Operation(summary = "分页查询{Entity中文名称}列表", description = "支持按条件筛选和排序")
    @GetMapping
    public Result<PageResult<{Entity}VO>> list(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<{Entity}VO> page = {entity}Service.list(keyword, pageable);
        return Result.success(PageResult.from(page));
    }

    /**
     * 根据ID查询{Entity中文名称}详情
     */
    @Operation(summary = "查询{Entity中文名称}详情", description = "根据ID获取详细信息")
    @GetMapping("/{id}")
    public Result<{Entity}VO> getById(@PathVariable Long id) {
        {Entity}VO vo = {entity}Service.getById(id);
        return Result.success(vo);
    }

    /**
     * 创建{Entity中文名称}
     */
    @Operation(summary = "创建{Entity中文名称}", description = "创建新的{Entity中文名称}记录")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody {Entity}CreateRequest request) {
        Long id = {entity}Service.create(request);
        return Result.success(id);
    }

    /**
     * 更新{Entity中文名称}
     */
    @Operation(summary = "更新{Entity中文名称}", description = "根据ID更新{Entity中文名称}信息")
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody {Entity}UpdateRequest request) {
        {entity}Service.update(id, request);
        return Result.success();
    }

    /**
     * 删除{Entity中文名称}
     */
    @Operation(summary = "删除{Entity中文名称}", description = "逻辑删除{Entity中文名称}记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        {entity}Service.delete(id);
        return Result.success();
    }
}
```

### 1.2 带业务逻辑的 Controller

```java
/**
 * 提交{Entity中文名称}（示例：提交绩效计划）
 */
@Operation(summary = "提交{Entity中文名称}", description = "提交后进行状态流转")
@PostMapping("/{id}/submit")
public Result<Void> submit(@PathVariable Long id) {
    {entity}Service.submit(id);
    return Result.success();
}

/**
 * 审批{Entity中文名称}（示例：审批绩效计划）
 */
@Operation(summary = "审批{Entity中文名称}", description = "主管审批{Entity中文名称}")
@PostMapping("/{id}/approve")
public Result<Void> approve(
        @PathVariable Long id,
        @RequestParam Boolean approved,
        @RequestParam(required = false) String comment) {
    {entity}Service.approve(id, approved, comment);
    return Result.success();
}
```

---

## 2. Service 标准模板

### 2.1 Service 接口

```java
package com.iyunxin.jxkh.module.{module}.service;

import com.iyunxin.jxkh.module.{module}.dto.{Entity}CreateRequest;
import com.iyunxin.jxkh.module.{module}.dto.{Entity}UpdateRequest;
import com.iyunxin.jxkh.module.{module}.dto.{Entity}VO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * {Entity中文名称}服务接口
 * 
 * @author JXKH Team
 * @since 2026-04-15
 */
public interface {Entity}Service {

    /**
     * 分页查询{Entity中文名称}列表
     */
    Page<{Entity}VO> list(String keyword, Pageable pageable);

    /**
     * 根据ID查询{Entity中文名称}详情
     */
    {Entity}VO getById(Long id);

    /**
     * 创建{Entity中文名称}
     */
    Long create({Entity}CreateRequest request);

    /**
     * 更新{Entity中文名称}
     */
    void update(Long id, {Entity}UpdateRequest request);

    /**
     * 删除{Entity中文名称}
     */
    void delete(Long id);
}
```

### 2.2 Service 实现类

```java
package com.iyunxin.jxkh.module.{module}.service.impl;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.module.{module}.domain.{Entity};
import com.iyunxin.jxkh.module.{module}.dto.{Entity}CreateRequest;
import com.iyunxin.jxkh.module.{module}.dto.{Entity}UpdateRequest;
import com.iyunxin.jxkh.module.{module}.dto.{Entity}VO;
import com.iyunxin.jxkh.module.{module}.repository.{Entity}Repository;
import com.iyunxin.jxkh.module.{module}.service.{Entity}Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {Entity中文名称}服务实现
 * 
 * @author JXKH Team
 * @since 2026-04-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class {Entity}ServiceImpl implements {Entity}Service {

    private final {Entity}Repository {entity}Repository;

    @Override
    public Page<{Entity}VO> list(String keyword, Pageable pageable) {
        log.debug("查询{Entity中文名称}列表, keyword: {}", keyword);
        
        Page<{Entity}> entities;
        if (keyword != null && !keyword.isEmpty()) {
            entities = {entity}Repository.findByKeyword(keyword, pageable);
        } else {
            entities = {entity}Repository.findAll(pageable);
        }
        
        return entities.map(this::convertToVO);
    }

    @Override
    public {Entity}VO getById(Long id) {
        log.debug("查询{Entity中文名称}详情, id: {}", id);
        
        {Entity} entity = {entity}Repository.findById(id)
                .orElseThrow(() -> new BusinessException("{ENTITY}_NOT_FOUND", "{Entity中文名称}不存在"));
        
        return convertToVO(entity);
    }

    @Override
    @Transactional
    public Long create({Entity}CreateRequest request) {
        log.info("创建{Entity中文名称}, name: {}", request.getName());
        
        // 业务校验
        validateCreateRequest(request);
        
        // 转换为实体
        {Entity} entity = convertToEntity(request);
        
        // 保存
        {Entity} saved = {entity}Repository.save(entity);
        
        log.info("{Entity中文名称}创建成功, id: {}", saved.getId());
        return saved.getId();
    }

    @Override
    @Transactional
    public void update(Long id, {Entity}UpdateRequest request) {
        log.info("更新{Entity中文名称}, id: {}", id);
        
        // 查询实体
        {Entity} entity = {entity}Repository.findById(id)
                .orElseThrow(() -> new BusinessException("{ENTITY}_NOT_FOUND", "{Entity中文名称}不存在"));
        
        // 业务校验
        validateUpdateRequest(request);
        
        // 更新字段
        updateEntity(entity, request);
        
        // 保存
        {entity}Repository.save(entity);
        
        log.info("{Entity中文名称}更新成功, id: {}", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("删除{Entity中文名称}, id: {}", id);
        
        {Entity} entity = {entity}Repository.findById(id)
                .orElseThrow(() -> new BusinessException("{ENTITY}_NOT_FOUND", "{Entity中文名称}不存在"));
        
        // 逻辑删除
        entity.setDeleted(true);
        {entity}Repository.save(entity);
        
        log.info("{Entity中文名称}删除成功, id: {}", id);
    }

    // ==================== 私有方法 ====================

    /**
     * 校验创建请求
     */
    private void validateCreateRequest({Entity}CreateRequest request) {
        // TODO: 添加业务校验逻辑
    }

    /**
     * 校验更新请求
     */
    private void validateUpdateRequest({Entity}UpdateRequest request) {
        // TODO: 添加业务校验逻辑
    }

    /**
     * 转换请求为实体
     */
    private {Entity} convertToEntity({Entity}CreateRequest request) {
        {Entity} entity = new {Entity}();
        // TODO: 设置字段
        return entity;
    }

    /**
     * 更新实体字段
     */
    private void updateEntity({Entity} entity, {Entity}UpdateRequest request) {
        // TODO: 更新字段
    }

    /**
     * 转换实体为VO
     */
    private {Entity}VO convertToVO({Entity} entity) {
        {Entity}VO vo = new {Entity}VO();
        // TODO: 设置字段
        return vo;
    }
}
```

---

## 3. Repository 标准模板

```java
package com.iyunxin.jxkh.module.{module}.repository;

import com.iyunxin.jxkh.module.{module}.domain.{Entity};
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * {Entity中文名称}数据访问层
 * 
 * @author JXKH Team
 * @since 2026-04-15
 */
@Repository
public interface {Entity}Repository extends JpaRepository<{Entity}, Long> {

    /**
     * 根据关键字搜索
     */
    @Query("SELECT e FROM {Entity} e WHERE e.name LIKE %:keyword% OR e.code LIKE %:keyword%")
    Page<{Entity}> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据编码查询
     */
    Optional<{Entity}> findByCode(String code);

    /**
     * 检查编码是否存在
     */
    boolean existsByCode(String code);
}
```

---

## 4. Domain Entity 标准模板

```java
package com.iyunxin.jxkh.module.{module}.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * {Entity中文名称}实体
 * 
 * @author JXKH Team
 * @since 2026-04-15
 */
@Data
@Entity
@Table(name = "{table_name}")
@EntityListeners(AuditingEntityListener.class)
public class {Entity} {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Integer sort = 0;

    // 审计字段
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private Long createdBy;

    @LastModifiedBy
    private Long updatedBy;

    @Column(nullable = false)
    private Boolean isDeleted = false;
}
```

---

## 5. DTO 标准模板

### 5.1 Create Request

```java
package com.iyunxin.jxkh.module.{module}.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * {Entity中文名称}创建请求
 * 
 * @author JXKH Team
 * @since 2026-04-15
 */
@Data
@Schema(description = "{Entity中文名称}创建请求")
public class {Entity}CreateRequest {

    @Schema(description = "名称", example = "测试{Entity}")
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "编码", example = "TEST_CODE")
    @NotBlank(message = "编码不能为空")
    private String code;

    @Schema(description = "描述", example = "这是测试{Entity}")
    private String description;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled = true;

    @Schema(description = "排序", example = "0")
    private Integer sort = 0;
}
```

### 5.2 Update Request

```java
package com.iyunxin.jxkh.module.{module}.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * {Entity中文名称}更新请求
 * 
 * @author JXKH Team
 * @since 2026-04-15
 */
@Data
@Schema(description = "{Entity中文名称}更新请求")
public class {Entity}UpdateRequest {

    @Schema(description = "名称", example = "测试{Entity}")
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "描述", example = "这是测试{Entity}")
    private String description;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    @Schema(description = "排序", example = "0")
    private Integer sort;
}
```

### 5.3 VO (View Object)

```java
package com.iyunxin.jxkh.module.{module}.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * {Entity中文名称}视图对象
 * 
 * @author JXKH Team
 * @since 2026-04-15
 */
@Data
@Schema(description = "{Entity中文名称}视图对象")
public class {Entity}VO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "名称", example = "测试{Entity}")
    private String name;

    @Schema(description = "编码", example = "TEST_CODE")
    private String code;

    @Schema(description = "描述", example = "这是测试{Entity}")
    private String description;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "排序", example = "0")
    private Integer sort;

    @Schema(description = "创建时间", example = "2026-04-15T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-04-15T10:00:00")
    private LocalDateTime updatedAt;
}
```

---

## 6. 统一响应体模板

### 6.1 Result 通用响应

```java
package com.iyunxin.jxkh.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应体
 * 
 * @author JXKH Team
 * @since 2026-04-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一响应体")
public class Result<T> {

    @Schema(description = "响应码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "success")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 成功响应（有数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
```

### 6.2 PageResult 分页响应

```java
package com.iyunxin.jxkh.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页响应体
 * 
 * @author JXKH Team
 * @since 2026-04-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应体")
public class PageResult<T> {

    @Schema(description = "数据列表")
    private List<T> content;

    @Schema(description = "总记录数", example = "100")
    private Long totalElements;

    @Schema(description = "总页数", example = "10")
    private Integer totalPages;

    @Schema(description = "当前页码", example = "1")
    private Integer number;

    @Schema(description = "每页大小", example = "10")
    private Integer size;

    /**
     * 从 Spring Data Page 转换
     */
    public static <T> PageResult<T> from(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setContent(page.getContent());
        result.setTotalElements(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        result.setNumber(page.getNumber());
        result.setSize(page.getSize());
        return result;
    }
}
```

---

## 7. 统一异常处理模板

```java
package com.iyunxin.jxkh.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务异常
 * 
 * @author JXKH Team
 * @since 2026-04-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
```

```java
package com.iyunxin.jxkh.middleware.exception;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 
 * @author JXKH Team
 * @since 2026-04-15
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        log.warn("参数校验异常: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数绑定失败");
        log.warn("参数绑定异常: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统内部错误");
    }
}
```

---

## 8. 使用示例

以"组织管理"为例，完整的项目结构：

```
module/org/
├── controller/
│   └── OrgController.java          # 使用模板 1.1
├── service/
│   ├── OrgService.java             # 使用模板 2.1
│   └── impl/
│       └── OrgServiceImpl.java     # 使用模板 2.2
├── repository/
│   └── OrgRepository.java          # 使用模板 3
├── domain/
│   └── Org.java                    # 使用模板 4
└── dto/
    ├── OrgCreateRequest.java       # 使用模板 5.1
    ├── OrgUpdateRequest.java       # 使用模板 5.2
    └── OrgVO.java                  # 使用模板 5.3
```

---

**文档版本**: V1.0  
**最后更新**: 2026-04-15  
**维护者**: JXKH Team
