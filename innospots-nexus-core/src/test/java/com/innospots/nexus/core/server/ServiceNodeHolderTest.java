package com.innospots.nexus.core.server;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceNodeHolderTest {

    @Test
    void registersServiceAndDetectsLeader() {
        ServiceNodeHolder holder = new ServiceNodeHolder(60);

        ServiceInfo service = ServiceInfo.named("nexus-scheduler", "scheduler-1")
                .host("10.0.0.1")
                .port(8080)
                .role(ServiceRole.LEADER)
                .status(ServiceStatus.ONLINE);

        holder.register(service);

        assertThat(holder.isRegistered()).isTrue();
        assertThat(holder.isLeader()).isTrue();
        assertThat(holder.currentService().status()).isEqualTo(ServiceStatus.ONLINE);
    }

    @Test
    void detectsInvalidServiceAfterHeartbeatTimeout() {
        ServiceNodeHolder holder = new ServiceNodeHolder(1);

        ServiceInfo service = ServiceInfo.named("nexus-scheduler", "scheduler-1")
                .host("10.0.0.1")
                .port(8080)
                .role(ServiceRole.LEADER)
                .updatedTime(LocalDateTime.now().minusSeconds(120));

        assertThat(holder.isInvalid(service)).isTrue();
    }

    @Test
    void holdsShardingPositionAndServiceCount() {
        ServiceNodeHolder holder = new ServiceNodeHolder(60);

        holder.setPosition(2);
        holder.setAvailableServicesSize(5);

        assertThat(holder.position()).isEqualTo(2);
        assertThat(holder.availableServicesSize()).isEqualTo(5);
    }

    @Test
    void tracksStartupTime() {
        ServiceNodeHolder holder = new ServiceNodeHolder(60);

        holder.markStartup();
        assertThat(holder.startupTime()).isNotNull();
    }

    @Test
    void unregistersAndClearsState() {
        ServiceNodeHolder holder = new ServiceNodeHolder(60);

        holder.register(ServiceInfo.named("test", "test-1").host("10.0.0.1").port(8080));
        holder.unregister();

        assertThat(holder.isRegistered()).isFalse();
    }

    @Test
    void providesLifecycleForWatchers() {
        ServiceNodeHolder holder = new ServiceNodeHolder(60);

        assertThat(holder.lifecycle().isRunning()).isTrue();
        holder.lifecycle().shutdown();
        assertThat(holder.lifecycle().isRunning()).isFalse();
    }
}
