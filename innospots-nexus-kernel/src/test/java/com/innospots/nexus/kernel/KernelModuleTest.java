package com.innospots.nexus.kernel;

import com.innospots.nexus.console.api.ConsoleEndpoint;
import com.innospots.nexus.console.extension.ConsoleExtension;
import com.innospots.nexus.kernel.domain.KernelDomain;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KernelModuleTest {

    @Test
    void kernelDependsOnConsoleBoundary() {
        assertThat(new KernelModule().consoleEndpointType()).isEqualTo(ConsoleEndpoint.class);
    }

    @Test
    void kernelExposesConsoleExtensionBoundary() {
        assertThat(new KernelModule().consoleExtensionType()).isEqualTo(ConsoleExtension.class);
    }

    @Test
    void kernelDeclaresCoreBusinessDomains() {
        assertThat(new KernelModule().domains()).containsExactlyInAnyOrder(KernelDomain.values());
    }
}
