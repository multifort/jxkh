package com.iyunxin.jxkh.module.performance.repository;

import com.iyunxin.jxkh.module.performance.domain.WeightScheme;
import com.iyunxin.jxkh.module.performance.domain.WeightSchemeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 权重方案Repository
 */
@Repository
public interface WeightSchemeRepository extends JpaRepository<WeightScheme, Long>, JpaSpecificationExecutor<WeightScheme> {
    
    /**
     * 根据编码、版本和组织ID查找
     */
    Optional<WeightScheme> findByCodeAndVersionAndOrgId(String code, Integer version, Long orgId);
    
    /**
     * 查找某个编码的最新版本
     */
    Optional<WeightScheme> findTopByCodeAndOrgIdOrderByVersionDesc(String code, Long orgId);
    
    /**
     * 根据状态查找
     */
    List<WeightScheme> findByStatusAndIsDeletedFalse(WeightSchemeStatus status);
    
    /**
     * 根据周期ID查找
     */
    List<WeightScheme> findByCycleIdAndIsDeletedFalse(Long cycleId);
}
