package com.innospots.nexus.core.persistence.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.core.persistence.entity.BaseEntity;
import com.innospots.nexus.core.persistence.entity.TenantBaseEntity;
import com.innospots.nexus.core.persistence.entity.WorkspaceBaseEntity;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus meta-object handler that auto-fills audit fields on
 * entities extending {@link BaseEntity}, {@link TenantBaseEntity}, or
 * {@link WorkspaceBaseEntity}.
 * <p>Reads user identity, tenant ID, and workspace ID from {@link
 * com.innospots.nexus.base.thread.TLC thread-local context}, so no
 * explicit field assignment is needed at the repository layer.</p>
 */
public class AuditMetaObjectHandler implements MetaObjectHandler {

    /**
     * Fills createdAt, updatedAt, createdBy, updatedBy on insert.
     * Also fills tenantId and workspaceId when present in TLC.
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String userName = currentUserName();

        fillStrategy(metaObject, "createdAt", now);
        fillStrategy(metaObject, "updatedAt", now);
        fillStrategy(metaObject, "createdBy", userName);
        fillStrategy(metaObject, "updatedBy", userName);
        fillScope(metaObject, true);
    }

    /**
     * Fills updatedAt and updatedBy on update.
     * Also refreshes tenantId and workspaceId from TLC when present.
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName("updatedAt", LocalDateTime.now(), metaObject);
        setFieldValByName("updatedBy", currentUserName(), metaObject);
        fillScope(metaObject, false);
    }

    /**
     * Resolves the current user name from TLC.
     * Falls back to user ID (as string) if user name is not set.
     */
    private String currentUserName() {
        String userName = TLC.userName();
        if (userName != null && !userName.isBlank()) {
            return userName;
        }
        Long userId = TLC.userId();
        return userId == null ? null : String.valueOf(userId);
    }

    private void fillScope(MetaObject metaObject, boolean insert) {
        String tenantId = TLC.tenantId();
        String workspaceId = TLC.workspaceId();
        if (tenantId != null) {
            if (insert) {
                fillStrategy(metaObject, "tenantId", tenantId);
            } else {
                setFieldValByName("tenantId", tenantId, metaObject);
            }
        }
        if (workspaceId != null) {
            if (insert) {
                fillStrategy(metaObject, "workspaceId", workspaceId);
            } else {
                setFieldValByName("workspaceId", workspaceId, metaObject);
            }
        }
    }
}
