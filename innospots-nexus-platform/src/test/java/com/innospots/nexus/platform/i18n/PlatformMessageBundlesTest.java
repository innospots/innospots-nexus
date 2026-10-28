package com.innospots.nexus.platform.i18n;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.i18n.domain.vo.MessageBundleVo;
import com.innospots.nexus.console.i18n.loader.MessageBundleLoader;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformMessageBundlesTest {

    private static final Pattern DSL_PATH_SEGMENT = Pattern.compile(
            "\\.(body|children|props|events|columns)\\.");

    private static final List<String> EXPECTED_NAMESPACES = List.of(
            "access",
            "audit",
            "auth",
            "common",
            "dictionary",
            "invite",
            "menu",
            "organization",
            "platform-user",
            "plugin",
            "role");

    private final MessageBundleLoader loader = new MessageBundleLoader();

    @Test
    void packagesAllConsoleBundlesInPlatformResources() {
        assertThat(loader.namespaces()).containsExactlyElementsOf(EXPECTED_NAMESPACES);

        Map<String, MessageBundleVo> chinese = loader.loadAll("zh-CN");
        Map<String, MessageBundleVo> english = loader.loadAll("en-US");

        assertThat(chinese).containsOnlyKeys(EXPECTED_NAMESPACES);
        assertThat(english).containsOnlyKeys(EXPECTED_NAMESPACES);
        for (String namespace : EXPECTED_NAMESPACES) {
            assertThat(chinese.get(namespace).messages())
                    .as("Chinese bundle %s", namespace)
                    .isNotEmpty()
                    .allSatisfy((key, value) -> {
                        assertThat(key).startsWith(namespace + ".");
                        assertThat(DSL_PATH_SEGMENT.matcher(key).find())
                                .as("DSL path key in %s: %s", namespace, key)
                                .isFalse();
                    });
            assertThat(english.get(namespace).messages())
                    .as("English bundle %s", namespace)
                    .isNotEmpty()
                    .containsOnlyKeys(chinese.get(namespace).messages().keySet());
        }

        assertThat(chinese.get("common").messages())
                .containsKeys(
                        "common.page.login.title",
                        "common.ui.cancel");
        assertThat(chinese.get("role").messages())
                .containsKey("role.role-list.title");
    }
}
