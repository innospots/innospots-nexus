package com.innospots.nexus.console.logger.operator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.innospots.nexus.console.logger.dao.AuditLogDao;
import com.innospots.nexus.console.logger.domain.context.InvocationLogContext;
import com.innospots.nexus.console.logger.domain.entity.AuditLogEntity;

class InvocationLogOperatorTest {

    @Test
    void persistsLocalizedOperationMessageAndHttpPath() {
        AuditLogDao auditLogDao = mock(AuditLogDao.class);
        InvocationLogOperator operator = new InvocationLogOperator(auditLogDao);
        InvocationLogContext context = new InvocationLogContext(
                "com.example.Endpoint",
                "create",
                "dictionary.type.create",
                new Object[0],
                null,
                null,
                System.currentTimeMillis(),
                System.currentTimeMillis() + 1,
                "alice",
                "创建字典类型",
                "POST /platform/dictionary-types");

        operator.record(context);

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogDao).insert(captor.capture());
        AuditLogEntity entity = captor.getValue();
        assertThat(entity.getMessage()).isEqualTo("创建字典类型");
        assertThat(entity.getPath()).isEqualTo("POST /platform/dictionary-types");
        assertThat(entity.getAction()).isEqualTo("dictionary.type.create");
        assertThat(entity.getActor()).isEqualTo("alice");
        assertThat(entity.getDurationMs()).isEqualTo(1L);
    }
}
