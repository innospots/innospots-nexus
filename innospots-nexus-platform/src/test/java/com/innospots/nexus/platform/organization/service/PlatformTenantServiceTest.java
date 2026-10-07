package com.innospots.nexus.platform.organization.service;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.events.EventBus;
import com.innospots.nexus.platform.organization.domain.entity.TenantEntity;
import com.innospots.nexus.platform.organization.domain.enums.TenantType;
import com.innospots.nexus.platform.organization.domain.event.TenantCreatedEvent;
import com.innospots.nexus.platform.organization.domain.request.TenantCreateRequest;
import com.innospots.nexus.platform.organization.operator.EnterpriseOperator;
import com.innospots.nexus.platform.organization.operator.TenantOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

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
                TenantType.TEAM.name(),
                null,
                null);

        new PlatformTenantService(tenantOperator, mock(EnterpriseOperator.class)).createTenant(request);

        assertThat(captured.get()).isNotNull();
        assertThat(captured.get().tenantId()).isEqualTo("tnt01");
        assertThat(captured.get().tenantCode()).isEqualTo("acme");
        assertThat(captured.get().eventType()).isEqualTo("platform.organization.created");
    }
}
