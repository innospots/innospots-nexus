package com.innospots.nexus.console.credential.otp.domain.entity;

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

class OtpChallengeEntityContractsTest {

    @Test
    void otpChallengeUsesOwnershipScopedTable() {
        assertThat(OtpChallengeEntity.class.getAnnotation(Table.class).name())
                .isEqualTo("nx_otp_challenge");
        assertThat(OtpChallengeEntity.class.getAnnotation(TableName.class).value())
                .isEqualTo("nx_otp_challenge");
        assertThat(OtpChallengeEntity.class.getSuperclass()).isEqualTo(OwnershipEntity.class);
        assertThat(new OtpChallengeEntity().idPrefix()).isEqualTo("och");
    }

    @Test
    void otpChallengeDeclaresLookupIndex() {
        Index[] indexes = OtpChallengeEntity.class.getAnnotation(Table.class).indexes();
        assertThat(indexes).anySatisfy(index -> {
            assertThat(index.name()).isEqualTo("idx_nx_otp_challenge_lookup");
            assertThat(index.columnList()).contains("purpose", "channel", "destination");
        });
    }

    @Test
    void otpChallengeStoresHashedVerifier() throws NoSuchFieldException {
        assertPersistenceId(OtpChallengeEntity.class.getDeclaredField("challengeId"));
        assertField(OtpChallengeEntity.class, "purpose", String.class, 32, false);
        assertField(OtpChallengeEntity.class, "channel", String.class, 32, false);
        assertField(OtpChallengeEntity.class, "destination", String.class, 256, false);
        assertField(OtpChallengeEntity.class, "algorithm", String.class, 64, false);
        assertField(OtpChallengeEntity.class, "codeVerifier", String.class, 512, false);
        assertField(OtpChallengeEntity.class, "expiresAt", LocalDateTime.class, 255, false);
        assertField(OtpChallengeEntity.class, "consumedAt", LocalDateTime.class, 255, true);
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
