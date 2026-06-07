package com.innospots.nexus.core.entity;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.innospots.nexus.base.thread.TLC;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus meta-object handler that auto-fills audit fields on
 * entities extending {@link BaseEntity} or {@link ProjectBaseEntity}.
 * <p>Reads user identity and project ID from {@link
 * com.innospots.nexus.base.thread.TLC thread-local context}, so no
 * explicit field assignment is needed at the repository layer.</p>
 */
public class AuditMetaObjectHandler implements MetaObjectHandler {

    /**
     * Fills createdTime, updatedTime, createdBy, updatedBy on insert.
     * If a project ID is available in TLC, also fills projectId.
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String userName = currentUserName();
        Long projectId = TLC.projectId();

        fillStrategy(metaObject, "createdTime", now);
        fillStrategy(metaObject, "updatedTime", now);
        fillStrategy(metaObject, "createdBy", userName);
        fillStrategy(metaObject, "updatedBy", userName);
        if (projectId != null) {
            fillStrategy(metaObject, "projectId", projectId);
        }
    }

    /**
     * Fills updatedTime and updatedBy on update.
     * Also refreshes projectId from TLC if available.
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName("updatedTime", LocalDateTime.now(), metaObject);
        setFieldValByName("updatedBy", currentUserName(), metaObject);
        Long projectId = TLC.projectId();
        if (projectId != null) {
            setFieldValByName("projectId", projectId, metaObject);
        }
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
}
