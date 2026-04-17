package com.iyunxin.jxkh.module.performance.repository;

import com.iyunxin.jxkh.module.performance.domain.IndicatorCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 指标分类Repository
 */
@Repository
public interface IndicatorCategoryRepository extends JpaRepository<IndicatorCategory, Long>, JpaSpecificationExecutor<IndicatorCategory> {
    
    /**
     * 根据编码和组织ID查找
     */
    Optional<IndicatorCategory> findByCodeAndOrgId(String code, Long orgId);
    
    /**
     * 查找父分类下的所有子分类
     */
    List<IndicatorCategory> findByParentIdAndIsDeletedFalse(Long parentId);
    
    /**
     * 检查是否有子分类
     */
    boolean existsByParentIdAndIsDeletedFalse(Long parentId);
}
