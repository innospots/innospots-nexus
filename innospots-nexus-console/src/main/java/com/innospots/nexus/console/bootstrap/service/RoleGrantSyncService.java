package com.innospots.nexus.console.bootstrap.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.transaction.Transactional;

import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest.RoleDefinition;
import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest.ModuleGrant;
import com.innospots.nexus.console.bootstrap.loader.RoleAuthorizationManifestLoader;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.console.permission.domain.enums.PermissionGrantKeyType;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 启动引导第 3 阶段：按配置写入角色资源授权。
 *
 * <p><b>前置依赖（必须已执行）：</b></p>
 * <ul>
 *   <li>第 1 阶段 {@code CATALOG}：权限目录已同步，
 *       {@link ConsoleCatalogService#grantKeyTypes()} 才能解析授权键；</li>
 *   <li>第 2 阶段 {@code ROLE}：角色已存在，授权行才有挂载主体。</li>
 * </ul>
 * <p>任一项缺失都会让本阶段失败，因此 {@link com.innospots.nexus.console.bootstrap.domain.model.BootstrapStage}
 * 把本阶段固定在 220，排在目录（200）与角色（210）之后。</p>
 *
 * <p><b>幂等语义：</b>按 {@code subject + grant_key} upsert，重复启动只会补齐缺失项；
 * 配置中移除的授权<b>不</b>自动删除，避免误删管理端授予的权限。</p>
 *
 * @author Smars
 * @date 2026/11/08
 */
public class RoleGrantSyncService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleGrantSyncService.class);

    private final RoleDefinitionSyncService roleDefinitionSyncService;
    private final PermissionGrantDao grantDao;
    private final ConsoleCatalogService catalogService;

    /**
     * 创建角色授权同步服务。
     *
     * @param roleDefinitionSyncService 角色同步服务，复用其角色定位规则
     * @param grantDao                  授权 DAO
     * @param catalogService            权限目录服务，用于校验授权键是否有效
     */
    public RoleGrantSyncService(
            RoleDefinitionSyncService roleDefinitionSyncService,
            PermissionGrantDao grantDao,
            ConsoleCatalogService catalogService
    ) {
        if (roleDefinitionSyncService == null || grantDao == null || catalogService == null) {
            throw new IllegalArgumentException("Role grant sync dependencies are required");
        }
        this.roleDefinitionSyncService = roleDefinitionSyncService;
        this.grantDao = grantDao;
        this.catalogService = catalogService;
    }

    /**
     * 授权同步结果。
     *
     * @param created 新建授权数
     * @param updated 更新授权数
     * @param skipped 角色不存在而跳过的授权数
     */
    public record SyncResult(int created, int updated, int skipped) {
    }

    /**
     * 执行角色授权同步。
     *
     * @param definitions 配置声明的角色及其授权
     * @return 同步结果
     */
    @Transactional
    public SyncResult sync(List<RoleDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return new SyncResult(0, 0, 0);
        }
        // 目录只读一次：grantKeyTypes() 会遍历目录树，逐条授权调用代价很高。
        Map<String, PermissionGrantKeyType> grantKeyTypes = catalogService.grantKeyTypes();
        int created = 0;
        int updated = 0;
        int skipped = 0;
        for (RoleDefinition definition : definitions) {
            List<ModuleGrant> grants = definition.grants();
            if (grants == null || grants.isEmpty()) {
                continue;
            }
            // 一个模块声明多个分组，落库时展开为多条授权键。
            List<String> grantKeys = RoleAuthorizationManifestLoader.expandGrantKeys(grants);
            RoleOwnerType ownerType = RoleDefinitionSyncService.parseOwnerType(definition.ownerType());
            RoleEntity role = roleDefinitionSyncService.findRole(definition.roleCode().trim(), ownerType);
            if (role == null) {
                // 角色阶段应已创建；缺失说明前序阶段被跳过，记录后继续而不是中断。
                LOGGER.warn(
                        "Role not found when syncing grants, skipped roleCode={} ownerType={}",
                        definition.roleCode(),
                        ownerType);
                skipped += grantKeys.size();
                continue;
            }
            for (String grantKey : grantKeys) {
                PermissionGrantKeyType keyType = grantKeyTypes.get(grantKey);
                if (keyType == null) {
                    throw ConsoleExceptions.build(
                            NexusStatusCode.CONFIG_ERROR.fullCode(),
                            "Unknown permission resource in roles.yaml: " + grantKey
                                    + " (role " + role.getRoleCode() + ")");
                }
                PermissionGrantEntity existing = findGrant(role, grantKey);
                if (existing == null) {
                    grantDao.insert(newGrant(role, grantKey, keyType));
                    created++;
                } else if (applyGrant(existing, keyType)) {
                    grantDao.updateById(existing);
                    updated++;
                }
            }
        }
        LOGGER.info(
                "Role grant sync finished created={} updated={} skipped={}",
                created,
                updated,
                skipped);
        return new SyncResult(created, updated, skipped);
    }

    private PermissionGrantEntity findGrant(RoleEntity role, String grantKey) {
        return grantDao.selectOne(Wrappers.<PermissionGrantEntity>lambdaQuery()
                .eq(PermissionGrantEntity::getSubjectType, PermissionSubjectType.ROLE.name())
                .eq(PermissionGrantEntity::getSubjectId, role.getRoleId())
                .eq(PermissionGrantEntity::getGrantKey, grantKey)
                .eq(PermissionGrantEntity::getOwnerType, role.getOwnerType())
                .eq(PermissionGrantEntity::getSecurityRealm, role.getSecurityRealm())
                .last("LIMIT 1"));
    }

    private PermissionGrantEntity newGrant(
            RoleEntity role,
            String grantKey,
            PermissionGrantKeyType keyType
    ) {
        PermissionGrantEntity grant = new PermissionGrantEntity();
        grant.setSubjectType(PermissionSubjectType.ROLE.name());
        grant.setSubjectId(role.getRoleId());
        grant.setGrantKey(grantKey);
        grant.setGrantKeyType(keyType.code());
        grant.setOwnerType(role.getOwnerType());
        grant.setOwnerId(role.getOwnerId());
        grant.setSecurityRealm(role.getSecurityRealm());
        grant.setReadOnly(true);
        return grant;
    }

    /**
     * 把配置声明同步到已有授权行。
     *
     * @return 是否有字段发生变化
     */
    private boolean applyGrant(PermissionGrantEntity entity, PermissionGrantKeyType keyType) {
        boolean changed = false;
        if (!keyType.code().equals(entity.getGrantKeyType())) {
            entity.setGrantKeyType(keyType.code());
            changed = true;
        }
        if (!Boolean.TRUE.equals(entity.getReadOnly())) {
            entity.setReadOnly(true);
            changed = true;
        }
        return changed;
    }

    /**
     * 判断授权键是否存在于权限目录中；供配置自检使用。
     *
     * @param grantKey 授权键
     * @return 存在的目录层级
     */
    public Optional<PermissionGrantKeyType> resolveKeyType(String grantKey) {
        return Optional.ofNullable(catalogService.grantKeyTypes().get(grantKey));
    }
}
