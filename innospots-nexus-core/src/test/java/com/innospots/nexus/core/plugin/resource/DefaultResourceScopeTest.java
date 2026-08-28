package com.innospots.nexus.core.plugin.resource;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultResourceScopeTest {

    @Test
    void closesInReverseOrderAndDoesNotRepeatAnEarlyRegistration() {
        List<String> calls = new ArrayList<>();
        DefaultResourceScope scope = new DefaultResourceScope();
        ResourceRegistration first = scope.add(() -> calls.add("first"));
        scope.add(() -> calls.add("second"));

        first.close();
        first.close();
        scope.close();
        scope.close();

        assertThat(calls).containsExactly("first", "second");
    }

    @Test
    void continuesClosingAfterOneDisposerFails() {
        List<String> calls = new ArrayList<>();
        DefaultResourceScope scope = new DefaultResourceScope();
        scope.add(() -> calls.add("last"));
        scope.add(() -> {
            calls.add("failure");
            throw new RuntimeException("expected");
        });
        scope.add(() -> calls.add("first"));

        assertThatThrownBy(scope::close).isInstanceOf(NexusException.class);
        assertThat(calls).containsExactly("first", "failure", "last");
    }

    @Test
    void closesManagedResourceWhenRegistrationIsRejected() {
        List<String> calls = new ArrayList<>();
        DefaultResourceScope scope = new DefaultResourceScope();
        scope.close();

        assertThatThrownBy(() -> scope.manage((AutoCloseable) () -> calls.add("closed")))
                .isInstanceOf(NexusException.class);

        assertThat(calls).containsExactly("closed");
    }
}
