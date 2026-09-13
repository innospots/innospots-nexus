package com.innospots.nexus.core.persistence;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.core.persistence.entity.BaseEntity;
import com.innospots.nexus.core.persistence.entity.ProjectBaseEntity;
import com.innospots.nexus.core.persistence.entity.TenantBaseEntity;
import com.innospots.nexus.core.persistence.entity.WorkspaceBaseEntity;
import com.innospots.nexus.core.persistence.handler.AuditMetaObjectHandler;
import com.innospots.nexus.core.persistence.id.DbPrimaryGenerator;
import com.innospots.nexus.core.resource.domain.entity.MetaResourceEntity;
import com.innospots.nexus.core.server.domain.entity.ServiceRegistryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Table;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoreEntityContractsTest {

    @Test
    void baseEntityDefinesAuditFillFields() throws NoSuchFieldException {
        assertThat(BaseEntity.class.getAnnotation(MappedSuperclass.class)).isNotNull();

        assertThat(BaseEntity.class.getDeclaredField("createdAt").getAnnotation(TableField.class).fill())
                .isEqualTo(FieldFill.INSERT);
        assertColumnNameIsImplicit(BaseEntity.class.getDeclaredField("createdAt"));
        assertThat(BaseEntity.class.getDeclaredField("updatedAt").getAnnotation(TableField.class).fill())
                .isEqualTo(FieldFill.INSERT_UPDATE);
        assertColumnNameIsImplicit(BaseEntity.class.getDeclaredField("updatedAt"));
        assertThat(BaseEntity.class.getDeclaredField("createdBy").getAnnotation(TableField.class).fill())
                .isEqualTo(FieldFill.INSERT);
        assertColumnNameIsImplicit(BaseEntity.class.getDeclaredField("createdBy"));
        assertThat(BaseEntity.class.getDeclaredField("createdBy").getAnnotation(Column.class).length()).isEqualTo(64);
        assertThat(BaseEntity.class.getDeclaredField("updatedBy").getAnnotation(TableField.class).fill())
                .isEqualTo(FieldFill.INSERT_UPDATE);
        assertColumnNameIsImplicit(BaseEntity.class.getDeclaredField("updatedBy"));
        assertThat(BaseEntity.class.getDeclaredField("updatedBy").getAnnotation(Column.class).length()).isEqualTo(64);
    }

    @Test
    void projectEntityDefinesProjectScopeFillField() throws NoSuchFieldException {
        assertThat(ProjectBaseEntity.class.getAnnotation(MappedSuperclass.class)).isNotNull();
        assertThat(ProjectBaseEntity.class.getSuperclass()).isEqualTo(WorkspaceBaseEntity.class);

        TableField projectId = ProjectBaseEntity.class.getDeclaredField("projectId")
                .getAnnotation(TableField.class);

        assertThat(projectId.fill()).isEqualTo(FieldFill.INSERT_UPDATE);
        assertColumnNameIsImplicit(ProjectBaseEntity.class.getDeclaredField("projectId"));
    }

    @Test
    void workspaceEntityDefinesWorkspaceScopeFillField() throws NoSuchFieldException {
        assertThat(WorkspaceBaseEntity.class.getAnnotation(MappedSuperclass.class)).isNotNull();
        assertThat(TenantBaseEntity.class.getAnnotation(MappedSuperclass.class)).isNotNull();
        assertThat(WorkspaceBaseEntity.class.getSuperclass()).isEqualTo(TenantBaseEntity.class);
        assertThat(TenantBaseEntity.class.getSuperclass()).isEqualTo(BaseEntity.class);

        TableField tenantId = TenantBaseEntity.class.getDeclaredField("tenantId")
                .getAnnotation(TableField.class);
        TableField workspaceId = WorkspaceBaseEntity.class.getDeclaredField("workspaceId")
                .getAnnotation(TableField.class);

        assertThat(tenantId.fill()).isEqualTo(FieldFill.INSERT_UPDATE);
        assertThat(workspaceId.fill()).isEqualTo(FieldFill.INSERT_UPDATE);
        assertColumnNameIsImplicit(WorkspaceBaseEntity.class.getDeclaredField("workspaceId"));
        assertColumnNameIsImplicit(TenantBaseEntity.class.getDeclaredField("tenantId"));
    }

    @Test
    void auditMetaObjectHandlerProvidesMybatisPlusFillContract() {
        MetaObjectHandler handler = new AuditMetaObjectHandler();

        assertThat(handler).isInstanceOf(MetaObjectHandler.class);
    }

    @Test
    void auditMetaObjectHandlerFillsCreatedByAndUpdatedByWithUserName() {
        try {
            TLC.userId(1001L);
            TLC.userName("alice");
            TLC.tenantId("tnt01");
            TLC.workspaceId("wks01");
            MetaResourceEntity entity = new MetaResourceEntity();

            new AuditMetaObjectHandler().insertFill(SystemMetaObject.forObject(entity));

            assertThat(entity.getCreatedBy()).isEqualTo("alice");
            assertThat(entity.getUpdatedBy()).isEqualTo("alice");
            assertThat(entity.getTenantId()).isEqualTo("tnt01");
            assertThat(entity.getWorkspaceId()).isEqualTo("wks01");
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getUpdatedAt()).isNotNull();
        } finally {
            TLC.clear();
        }
    }

    @Test
    void auditMetaObjectHandlerFillsProjectIdOnProjectScopedEntities() {
        try {
            TLC.tenantId("tnt01");
            TLC.workspaceId("wks01");
            TLC.projectId("prj01");
            ProjectScopedEntity entity = new ProjectScopedEntity();

            new AuditMetaObjectHandler().insertFill(SystemMetaObject.forObject(entity));

            assertThat(entity.getTenantId()).isEqualTo("tnt01");
            assertThat(entity.getWorkspaceId()).isEqualTo("wks01");
            assertThat(entity.getProjectId()).isEqualTo("prj01");
        } finally {
            TLC.clear();
        }
    }

    @Test
    void concreteEntitiesDefinePersistenceEntityAndTableAnnotations() {
        assertPersistenceTable(MetaResourceEntity.class, MetaResourceEntity.TABLE_NAME);
        assertPersistenceTable(ServiceRegistryEntity.class, ServiceRegistryEntity.TABLE_NAME);

        assertThat(BaseEntity.class.getAnnotation(Entity.class)).isNull();
        assertThat(TenantBaseEntity.class.getAnnotation(Entity.class)).isNull();
        assertThat(WorkspaceBaseEntity.class.getAnnotation(Entity.class)).isNull();
    }

    @Test
    void primaryKeyFieldsDefineBothMybatisPlusAndPersistenceIdentifiers() throws NoSuchFieldException {
        assertPersistenceId(MetaResourceEntity.class.getDeclaredField("resourceId"));
        assertPersistenceId(ServiceRegistryEntity.class.getDeclaredField("serviceRegistryId"));
    }

    @Test
    void resourceAndServiceEntitiesExposePersistenceShape() {
        assertThat(MetaResourceEntity.class.getAnnotation(TableName.class).value()).isEqualTo("nx_meta_resource");
        assertThat(ServiceRegistryEntity.class.getAnnotation(TableName.class).value()).isEqualTo("nx_service_registry");

        MetaResourceEntity resource = new MetaResourceEntity();
        resource.setResourceId("res01HZY8J6Y3D6S4V7N9X2M5Q8");
        resource.setFileUri("file://demo");

        ServiceRegistryEntity service = new ServiceRegistryEntity();
        service.setServiceRegistryId("srv01HZY8J6Y3D6S4V7N9X2M5Q8");
        service.setServiceName("nexus-core");

        assertThat(resource.getResourceId()).startsWith("res");
        assertThat(service.getServiceName()).isEqualTo("nexus-core");
        assertThat(ServiceRegistryEntity.class.getDeclaredFields())
                .extracting(Field::getName)
                .contains("serviceStatus", "serviceRole")
                .doesNotContain("status", "role");
    }

    @Test
    void concreteEntityPrimaryKeysUseAssignedUuidStringIdentifiersWithLength32() throws NoSuchFieldException {
        assertStringAssignedUuidPrimaryKey(MetaResourceEntity.class.getDeclaredField("resourceId"));
        assertStringAssignedUuidPrimaryKey(ServiceRegistryEntity.class.getDeclaredField("serviceRegistryId"));
    }

    @Test
    void databasePrimaryGeneratorUsesEntityPrefixes() {
        DbPrimaryGenerator generator = new DbPrimaryGenerator();

        assertThat(generator.nextUUID(new MetaResourceEntity())).startsWith("res").hasSize(29);
        assertThat(generator.nextUUID(new ServiceRegistryEntity())).startsWith("srv").hasSize(29);
        assertThat(generator.nextUUID(new BaseEntity())).hasSize(26);
    }

    @Test
    void columnNamesStayImplicitAndStringLengthsArePowersOfTwo() {
        List<Class<?>> entityTypes = List.of(
                BaseEntity.class,
                TenantBaseEntity.class,
                WorkspaceBaseEntity.class,
                ProjectBaseEntity.class,
                MetaResourceEntity.class,
                ServiceRegistryEntity.class
        );

        for (Class<?> entityType : entityTypes) {
            for (Field field : entityType.getDeclaredFields()) {
                Column column = field.getAnnotation(Column.class);
                if (column == null) {
                    continue;
                }
                assertColumnNameIsImplicit(field);
                if (field.getType() == String.class) {
                    assertThat(column.length())
                            .as("%s.%s length", entityType.getSimpleName(), field.getName())
                            .isGreaterThan(0);
                    assertThat(isPowerOfTwo(column.length()))
                            .as("%s.%s length must be power of two", entityType.getSimpleName(), field.getName())
                            .isTrue();
                }
            }
        }
    }

    private static void assertColumnNameIsImplicit(Field field) {
        assertThat(field.getAnnotation(Column.class).name())
                .as("%s.%s column name", field.getDeclaringClass().getSimpleName(), field.getName())
                .isEmpty();
    }

    private static boolean isPowerOfTwo(int value) {
        return value > 0 && (value & (value - 1)) == 0;
    }

    private static void assertPersistenceTable(Class<?> entityType, String tableName) {
        assertThat(entityType.getAnnotation(Entity.class))
                .as("%s @Entity", entityType.getSimpleName())
                .isNotNull();
        assertThat(entityType.getAnnotation(Table.class).name())
                .as("%s @Table", entityType.getSimpleName())
                .isEqualTo(tableName);
        assertThat(entityType.getAnnotation(TableName.class).value())
                .as("%s @TableName", entityType.getSimpleName())
                .isEqualTo(tableName);
    }

    private static void assertPersistenceId(Field field) {
        assertThat(field.getAnnotation(TableId.class))
                .as("%s.%s @TableId", field.getDeclaringClass().getSimpleName(), field.getName())
                .isNotNull();
        assertThat(field.getAnnotation(Id.class))
                .as("%s.%s @Id", field.getDeclaringClass().getSimpleName(), field.getName())
                .isNotNull();
    }

    @MappedSuperclass
    static class ProjectScopedEntity extends ProjectBaseEntity {
    }

    private static void assertStringAssignedUuidPrimaryKey(Field field) {
        assertPersistenceId(field);
        assertThat(field.getType())
                .as("%s.%s type", field.getDeclaringClass().getSimpleName(), field.getName())
                .isEqualTo(String.class);
        assertThat(field.getAnnotation(TableId.class).type())
                .as("%s.%s id type", field.getDeclaringClass().getSimpleName(), field.getName())
                .isEqualTo(IdType.ASSIGN_UUID);
        assertThat(field.getAnnotation(Column.class).length())
                .as("%s.%s length", field.getDeclaringClass().getSimpleName(), field.getName())
                .isEqualTo(32);
    }
}
