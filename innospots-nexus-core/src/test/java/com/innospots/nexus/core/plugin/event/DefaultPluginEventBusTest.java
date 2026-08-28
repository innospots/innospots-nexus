package com.innospots.nexus.core.plugin.event;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.resource.DefaultResourceScope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultPluginEventBusTest {

    @Test
    void isolatesObserverFailuresAndRemovesScopedSubscriptions() {
        DefaultPluginEventBus bus = new DefaultPluginEventBus();
        DefaultResourceScope scope = new DefaultResourceScope();
        AtomicInteger calls = new AtomicInteger();
        PluginEventBus scoped = bus.scoped(scope);
        scoped.subscribe(SampleEvent.class, event -> {
            throw new RuntimeException("expected observer failure");
        });
        scoped.subscribe(SampleEvent.class, event -> calls.incrementAndGet());

        bus.publish(new SampleEvent());
        assertThat(calls).hasValue(1);

        scope.close();
        bus.publish(new SampleEvent());
        assertThat(calls).hasValue(1);
    }

    @Test
    void doesNotLeakSubscriptionWhenItsResourceScopeIsAlreadyClosed() {
        DefaultPluginEventBus bus = new DefaultPluginEventBus();
        DefaultResourceScope scope = new DefaultResourceScope();
        AtomicInteger calls = new AtomicInteger();
        scope.close();

        assertThatThrownBy(() -> bus.scoped(scope)
                .subscribe(SampleEvent.class, event -> calls.incrementAndGet()))
                .isInstanceOf(RuntimeException.class);

        bus.publish(new SampleEvent());
        assertThat(calls).hasValue(0);
    }

    @Test
    void closesSubscriberReferencesAndRejectsNewSubscriptions() {
        DefaultPluginEventBus bus = new DefaultPluginEventBus();
        AtomicInteger calls = new AtomicInteger();
        bus.subscribe(SampleEvent.class, event -> calls.incrementAndGet());

        bus.close();
        bus.publish(new SampleEvent());

        assertThat(calls).hasValue(0);
        assertThatThrownBy(() -> bus.subscribe(SampleEvent.class, event -> calls.incrementAndGet()))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void rejectsNullScopedResourceOwnership() {
        DefaultPluginEventBus bus = new DefaultPluginEventBus();

        assertThatThrownBy(() -> bus.scoped(null))
                .isInstanceOf(NexusException.class);
    }

    private record SampleEvent() implements PluginEvent {
    }
}
