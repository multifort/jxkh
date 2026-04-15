package com.iyunxin.jxkh.module.org.repository;

import com.iyunxin.jxkh.module.org.domain.Org;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 组织 Repository
 */
@Repository
public interface OrgRepository extends JpaRepository<Org, Long> {

    Optional<Org> findByCode(String code);

    List<Org> findByParentId(Long parentId);

    List<Org> findByParentIdAndIsDeletedFalse(Long parentId);

    List<Org> findByIsDeletedFalseOrderBySort();

    @Query("SELECT o FROM Org o WHERE o.enabled = true AND o.isDeleted = false")
    List<Org> findAllActive();
}
