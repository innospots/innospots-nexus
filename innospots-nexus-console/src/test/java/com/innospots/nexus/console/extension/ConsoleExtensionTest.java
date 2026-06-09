package com.innospots.nexus.console.extension;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsoleExtensionTest {

    @Test
    void consoleContributionUsesImmutableDeclarations() {
        ConsoleMenuDeclaration menu = new ConsoleMenuDeclaration("system", "System", "/system", 10);
        ConsoleRouteDeclaration route = new ConsoleRouteDeclaration("system", "/system", "system-view");
        ConsoleContribution contribution = ConsoleContribution.of("kernel", "Kernel", List.of(menu), List.of(route));

        assertThat(contribution.extensionKey()).isEqualTo("kernel");
        assertThat(contribution.menus()).containsExactly(menu);
        assertThat(contribution.routes()).containsExactly(route);
        assertThatThrownBy(() -> contribution.menus().add(menu)).isInstanceOf(UnsupportedOperationException.class);
    }
}
