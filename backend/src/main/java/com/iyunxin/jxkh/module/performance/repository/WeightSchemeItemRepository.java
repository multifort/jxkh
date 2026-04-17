package com.iyunxin.jxkh.module.performance.repository;

import com.iyunxin.jxkh.module.performance.domain.WeightSchemeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 权重方案明细Repository
 */
@Repository
public interface WeightSchemeItemRepository extends JpaRepository<WeightSchemeItem, Long> {
    
    /**
     * 根据方案ID查找所有明细
     */
    List<WeightSchemeItem> findBySchemeIdOrderBySortOrderAsc(Long schemeId);
    
    /**
     * 删除方案的所有明细
     */
    @Modifying
    @Query("DELETE FROM WeightSchemeItem w WHERE w.schemeId = :schemeId")
    void deleteBySchemeId(@Param("schemeId") Long schemeId);
    
    /**
     * 计算方案的权重总和
     */
    @Query("SELECT COALESCE(SUM(w.weight), 0) FROM WeightSchemeItem w WHERE w.schemeId = :schemeId")
    java.math.BigDecimal sumWeightBySchemeId(@Param("schemeId") Long schemeId);
}
