package com.innospots.nexus.kernel.organization.domain.entity;

import java.lang.reflect.Field;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.persistence.entity.TenantBaseEntity;
import com.innospots.nexus.kernel.organization.domain.enums.OrganizationUnitType;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationEntityContractsTest {

    @Test
    void organizationUnitEntityExposesTenantScopedPersistenceTable() {
        assertPersistenceTable(OrganizationUnitEntity.class, "nx_organization_unit");
        assertThat(OrganizationUnitEntity.class.getSuperclass()).isEqualTo(TenantBaseEntity.class);
        assertThat(new OrganizationUnitEntity().idPrefix()).isEqualTo("org");
    }

    @Test
    void organizationMemberEntityExposesTenantScopedPersistenceTable() {
        assertPersistenceTable(OrganizationMemberEntity.class, "nx_organization_member");
        assertThat(OrganizationMemberEntity.class.getSuperclass()).isEqualTo(TenantBaseEntity.class);
        assertThat(new OrganizationMemberEntity().idPrefix()).isEqualTo("ogm");
    }

    @Test
    void organizationUnitEntityContainsTreeFields() throws NoSuchFieldException {
        assertPersistenceId(OrganizationUnitEntity.class.getDeclaredField("unitId"));
        assertField(OrganizationUnitEntity.class, "unitId", String.class, 32, false);
        assertField(OrganizationUnitEntity.class, "parentId", String.class, 32, true);
        assertField(OrganizationUnitEntity.class, "unitCode", String.class, 64, false);
        assertField(OrganizationUnitEntity.class, "unitName", String.class, 128, false);
        assertField(OrganizationUnitEntity.class, "unitType", String.class, 32, false);
        assertField(OrganizationUnitEntity.class, "sortOrder", Integer.class, 255, false);
        assertField(OrganizationUnitEntity.class, "status", String.class, 32, false);
    }

    @Test
    void organizationMemberEntityContainsAssociationFields() throws NoSuchFieldException {
        assertPersistenceId(OrganizationMemberEntity.class.getDeclaredField("organizationMemberId"));
        assertField(OrganizationMemberEntity.class, "organizationMemberId", String.class, 32, false);
        assertField(OrganizationMemberEntity.class, "unitId", String.class, 32, false);
        assertField(OrganizationMemberEntity.class, "tenantMemberId", String.class, 32, false);
    }

    @Test
    void organizationUnitTypeEnumeratesInternalTreeNodes() {
        assertThat(OrganizationUnitType.values()).containsExactly(
                OrganizationUnitType.COMPANY,
                OrganizationUnitType.BRANCH,
                OrganizationUnitType.DEPARTMENT,
                OrganizationUnitType.TEAM);
    }

    @Test
    void organizationEntitiesDeclareTenantAwareIndexes() {
        assertIndex(OrganizationUnitEntity.class, "uk_nx_organization_unit_code", "tenant_id,unit_code", true);
        assertIndex(OrganizationUnitEntity.class, "idx_nx_organization_unit_parent",
                "tenant_id,parent_id,sort_order", false);
        assertIndex(OrganizationMemberEntity.class, "uk_nx_organization_member",
                "tenant_id,unit_id,tenant_member_id", true);
    }

    private static void assertPersistenceTable(Class<?> entityType, String tableName) {
        assertThat(entityType.getAnnotation(Entity.class)).isNotNull();
        assertThat(entityType.getAnnotation(Table.class).name()).isEqualTo(tableName);
        assertThat(entityType.getAnnotation(TableName.class).value()).isEqualTo(tableName);
    }

    private static void assertIndex(Class<?> entityType, String indexName, String columnList, boolean unique) {
        assertThat(entityType.getAnnotation(Table.class).indexes())
                .filteredOn(index -> index.name().equals(indexName))
                .singleElement()
                .satisfies(index -> {
                    Index tableIndex = (Index) index;
                    assertThat(tableIndex.columnList()).isEqualTo(columnList);
                    assertThat(tableIndex.unique()).isEqualTo(unique);
                });
    }

    private static void assertPersistenceId(Field field) {
        assertThat(field.getAnnotation(Id.class)).isNotNull();
        assertThat(field.getAnnotation(TableId.class).type()).isEqualTo(IdType.ASSIGN_UUID);
    }

    private static void assertField(
            Class<?> entityType,
            String fieldName,
            Class<?> fieldType,
            int length,
            boolean nullable
    ) throws NoSuchFieldException {
        Field field = entityType.getDeclaredField(fieldName);
        Column column = field.getAnnotation(Column.class);

        assertThat(field.getType()).isEqualTo(fieldType);
        assertThat(column).isNotNull();
        assertThat(column.length()).isEqualTo(length);
        assertThat(column.nullable()).isEqualTo(nullable);
    }
}
