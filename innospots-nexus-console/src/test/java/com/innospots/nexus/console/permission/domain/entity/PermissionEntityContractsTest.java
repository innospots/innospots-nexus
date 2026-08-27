package com.innospots.nexus.console.permission.domain.entity;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.permission.domain.enums.PermissionResourceType;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionEntityContractsTest {

    @Test
    void keepsOnlyTheTwoNormalizedPermissionEntities() {
        assertThat(PermissionResourceEntity.class.getAnnotation(Entity.class)).isNotNull();
        assertThat(PermissionGrantEntity.class.getAnnotation(Entity.class)).isNotNull();
        assertThat(PermissionResourceEntity.class.getAnnotation(TableName.class).value())
                .isEqualTo("nx_permission_resource");
        assertThat(PermissionGrantEntity.class.getAnnotation(TableName.class).value())
                .isEqualTo("nx_permission_grant");
        assertThat(Arrays.stream(PermissionResourceEntity.class.getDeclaredFields())
                .map(Field::getName))
                .doesNotContain("targetKey", "resourceDefinition", "permissionId");
    }

    @Test
    void permissionEntitiesIsolateBySecurityRealm() throws NoSuchFieldException {
        assertRealmField(PermissionResourceEntity.class);
        assertRealmField(PermissionGrantEntity.class);
        assertThat(PermissionResourceType.values()).containsExactly(
                PermissionResourceType.MODULE,
                PermissionResourceType.MENU,
                PermissionResourceType.PAGE,
                PermissionResourceType.ACTION,
                PermissionResourceType.DATASOURCE,
                PermissionResourceType.CAPABILITY);
    }

    private static void assertRealmField(Class<?> entityType) throws NoSuchFieldException {
        Field field = entityType.getDeclaredField("securityRealm");
        Column column = field.getAnnotation(Column.class);
        assertThat(field.getType()).isEqualTo(String.class);
        assertThat(column).isNotNull();
        assertThat(column.length()).isEqualTo(32);
        assertThat(column.nullable()).isFalse();
    }
}
