package com.innospots.nexus.console.permission.domain.entity;

import java.lang.reflect.Field;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionEntityContractsTest {

    @Test
    void grantEntityUsesWorkspaceScopedGrantTable() {
        assertThat(PermissionGrantEntity.class.getAnnotation(Entity.class)).isNotNull();
        assertThat(PermissionGrantEntity.class.getAnnotation(TableName.class).value())
                .isEqualTo("nx_permission_grant");
    }

    @Test
    void grantEntityIsolatesBySecurityRealm() throws NoSuchFieldException {
        Field field = PermissionGrantEntity.class.getDeclaredField("securityRealm");
        Column column = field.getAnnotation(Column.class);
        assertThat(field.getType()).isEqualTo(String.class);
        assertThat(column).isNotNull();
        assertThat(column.length()).isEqualTo(32);
        assertThat(column.nullable()).isFalse();
    }
}
