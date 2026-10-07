package com.innospots.nexus.console.role.operator;

import java.util.List;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;
import com.innospots.nexus.console.role.domain.request.RoleBindingAddRequest;
import com.innospots.nexus.console.role.domain.request.RoleBindingPageRequest;
import com.innospots.nexus.console.role.domain.vo.RoleBindingVo;
import com.innospots.nexus.console.role.converter.RoleConverter;
import com.innospots.nexus.console.role.status.RoleStatusCode;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipGuard;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * 角色与主体绑定关系维护，可供子类扩展校验或同步逻辑。
 */
@RequiredArgsConstructor
public class RoleBindingOperator {

    private final RoleOperator roleOperator;
    private final RoleBindingDao roleBindingDao;
    private final RoleConverter roleConverter;

    public PageResult<RoleBindingVo> pageRoleBindings(String roleId, RoleBindingPageRequest request) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireWorkspaceScope();
        roleOperator.requireEntity(roleId, ownership);
        RoleBindingPageRequest pageRequest = request == null ? new RoleBindingPageRequest() : request;
        LambdaQueryWrapper<RoleBindingEntity> query = ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<RoleBindingEntity>(), ownership)
                .eq(RoleBindingEntity::getRoleId, roleId)
                .orderByDesc(RoleBindingEntity::getCreatedAt);
        if (pageRequest.subjectType() != null) {
            query.eq(RoleBindingEntity::getSubjectType, pageRequest.subjectType().name());
        }
        if (RoleOperator.hasText(pageRequest.input())) {
            query.like(RoleBindingEntity::getSubjectId, pageRequest.input().trim());
        }
        IPage<RoleBindingEntity> selectedPage = roleBindingDao.selectPage(
                new Page<>(pageRequest.pageNo(), pageRequest.pageSize()),
                query);
        List<RoleBindingVo> records = selectedPage.getRecords().stream()
                .map(roleConverter::toBindingVo)
                .toList();
        return PageResult.of(records, pageRequest.pageNo(), pageRequest.pageSize(), selectedPage.getTotal());
    }

    @Transactional
    public void addRoleBindings(String roleId, RoleBindingAddRequest request) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireWorkspaceScope();
        Objects.requireNonNull(request, "request");
        roleOperator.requireEntity(roleId, ownership);
        if (request.subjectType() == null || request.subjectIds().isEmpty()) {
            return;
        }
        for (String subjectId : request.subjectIds()) {
            if (subjectId == null || subjectId.isBlank()) {
                continue;
            }
            if (bindingExists(roleId, request.subjectType(), subjectId, ownership)) {
                continue;
            }
            RoleBindingEntity binding = new RoleBindingEntity();
            ConsoleOwnershipScope.stamp(binding, ownership);
            binding.setRoleId(roleId);
            binding.setSubjectType(request.subjectType().name());
            binding.setSubjectId(subjectId);
            roleBindingDao.insert(binding);
        }
    }

    @Transactional
    public void removeRoleBinding(String roleId, String bindingId) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireWorkspaceScope();
        roleOperator.requireEntity(roleId, ownership);
        RoleBindingEntity binding = roleBindingDao.selectOne(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<RoleBindingEntity>()
                        .eq(RoleBindingEntity::getBindingId, bindingId)
                        .eq(RoleBindingEntity::getRoleId, roleId),
                ownership));
        if (binding == null) {
            throw NexusException.build(RoleStatusCode.ROLE_BINDING_NOT_FOUND);
        }
        roleBindingDao.deleteById(bindingId);
    }

    protected boolean bindingExists(
            String roleId,
            RoleBindingSubjectType subjectType,
            String subjectId,
            ConsoleOwnership ownership
    ) {
        return roleBindingDao.selectCount(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<RoleBindingEntity>()
                        .eq(RoleBindingEntity::getRoleId, roleId)
                        .eq(RoleBindingEntity::getSubjectType, subjectType.name())
                        .eq(RoleBindingEntity::getSubjectId, subjectId),
                ownership)) > 0;
    }
}
