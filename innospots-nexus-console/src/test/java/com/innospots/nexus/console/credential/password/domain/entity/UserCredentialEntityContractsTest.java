package com.innospots.nexus.console.credential.password.domain.entity;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.persistence.entity.OwnershipEntity;

import static org.assertj.core.api.Assertions.assertThat;

class UserCredentialEntityContractsTest {

    @Test
    void userCredentialUsesOwnershipScopedTable() {
        assertThat(UserCredentialEntity.class.getAnnotation(Table.class).name())
                .isEqualTo("nx_user_credential");
        assertThat(UserCredentialEntity.class.getAnnotation(TableName.class).value())
                .isEqualTo("nx_user_credential");
        assertThat(UserCredentialEntity.class.getSuperclass()).isEqualTo(OwnershipEntity.class);
        assertThat(new UserCredentialEntity().idPrefix()).isEqualTo("ucr");
    }

    @Test
    void userCredentialDeclaresSubjectUniqueIndex() {
        Index[] indexes = UserCredentialEntity.class.getAnnotation(Table.class).indexes();
        assertThat(indexes).anySatisfy(index -> {
            assertThat(index.name()).isEqualTo("uk_nx_user_credential_subject");
            assertThat(index.columnList()).contains("owner_type", "subject_id", "credential_kind");
            assertThat(index.unique()).isTrue();
        });
    }

    @Test
    void userCredentialStoresOpaqueVerifierFields() throws NoSuchFieldException {
        assertPersistenceId(UserCredentialEntity.class.getDeclaredField("credentialId"));
        assertField(UserCredentialEntity.class, "subjectId", String.class, 32, false);
        assertField(UserCredentialEntity.class, "credentialKind", String.class, 32, false);
        assertField(UserCredentialEntity.class, "algorithm", String.class, 64, false);
        assertField(UserCredentialEntity.class, "verifier", String.class, 512, false);
        assertField(UserCredentialEntity.class, "verifierParams", String.class, 1024, true);
        assertField(UserCredentialEntity.class, "credentialVersion", Integer.class, 255, false);
        assertField(UserCredentialEntity.class, "expiredAt", LocalDateTime.class, 255, true);
    }

    private static void assertPersistenceId(Field field) {
        assertThat(field.getAnnotation(Id.class)).isNotNull();
        assertThat(field.getAnnotation(TableId.class).type()).isEqualTo(IdType.ASSIGN_UUID);
    }

    private static void assertField(
            Class<?> type,
            String name,
            Class<?> fieldType,
            int length,
            boolean nullable
    ) throws NoSuchFieldException {
        Field field = type.getDeclaredField(name);
        assertThat(field.getType()).isEqualTo(fieldType);
        Column column = field.getAnnotation(Column.class);
        assertThat(column).isNotNull();
        if (length < 255) {
            assertThat(column.length()).isEqualTo(length);
        }
        assertThat(column.nullable()).isEqualTo(nullable);
    }
}
