package com.iyunxin.jxkh.module.performance.repository;

import com.iyunxin.jxkh.module.performance.domain.Indicator;
import com.iyunxin.jxkh.module.performance.domain.IndicatorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 指标Repository
 */
@Repository
public interface IndicatorRepository extends JpaRepository<Indicator, Long>, JpaSpecificationExecutor<Indicator> {
    
    /**
     * 根据编码和组织ID查找
     */
    Optional<Indicator> findByCodeAndOrgId(String code, Long orgId);
    
    /**
     * 根据分类ID查找
     */
    List<Indicator> findByCategoryIdAndIsDeletedFalse(Long categoryId);
    
    /**
     * 检查分类下是否有指标
     */
    boolean existsByCategoryIdAndIsDeletedFalse(Long categoryId);
    
    /**
     * 根据状态查找
     */
    List<Indicator> findByStatusAndIsDeletedFalse(IndicatorStatus status);
}
