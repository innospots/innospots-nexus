package com.innospots.nexus.platform.role.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;
import com.innospots.nexus.console.role.domain.request.RoleBindingAddRequest;
import com.innospots.nexus.console.role.domain.request.RoleBindingPageRequest;
import com.innospots.nexus.console.role.domain.vo.RoleBindingVo;
import com.innospots.nexus.console.role.operator.RoleBindingOperator;
import com.innospots.nexus.console.role.operator.RoleOperator;
import com.innospots.nexus.platform.scope.PlatformConsoleSession;
import com.innospots.nexus.console.support.MutationLog;
import com.innospots.nexus.console.user.admin.domain.entity.AdminUserEntity;
import com.innospots.nexus.console.user.admin.operator.AdminUserOperator;

/**
 * 运营平台角色绑定服务：PLATFORM 会话与归属固化，委托 {@link RoleBindingOperator}。
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformRoleBindingService {

    private static final int USER_SEARCH_ID_CAP = 500;

    private final RoleBindingOperator roleBindingOperator;
    private final AdminUserOperator platformUserOperator;

    public PageResult<RoleBindingVo> pageRoleBindings(String roleId, RoleBindingPageRequest request) {
        Checks.notBlank(roleId, "roleId");
        RoleBindingPageRequest pageRequest = request == null ? new RoleBindingPageRequest() : request;
        List<String> subjectIdFilter = resolveUserSubjectIdFilter(pageRequest);
        PageResult<RoleBindingVo> page = roleBindingOperator.pageRoleBindings(
                roleId,
                pageRequest,
                PlatformConsoleSession.requireOwnership(),
                subjectIdFilter);
        return enrichUserBindings(page);
    }

    public void addRoleBindings(String roleId, RoleBindingAddRequest request) {
        Checks.notBlank(roleId, "roleId");
        Objects.requireNonNull(request, "request");
        roleBindingOperator.addRoleBindings(
                roleId, request, PlatformConsoleSession.requireOwnership());
        MutationLog.info(
                log,
                "role bindings added roleId={} subjectType={} subjectCount={}",
                roleId,
                request.subjectType(),
                request.subjectIds().size());
    }

    public void removeRoleBinding(String roleId, String bindingId) {
        Checks.notBlank(roleId, "roleId");
        Checks.notBlank(bindingId, "bindingId");
        roleBindingOperator.removeRoleBinding(
                roleId, bindingId, PlatformConsoleSession.requireOwnership());
        MutationLog.info(log, "role binding removed roleId={} bindingId={}", roleId, bindingId);
    }

    private List<String> resolveUserSubjectIdFilter(RoleBindingPageRequest pageRequest) {
        if (pageRequest.subjectType() != RoleBindingSubjectType.USER) {
            return null;
        }
        if (!RoleOperator.hasText(pageRequest.input())) {
            return null;
        }
        return platformUserOperator.findPlatformUserIdsByKeyword(pageRequest.input(), USER_SEARCH_ID_CAP);
    }

    private PageResult<RoleBindingVo> enrichUserBindings(PageResult<RoleBindingVo> page) {
        if (page == null || page.records() == null || page.records().isEmpty()) {
            return page;
        }
        List<String> userIds = page.records().stream()
                .filter(row -> row.subjectType() == RoleBindingSubjectType.USER)
                .map(RoleBindingVo::subjectId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return page;
        }
        Map<String, AdminUserEntity> users = platformUserOperator.findByIds(userIds);
        List<RoleBindingVo> enriched = page.records().stream()
                .map(row -> enrichRow(row, users))
                .toList();
        return PageResult.of(enriched, page.pageNo(), page.pageSize(), page.total());
    }

    private static RoleBindingVo enrichRow(RoleBindingVo row, Map<String, AdminUserEntity> users) {
        if (row.subjectType() != RoleBindingSubjectType.USER) {
            return row;
        }
        AdminUserEntity user = users.get(row.subjectId());
        if (user == null) {
            return row;
        }
        return new RoleBindingVo(
                row.bindingId(),
                row.roleId(),
                row.subjectType(),
                row.subjectId(),
                user.getLoginName(),
                user.getDisplayName(),
                user.getEmail(),
                row.createdAt());
    }
}
