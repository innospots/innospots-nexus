package com.innospots.nexus.console.logger.operator;

import static org.assertj.core.api.Assertions.assertThat;
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
                "CREATE",
                new Object[0],
                null,
                null,
                System.currentTimeMillis(),
                System.currentTimeMillis() + 1,
                "alice",
                "创建字典类型",
                "POST /platform/dictionary-types",
                "dictionary.type.create");

        operator.record(context);

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogDao).insert(captor.capture());
        AuditLogEntity entity = captor.getValue();
        assertThat(entity.getMessage()).isEqualTo("创建字典类型");
        assertThat(entity.getPath()).isEqualTo("POST /platform/dictionary-types");
        assertThat(entity.getAction()).isEqualTo("CREATE");
        assertThat(entity.getTarget()).isEqualTo("dictionary.type.create");
        assertThat(entity.getActor()).isEqualTo("alice");
        assertThat(entity.getDurationMs()).isEqualTo(1L);
        assertThat(entity.getFailureDetail()).isNull();
    }

    @Test
    void keepsOperationMessageAndStoresFailureDetailSeparately() {
        AuditLogDao auditLogDao = mock(AuditLogDao.class);
        InvocationLogOperator operator = new InvocationLogOperator(auditLogDao);
        InvocationLogContext context = new InvocationLogContext(
                "com.example.Endpoint",
                "delete",
                "DELETE",
                new Object[0],
                null,
                new IllegalStateException("boom"),
                System.currentTimeMillis(),
                System.currentTimeMillis() + 2,
                "alice",
                "删除字典类型",
                "DELETE /platform/dictionary-types/missing",
                "dictionary.type.delete");

        operator.record(context);

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogDao).insert(captor.capture());
        AuditLogEntity entity = captor.getValue();
        assertThat(entity.getAction()).isEqualTo("DELETE");
        assertThat(entity.getTarget()).isEqualTo("dictionary.type.delete");
        assertThat(entity.getMessage()).isEqualTo("删除字典类型");
        assertThat(entity.getFailureDetail())
                .contains("java.lang.IllegalStateException")
                .contains("boom");
    }
}
