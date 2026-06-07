package com.innospots.nexus.core.server;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ServerContractsTest {

    @Test
    void definesServiceNodeModel() {
        ServiceInfo service = ServiceInfo.named("nexus-scheduler", "scheduler-1")
                .host("192.168.1.10")
                .port(8080)
                .status(ServiceStatus.ONLINE)
                .role(ServiceRole.FOLLOWER)
                .group("default")
                .tags("zone=cn-east,env=prod")
                .metrics(Map.of("cpu", "12%"));

        assertThat(service.serviceName()).isEqualTo("nexus-scheduler");
        assertThat(service.host()).isEqualTo("192.168.1.10");
        assertThat(service.port()).isEqualTo(8080);
        assertThat(service.status()).isEqualTo(ServiceStatus.ONLINE);
        assertThat(service.role()).isEqualTo(ServiceRole.FOLLOWER);
        assertThat(service.group()).isEqualTo("default");
        assertThat(service.serverKey()).isEqualTo("192.168.1.10:8080");
        assertThat(service.tags()).containsEntry("zone", "cn-east");
    }

    @Test
    void lifecycleControlsServerRunningState() {
        ServiceLifecycle lifecycle = new ServiceLifecycle();

        assertThat(lifecycle.isRunning()).isTrue();
        lifecycle.shutdown();
        assertThat(lifecycle.isRunning()).isFalse();
        assertThat(lifecycle.isShutdown()).isTrue();
    }

    @Test
    void lifecycleTracksRegistryMetadata() {
        ServiceLifecycle lifecycle = new ServiceLifecycle();

        lifecycle.setServerKey("192.168.1.10:8080");
        lifecycle.setLeader(true);

        assertThat(lifecycle.serverKey()).isEqualTo("192.168.1.10:8080");
        assertThat(lifecycle.isRegistered()).isTrue();
        assertThat(lifecycle.isLeader()).isTrue();
    }
}
