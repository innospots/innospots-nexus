package com.innospots.nexus.console.bootstrap.service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.transaction.Transactional;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest.RoleDefinition;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 启动引导第 2 阶段：按配置创建或更新角色定义。
 *
 * <p><b>为什么独立于授权阶段：</b>授权行必须挂在已存在的角色上，
 * 且授权校验依赖第 1 阶段已建立的权限目录。拆开后每一阶段只有一个失败原因，
 * 日志与排错边界清晰。</p>
 *
 * <p><b>定位键：</b>{@code roleCode} + {@code ownerType} + {@code securityRealm}。
 * 这三者共同唯一确定一个角色（与 {@code uk_nx_role_owner_code} 一致），
 * 因此重启不会重复创建。</p>
 *
 * <p><b>只读语义：</b>配置声明的角色统一写入 {@code read_only=true}，
 * 管理端不允许删除、改授权或下线，只能改配置文件后重启生效。
 * 配置中移除的角色<b>不</b>自动删除，避免误删运营人员在管理端创建的数据。</p>
 *
 * <p>同步不依赖会话上下文，因此直接使用 DAO，不经过带权限校验的 Operator。</p>
 *
 * @author Smars
 * @date 2026/11/08
 */
public class RoleDefinitionSyncService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleDefinitionSyncService.class);

    private final RoleDao roleDao;

    /**
     * 创建角色同步服务。
     *
     * @param roleDao 角色 DAO
     */
    public RoleDefinitionSyncService(RoleDao roleDao) {
        if (roleDao == null) {
            throw new IllegalArgumentException("roleDao is required");
        }
        this.roleDao = roleDao;
    }

    /**
     * 角色同步结果。
     *
     * @param created 新建角色数
     * @param updated 更新角色数
     */
    public record SyncResult(int created, int updated) {
    }

    /**
     * 执行角色同步。
     *
     * @param definitions 配置声明的角色
     * @return 同步结果
     */
    @Transactional
    public SyncResult sync(List<RoleDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return new SyncResult(0, 0);
        }
        int created = 0;
        int updated = 0;
        for (RoleDefinition definition : definitions) {
            RoleOwnerType ownerType = parseOwnerType(definition.ownerType());
            RoleEntity role = findRole(definition.roleCode(), ownerType);
            if (role == null) {
                roleDao.insert(newRole(definition, ownerType));
                created++;
            } else if (applyDefinition(role, definition)) {
                roleDao.updateById(role);
                updated++;
            }
        }
        LOGGER.info("Role definition sync finished created={} updated={}", created, updated);
        return new SyncResult(created, updated);
    }

    /**
     * 按配置定位角色；供授权阶段复用同一套匹配规则。
     *
     * @param roleCode  角色编码
     * @param ownerType 作用域
     * @return 已存在的角色，不存在时返回 {@code null}
     */
    public RoleEntity findRole(String roleCode, RoleOwnerType ownerType) {
        LambdaQueryWrapper<RoleEntity> query = Wrappers.<RoleEntity>lambdaQuery()
                .eq(RoleEntity::getRoleCode, roleCode)
                .eq(RoleEntity::getOwnerType, ownerType.name())
                .eq(RoleEntity::getSecurityRealm, ownerType.securityRealm().name());
        if (ownerType == RoleOwnerType.PLATFORM) {
            // PLATFORM 角色的 ownerId 历史上可能是 null 或空串，两种都要认。
            query.and(wrapper -> wrapper
                    .isNull(RoleEntity::getOwnerId)
                    .or()
                    .eq(RoleEntity::getOwnerId, ""));
        }
        return roleDao.selectOne(query.last("LIMIT 1"));
    }

    /**
     * 解析配置中的作用域字符串。
     *
     * @param value 配置值
     * @return 作用域枚举
     */
    public static RoleOwnerType parseOwnerType(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        for (RoleOwnerType candidate : RoleOwnerType.values()) {
            if (candidate.name().equals(normalized)) {
                return candidate;
            }
        }
        throw ConsoleExceptions.build(
                NexusStatusCode.CONFIG_ERROR.fullCode(),
                "Invalid role ownerType in roles.yaml: " + value);
    }

    private RoleEntity newRole(RoleDefinition definition, RoleOwnerType ownerType) {
        RoleEntity entity = new RoleEntity();
        entity.setRoleCode(definition.roleCode().trim());
        entity.setRoleName(definition.roleName());
        entity.setOwnerType(ownerType.name());
        entity.setOwnerId(ownerType == RoleOwnerType.PLATFORM ? null : "");
        entity.setSecurityRealm(ownerType.securityRealm().name());
        entity.setDescription(definition.description());
        entity.setSortOrder(definition.order());
        entity.setStatus(BasicStatus.ENABLED.name());
        entity.setBuiltIn(true);
        entity.setAdministrator(definition.admin());
        entity.setReadOnly(true);
        return entity;
    }

    /**
     * 把配置定义写入已有角色。
     *
     * @return 是否有字段发生变化
     */
    private boolean applyDefinition(RoleEntity entity, RoleDefinition definition) {
        boolean changed = false;
        String roleName = definition.roleName();
        if (roleName != null && !roleName.equals(entity.getRoleName())) {
            entity.setRoleName(roleName);
            changed = true;
        }
        if (!Objects.equals(definition.description(), entity.getDescription())) {
            entity.setDescription(definition.description());
            changed = true;
        }
        if (definition.order() != (entity.getSortOrder() == null ? 0 : entity.getSortOrder())) {
            entity.setSortOrder(definition.order());
            changed = true;
        }
        if (definition.admin() != Boolean.TRUE.equals(entity.getAdministrator())) {
            entity.setAdministrator(definition.admin());
            changed = true;
        }
        if (!Boolean.TRUE.equals(entity.getReadOnly())) {
            entity.setReadOnly(true);
            changed = true;
        }
        // 配置声明的角色必须处于启用状态，否则授权对登录用户不可见。
        if (!BasicStatus.ENABLED.name().equals(entity.getStatus())) {
            entity.setStatus(BasicStatus.ENABLED.name());
            changed = true;
        }
        return changed;
    }
}
