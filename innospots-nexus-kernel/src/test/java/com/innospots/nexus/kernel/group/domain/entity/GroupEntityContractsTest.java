package com.innospots.nexus.kernel.group.domain.entity;

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

import com.innospots.nexus.core.entity.ProjectBaseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GroupEntityContractsTest {

    @Test
    void groupEntitiesUseProjectScopedPersistenceTables() {
        assertPersistenceTable(GroupEntity.class, "nx_group");
        assertPersistenceTable(GroupMemberEntity.class, "nx_group_member");
        assertPersistenceTable(TagEntity.class, "nx_tag");
        assertPersistenceTable(GroupMemberTagEntity.class, "nx_group_member_tag");

        assertThat(GroupEntity.class.getSuperclass()).isEqualTo(ProjectBaseEntity.class);
        assertThat(GroupMemberEntity.class.getSuperclass()).isEqualTo(ProjectBaseEntity.class);
        assertThat(TagEntity.class.getSuperclass()).isEqualTo(ProjectBaseEntity.class);
        assertThat(GroupMemberTagEntity.class.getSuperclass()).isEqualTo(ProjectBaseEntity.class);
    }

    @Test
    void groupEntityContainsHierarchyAndLifecycleFields() throws NoSuchFieldException {
        assertPersistenceId(GroupEntity.class, "groupId");
        assertField(GroupEntity.class, "groupId", String.class, 32, false);
        assertField(GroupEntity.class, "parentId", String.class, 32, true);
        assertField(GroupEntity.class, "groupCode", String.class, 64, false);
        assertField(GroupEntity.class, "groupName", String.class, 128, false);
        assertField(GroupEntity.class, "description", String.class, 256, true);
        assertField(GroupEntity.class, "status", String.class, 32, false);
        assertField(GroupEntity.class, "sortOrder", Integer.class, 255, false);
        assertField(GroupEntity.class, "builtIn", Boolean.class, 255, false);
    }

    @Test
    void groupMemberEntityEnforcesSingleGroupOwnership() throws NoSuchFieldException {
        assertPersistenceId(GroupMemberEntity.class, "groupMemberId");
        assertField(GroupMemberEntity.class, "groupMemberId", String.class, 32, false);
        assertField(GroupMemberEntity.class, "groupId", String.class, 32, false);
        assertField(GroupMemberEntity.class, "userId", String.class, 32, false);
        assertField(GroupMemberEntity.class, "leader", Boolean.class, 255, false);

        assertIndex(GroupMemberEntity.class, "uk_nx_group_member_project_user",
                "project_id,user_id", true);
        assertIndex(GroupMemberEntity.class, "idx_nx_group_member_project_group",
                "project_id,group_id", false);
    }

    @Test
    void tagEntityUsesProjectUniqueTagName() throws NoSuchFieldException {
        assertPersistenceId(TagEntity.class, "tagId");
        assertField(TagEntity.class, "tagId", String.class, 32, false);
        assertField(TagEntity.class, "tagName", String.class, 64, false);
        assertField(TagEntity.class, "description", String.class, 256, true);
        assertField(TagEntity.class, "status", String.class, 32, false);

        assertIndex(TagEntity.class, "uk_nx_tag_project_name",
                "project_id,tag_name", true);
    }

    @Test
    void groupMemberTagEntityLinksByTagName() throws NoSuchFieldException {
        assertPersistenceId(GroupMemberTagEntity.class, "groupMemberTagId");
        assertField(GroupMemberTagEntity.class, "groupMemberTagId", String.class, 32, false);
        assertField(GroupMemberTagEntity.class, "groupId", String.class, 32, false);
        assertField(GroupMemberTagEntity.class, "userId", String.class, 32, false);
        assertField(GroupMemberTagEntity.class, "tagName", String.class, 64, false);

        assertThat(GroupMemberTagEntity.class.getDeclaredFields())
                .extracting(Field::getName)
                .doesNotContain("tagId");
        assertIndex(GroupMemberTagEntity.class, "uk_nx_group_member_tag_assignment",
                "project_id,group_id,user_id,tag_name", true);
        assertIndex(GroupMemberTagEntity.class, "idx_nx_group_member_tag_project_name",
                "project_id,tag_name", false);
    }

    private static void assertPersistenceTable(Class<?> entityType, String tableName) {
        assertThat(entityType.getAnnotation(Entity.class)).isNotNull();
        assertThat(entityType.getAnnotation(Table.class).name()).isEqualTo(tableName);
        assertThat(entityType.getAnnotation(TableName.class).value()).isEqualTo(tableName);
    }

    private static void assertPersistenceId(Class<?> entityType, String fieldName)
            throws NoSuchFieldException {
        Field field = entityType.getDeclaredField(fieldName);
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

    private static void assertIndex(
            Class<?> entityType,
            String indexName,
            String columnList,
            boolean unique
    ) {
        assertThat(entityType.getAnnotation(Table.class).indexes())
                .filteredOn(index -> index.name().equals(indexName))
                .singleElement()
                .satisfies(index -> {
                    Index tableIndex = (Index) index;
                    assertThat(tableIndex.columnList()).isEqualTo(columnList);
                    assertThat(tableIndex.unique()).isEqualTo(unique);
                });
    }
}
