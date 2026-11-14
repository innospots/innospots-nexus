package com.innospots.nexus.platform.organization.service;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.events.EventBus;
import com.innospots.nexus.console.scope.tenant.domain.entity.TenantEntity;
import com.innospots.nexus.console.scope.tenant.domain.enums.TenantType;
import com.innospots.nexus.console.scope.tenant.domain.event.TenantCreatedEvent;
import com.innospots.nexus.console.scope.tenant.domain.request.TenantCreateRequest;
import com.innospots.nexus.console.scope.tenant.domain.request.TenantStatusUpdateRequest;
import com.innospots.nexus.console.scope.tenant.domain.vo.TenantVo;
import com.innospots.nexus.platform.organization.operator.EnterpriseOperator;
import com.innospots.nexus.console.scope.tenant.operator.TenantOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformTenantServiceTest {

    @AfterEach
    void clearEventBus() {
        EventBus.clear();
    }

    @Test
    void createTenantPublishesTenantCreatedEvent() {
        TenantOperator tenantOperator = mock(TenantOperator.class);
        doAnswer(invocation -> {
            TenantEntity stored = invocation.getArgument(0);
            stored.setTenantId("tnt01");
            return null;
        }).when(tenantOperator).insert(any(TenantEntity.class));

        AtomicReference<TenantCreatedEvent> captured = new AtomicReference<>();
        EventBus.subscribe(TenantCreatedEvent.class, event -> {
            captured.set(event);
            return null;
        });

        TenantCreateRequest request = new TenantCreateRequest(
                "Acme",
                "acme",
                TenantType.TEAM.name());

        new PlatformTenantService(tenantOperator, mock(EnterpriseOperator.class)).createTenant(request);

        assertThat(captured.get()).isNotNull();
        assertThat(captured.get().tenantId()).isEqualTo("tnt01");
        assertThat(captured.get().tenantCode()).isEqualTo("acme");
        assertThat(captured.get().eventType()).isEqualTo("platform.organization.created");
    }

    @Test
    void updateTenantStatusStoresCanonicalLifecycleName() {
        TenantOperator tenantOperator = mock(TenantOperator.class);
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId("tnt01");
        tenant.setTenantName("Acme");
        tenant.setTenantCode("acme");
        tenant.setTenantType(TenantType.TEAM.name());
        when(tenantOperator.requireById("tnt01")).thenReturn(tenant);

        PlatformTenantService service = new PlatformTenantService(
                tenantOperator,
                mock(EnterpriseOperator.class));

        TenantVo vo = service.updateTenantStatus("tnt01", new TenantStatusUpdateRequest(" dissolved "));

        assertThat(vo.status()).isEqualTo("DISSOLVED");
        assertThat(tenant.getStatus()).isEqualTo("DISSOLVED");
        verify(tenantOperator).update(tenant);
    }
}
