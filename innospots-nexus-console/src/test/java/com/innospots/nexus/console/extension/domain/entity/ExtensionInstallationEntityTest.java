package com.innospots.nexus.console.extension.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExtensionInstallationEntityTest {

    @Test
    void definesPersistentInstallationIdentityAndStateIndexes() throws NoSuchFieldException {
        assertThat(ExtensionInstallationEntity.class.getAnnotation(Entity.class)).isNotNull();
        assertThat(ExtensionInstallationEntity.class.getAnnotation(TableName.class).value())
                .isEqualTo(ExtensionInstallationEntity.TABLE_NAME);

        Table table = ExtensionInstallationEntity.class.getAnnotation(Table.class);
        assertThat(table.name()).isEqualTo(ExtensionInstallationEntity.TABLE_NAME);
        assertThat(table.indexes())
                .extracting(Index::columnList)
                .contains("extension_key", "state");

        TableId tableId = ExtensionInstallationEntity.class
                .getDeclaredField("installationId")
                .getAnnotation(TableId.class);
        assertThat(tableId.type()).isEqualTo(IdType.ASSIGN_UUID);
        assertThat(new ExtensionInstallationEntity().idPrefix()).isEqualTo("ext");
    }
}
