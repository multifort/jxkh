package com.iyunxin.jxkh.module.org.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.module.org.domain.Org;
import com.iyunxin.jxkh.module.org.service.OrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织管理控制器
 */
@Tag(name = "组织管理", description = "组织树CRUD操作")
@RestController
@RequestMapping("/api/v1/orgs")
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    @Operation(summary = "获取组织树")
    @GetMapping("/tree")
    public ApiResponse<List<OrgService.OrgTreeNode>> getOrgTree() {
        return ApiResponse.success(orgService.getOrgTree());
    }

    @Operation(summary = "获取所有活跃组织")
    @GetMapping
    public ApiResponse<List<Org>> getAllOrgs() {
        return ApiResponse.success(orgService.getAllActiveOrgs());
    }

    @Operation(summary = "获取组织详情")
    @GetMapping("/{id}")
    public ApiResponse<Org> getOrgById(@PathVariable Long id) {
        return ApiResponse.success(orgService.getOrgById(id));
    }

    @Operation(summary = "创建组织")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Org> createOrg(@RequestBody Org org) {
        return ApiResponse.success(orgService.createOrg(org));
    }

    @Operation(summary = "更新组织")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Org> updateOrg(@PathVariable Long id, @RequestBody Org org) {
        return ApiResponse.success(orgService.updateOrg(id, org));
    }

    @Operation(summary = "删除组织")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteOrg(@PathVariable Long id) {
        orgService.deleteOrg(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取子组织ID列表")
    @GetMapping("/{id}/children-ids")
    public ApiResponse<List<Long>> getSubOrgIds(@PathVariable Long id) {
        return ApiResponse.success(orgService.getSubOrgIds(id));
    }
}
