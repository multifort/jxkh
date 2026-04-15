package com.iyunxin.jxkh.module.org.service;

import com.iyunxin.jxkh.module.org.domain.Org;
import com.iyunxin.jxkh.module.org.repository.OrgRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrgService {

    private final OrgRepository orgRepository;

    /**
     * 获取组织树
     */
    public List<OrgTreeNode> getOrgTree() {
        List<Org> allOrgs = orgRepository.findByIsDeletedFalseOrderBySort();
        return buildTree(allOrgs, null);
    }

    /**
     * 递归构建组织树
     */
    private List<OrgTreeNode> buildTree(List<Org> allOrgs, Long parentId) {
        return allOrgs.stream()
                .filter(org -> parentId == null ? org.getParentId() == null : org.getParentId().equals(parentId))
                .map(org -> {
                    OrgTreeNode node = new OrgTreeNode(org);
                    node.setChildren(buildTree(allOrgs, org.getId()));
                    return node;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取所有活跃组织（扁平列表）
     */
    public List<Org> getAllActiveOrgs() {
        List<Org> allOrgs = orgRepository.findByIsDeletedFalseOrderBySort();
        return allOrgs.stream()
                .filter(org -> org.getEnabled() != null && org.getEnabled())
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取组织
     */
    public Org getOrgById(Long orgId) {
        return orgRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("组织不存在"));
    }

    /**
     * 创建组织
     */
    @Transactional
    public Org createOrg(Org org) {
        // 如果有父组织，检查父组织是否存在
        if (org.getParentId() != null) {
            getOrgById(org.getParentId());
        }
        
        // 检查 code 是否已存在
        if (org.getCode() != null && orgRepository.findByCode(org.getCode()).isPresent()) {
            throw new RuntimeException("组织代码已存在");
        }
        
        return orgRepository.save(org);
    }

    /**
     * 更新组织
     */
    @Transactional
    public Org updateOrg(Long orgId, Org org) {
        Org existing = getOrgById(orgId);
        
        existing.setName(org.getName());
        existing.setCode(org.getCode());
        existing.setOrgType(org.getOrgType());
        existing.setDescription(org.getDescription());
        existing.setSort(org.getSort());
        existing.setParentId(org.getParentId());
        
        return orgRepository.save(existing);
    }

    /**
     * 删除组织（逻辑删除）
     */
    @Transactional
    public void deleteOrg(Long orgId) {
        Org org = getOrgById(orgId);
        
        // 检查是否有子组织
        List<Org> children = orgRepository.findByParentId(orgId);
        if (!children.isEmpty()) {
            throw new RuntimeException("该组织下还有子组织，无法删除");
        }
        
        org.setIsDeleted(true);
        orgRepository.save(org);
        log.info("组织已删除: {}", orgId);
    }

    /**
     * 获取子组织ID列表（包括自己）
     */
    public List<Long> getSubOrgIds(Long orgId) {
        List<Org> allOrgs = orgRepository.findByIsDeletedFalseOrderBySort();
        List<Long> result = new ArrayList<>();
        collectSubOrgIds(allOrgs, orgId, result);
        result.add(orgId);
        return result;
    }

    /**
     * 递归收集子组织ID
     */
    private void collectSubOrgIds(List<Org> allOrgs, Long parentId, List<Long> result) {
        allOrgs.stream()
                .filter(org -> org.getParentId() != null && org.getParentId().equals(parentId))
                .forEach(org -> {
                    result.add(org.getId());
                    collectSubOrgIds(allOrgs, org.getId(), result);
                });
    }

    /**
     * 组织树节点DTO
     */
    public static class OrgTreeNode extends Org {
        private List<OrgTreeNode> children;

        public OrgTreeNode(Org org) {
            this.setId(org.getId());
            this.setParentId(org.getParentId());
            this.setName(org.getName());
            this.setCode(org.getCode());
            this.setOrgType(org.getOrgType());
            this.setDescription(org.getDescription());
            this.setSort(org.getSort());
            this.setEnabled(org.getEnabled());
            this.setIsDeleted(org.getIsDeleted());
            this.setCreatedAt(org.getCreatedAt());
            this.setUpdatedAt(org.getUpdatedAt());
            this.children = new ArrayList<>();
        }

        public List<OrgTreeNode> getChildren() {
            return children;
        }

        public void setChildren(List<OrgTreeNode> children) {
            this.children = children;
        }
    }
}

