package com.innospots.nexus.core.plugin.contribution.console;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ConsolePluginContributionDecoderTest {

    private final ConsolePluginContributionDecoder decoder = new ConsolePluginContributionDecoder();

    @Test
    void decodesConsoleV2RootWithModules() {
        Map<String, Object> declaration = new LinkedHashMap<>();
        declaration.put("type", "console");
        declaration.put("majorVersion", 2);
        declaration.put("domainKey", "nexus");
        declaration.put("orderIndex", 0);
        Map<String, Object> menu = new LinkedHashMap<>();
        menu.put("displayName", Map.of("en", "Roles"));
        menu.put("orderIndex", 0);
        menu.put("entryPageKey", "nexus-role-main");
        menu.put("children", List.of());
        Map<String, Object> module = new LinkedHashMap<>();
        module.put("moduleKey", "role");
        module.put("displayName", Map.of("en", "Role"));
        module.put("orderIndex", 0);
        module.put("children", List.of(menu));
        declaration.put("modules", List.of(module));

        ConsolePluginContribution contribution = decoder.decode(declaration);

        assertThat(contribution.domainKey()).isEqualTo("nexus");
        assertThat(contribution.modules()).hasSize(1);
        assertThat(contribution.modules().getFirst().moduleKey()).isEqualTo("role");
        assertThat(contribution.modules().getFirst().children()).hasSize(1);
        assertThat(contribution.modules().getFirst().children().getFirst().entryPageKey())
                .isEqualTo("nexus-role-main");
    }

    @Test
    void decodesLeafModuleWithoutMenuChild() {
        Map<String, Object> declaration = new LinkedHashMap<>();
        declaration.put("type", "console");
        declaration.put("majorVersion", 2);
        declaration.put("domainKey", "nexus");
        declaration.put("orderIndex", 0);
        Map<String, Object> module = new LinkedHashMap<>();
        module.put("moduleKey", "role");
        module.put("displayName", Map.of("en", "Role"));
        module.put("orderIndex", 0);
        module.put("entryPageKey", "nexus-role-main");
        module.put("children", List.of());
        declaration.put("modules", List.of(module));

        ConsolePluginContribution contribution = decoder.decode(declaration);

        assertThat(contribution.modules()).singleElement().satisfies(decoded -> {
            assertThat(decoded.moduleKey()).isEqualTo("role");
            assertThat(decoded.entryPageKey()).isEqualTo("nexus-role-main");
            assertThat(decoded.children()).isEmpty();
        });
    }
}
