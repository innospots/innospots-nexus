package com.innospots.nexus.console.i18n.loader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.i18n.domain.vo.MessageBundleVo;
import com.innospots.nexus.console.i18n.status.I18nStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageBundleLoaderTest {

    @Test
    void mergesBaseFallbackAndLocaleOverride() {
        MessageBundleLoader loader = new MessageBundleLoader(new MapClassLoader(Map.of(
                "i18n/console/namespaces",
                "common\n",
                "i18n/console/common/messages.properties",
                "common.fallback=基础回退\ncommon.override=基础值\n",
                "i18n/console/common/messages_en.properties",
                "common.override=English override\ncommon.english=English only\n"
        )));

        MessageBundleVo bundle = loader.load("en-US", "common");

        assertThat(bundle.messages())
                .containsEntry("common.fallback", "基础回退")
                .containsEntry("common.override", "English override")
                .containsEntry("common.english", "English only");
    }

    @Test
    void rejectsKeysOutsideNamespacePrefix() {
        MessageBundleLoader loader = new MessageBundleLoader(new MapClassLoader(Map.of(
                "i18n/console/namespaces",
                "common\n",
                "i18n/console/common/messages.properties",
                "other.key=非法前缀\n"
        )));

        assertThatThrownBy(() -> loader.load("zh-CN", "common"))
                .isInstanceOf(NexusException.class)
                .extracting(error -> ((NexusException) error).code())
                .isEqualTo(I18nStatusCode.BUNDLE_LOAD_FAILED.fullCode());
    }

    @Test
    void readsNamespacesFromClasspathIndexInDeclaredOrder() {
        MessageBundleLoader loader = new MessageBundleLoader(new MapClassLoader(Map.of(
                "i18n/console/namespaces",
                "common\nrole\n",
                "i18n/console/common/messages.properties",
                "common.action.save=保存\n",
                "i18n/console/role/messages.properties",
                "role.role-list.body.title=角色管理\n"
        )));

        assertThat(loader.namespaces()).containsExactly("common", "role");
        assertThat(loader.loadAll("zh-CN"))
                .containsOnlyKeys("common", "role");
    }

    @Test
    void rejectsNamespacePathTraversal() {
        MessageBundleLoader loader = new MessageBundleLoader(new MapClassLoader(Map.of(
                "i18n/console/namespaces",
                "common\n"
        )));

        assertThatThrownBy(() -> loader.load("zh-CN", "../common"))
                .isInstanceOf(NexusException.class)
                .extracting(error -> ((NexusException) error).code())
                .isEqualTo(I18nStatusCode.BUNDLE_LOAD_FAILED.fullCode());
    }

    private static final class MapClassLoader extends ClassLoader {

        private final Map<String, byte[]> resources;

        private MapClassLoader(Map<String, String> resources) {
            super(null);
            this.resources = resources.entrySet().stream()
                    .collect(java.util.stream.Collectors.toUnmodifiableMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            byte[] resource = resources.get(name);
            return resource == null ? null : new ByteArrayInputStream(resource);
        }
    }
}
