package com.innospots.nexus.kernel.persistence;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.innospots.nexus.core.persistence.entity.BaseEntity;
import com.innospots.nexus.kernel.persistence.entity.TenantBaseEntity;
import com.innospots.nexus.kernel.persistence.entity.TenantProjectBaseEntity;
import com.innospots.nexus.kernel.persistence.entity.TenantWorkspaceBaseEntity;
import jakarta.persistence.MappedSuperclass;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KernelPersistenceEntityContractsTest {

    @Test
    void tenantScopedBasesFormKernelHierarchy() throws NoSuchFieldException {
        assertThat(TenantBaseEntity.class.getAnnotation(MappedSuperclass.class)).isNotNull();
        assertThat(TenantWorkspaceBaseEntity.class.getAnnotation(MappedSuperclass.class)).isNotNull();
        assertThat(TenantProjectBaseEntity.class.getAnnotation(MappedSuperclass.class)).isNotNull();

        assertThat(TenantBaseEntity.class.getSuperclass()).isEqualTo(BaseEntity.class);
        assertThat(TenantWorkspaceBaseEntity.class.getSuperclass()).isEqualTo(TenantBaseEntity.class);
        assertThat(TenantProjectBaseEntity.class.getSuperclass()).isEqualTo(TenantWorkspaceBaseEntity.class);

        assertThat(TenantBaseEntity.class.getDeclaredField("tenantId").getAnnotation(TableField.class).fill())
                .isEqualTo(FieldFill.INSERT);
        assertThat(TenantWorkspaceBaseEntity.class.getDeclaredField("workspaceId").getAnnotation(TableField.class).fill())
                .isEqualTo(FieldFill.INSERT);
        assertThat(TenantProjectBaseEntity.class.getDeclaredField("projectId").getAnnotation(TableField.class).fill())
                .isEqualTo(FieldFill.INSERT);
    }
}
