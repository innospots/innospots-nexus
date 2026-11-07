package com.innospots.nexus.console.scope.workspace.domain.entity;

import java.lang.reflect.Field;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.scope.tenant.domain.entity.base.TenantWorkspaceBaseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceMemberEntityContractsTest {

    @Test
    void workspaceMemberEntityExposesWorkspaceScopedPersistenceTable() {
        assertThat(WorkspaceMemberEntity.class.getAnnotation(Entity.class)).isNotNull();
        assertThat(WorkspaceMemberEntity.class.getAnnotation(Table.class).name())
                .isEqualTo("nx_workspace_member");
        assertThat(WorkspaceMemberEntity.class.getAnnotation(TableName.class).value())
                .isEqualTo("nx_workspace_member");
        assertThat(WorkspaceMemberEntity.class.getSuperclass())
                .isEqualTo(TenantWorkspaceBaseEntity.class);
        assertThat(new WorkspaceMemberEntity().idPrefix()).isEqualTo("WSM");
    }

    @Test
    void workspaceMemberEntityContainsMembershipFields() throws Exception {
        Field id = WorkspaceMemberEntity.class.getDeclaredField("workspaceMemberId");
        assertThat(id.getAnnotation(Id.class)).isNotNull();
        assertThat(id.getAnnotation(TableId.class).type()).isEqualTo(IdType.ASSIGN_UUID);
        assertThat(id.getAnnotation(Column.class).length()).isEqualTo(32);

        Field tenantMemberId = WorkspaceMemberEntity.class.getDeclaredField("tenantMemberId");
        assertThat(tenantMemberId.getAnnotation(Column.class).nullable()).isFalse();

        Field status = WorkspaceMemberEntity.class.getDeclaredField("status");
        assertThat(status.getAnnotation(Column.class).nullable()).isFalse();
    }

    @Test
    void workspaceScopedColumnsComeFromBaseEntity() {
        assertThat(TenantWorkspaceBaseEntity.class.getSuperclass().getSimpleName())
                .isEqualTo("TenantBaseEntity");
    }
}
