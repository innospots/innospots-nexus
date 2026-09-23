package com.innospots.nexus.core.persistence.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.core.persistence.entity.BaseEntity;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 元对象处理器，为继承 {@link BaseEntity} 或 kernel 租户域基类的实体自动填充审计字段。
 * <p>从 {@link com.innospots.nexus.base.thread.TLC 线程本地上下文} 读取用户身份、租户 ID、
 * 工作区 ID 与项目 ID，仓储层无需显式赋值。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see BaseEntity
 */
public class AuditMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时填充 createdAt、updatedAt、createdBy、updatedBy；
     * TLC 中存在时同时填充 tenantId、workspaceId、projectId。
     *
     * @param metaObject 元对象
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
     * 更新时填充 updatedAt、updatedBy；作用域列在插入后不可变。
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName("updatedAt", LocalDateTime.now(), metaObject);
        setFieldValByName("updatedBy", currentUserName(), metaObject);
    }

    /**
     * 从 TLC 解析当前用户名；未设置用户名时回退为用户 ID 字符串。
     *
     * @return 当前用户标识
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
        String projectId = TLC.projectId();
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
        if (projectId != null) {
            if (insert) {
                fillStrategy(metaObject, "projectId", projectId);
            } else {
                setFieldValByName("projectId", projectId, metaObject);
            }
        }
    }
}
