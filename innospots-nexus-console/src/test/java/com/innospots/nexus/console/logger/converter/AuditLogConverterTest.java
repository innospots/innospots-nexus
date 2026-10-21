package com.innospots.nexus.console.logger.converter;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.logger.domain.entity.AuditLogEntity;
import com.innospots.nexus.console.logger.domain.vo.AuditLogVo;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogConverterTest {

    @Test
    void mapsOperationFieldsWithoutConflatingTypeTargetAndMethod() {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setAuditLogId("alg-1");
        entity.setAction("CREATE");
        entity.setTarget("dictionary.type.create");
        entity.setPath("POST /platform/dictionary-types");
        entity.setMessage("创建字典类型");
        entity.setFailureDetail("java.lang.IllegalStateException: boom");
        entity.setOperatedTime(LocalDateTime.of(2026, 10, 21, 12, 30));
        entity.setActor("alice");
        entity.setExecutionResult("FAILURE");
        entity.setDurationMs(12L);

        AuditLogVo vo = AuditLogConverter.toVo(entity);

        assertThat(vo.operationType()).isEqualTo("CREATE");
        assertThat(vo.target()).isEqualTo("dictionary.type.create");
        assertThat(vo.method()).isEqualTo("POST /platform/dictionary-types");
        assertThat(vo.summary()).isEqualTo("创建字典类型");
        assertThat(vo.failureDetail()).isEqualTo("java.lang.IllegalStateException: boom");
    }

    @Test
    void fallsBackToLegacyActionForRowsWithoutTarget() {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setAction("dictionary.type.create");

        AuditLogVo vo = AuditLogConverter.toVo(entity);

        assertThat(vo.target()).isEqualTo("dictionary.type.create");
    }
}
