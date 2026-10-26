package com.innospots.nexus.console.i18n.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.i18n.domain.vo.MessageBundleSetVo;
import com.innospots.nexus.console.i18n.loader.MessageBundleLoader;
import com.innospots.nexus.console.i18n.status.I18nStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageBundleServiceTest {

    private final MessageBundleService service = new MessageBundleService(
            new MessageBundleLoader(new BundleClassLoader()));

    @Test
    void defaultsToAllIndexedChineseConsoleBundles() {
        MessageBundleSetVo result = service.load(
                null, null, null, null, null, null);

        assertThat(result.appId()).isEqualTo("console");
        assertThat(result.locale()).isEqualTo("zh-CN");
        assertThat(result.release()).isEqualTo("build-release");
        assertThat(result.format()).isEqualTo("simple-v1");
        assertThat(result.bundles()).containsOnlyKeys("common", "role", "dictionary");
        assertThat(result.bundles().get("common").messages())
                .containsEntry("common.action.save", "保存");
    }

    @Test
    void canonicalizesAliasesAndUsesRequestPrecedence() {
        MessageBundleSetVo result = service.load(
                "console", "en", "zh-CN", "zh-CN", "common", "rel-1");

        assertThat(result.locale()).isEqualTo("en-US");
        assertThat(result.release()).isEqualTo("rel-1");
        assertThat(result.bundles()).containsOnlyKeys("common");
        assertThat(result.bundles().get("common").messages())
                .containsEntry("common.action.save", "Save");
    }

    @Test
    void fallsBackFromCookieToAcceptedLanguage() {
        assertThat(service.load(null, null, "en_US", null, "common", null).locale())
                .isEqualTo("en-US");
        assertThat(service.load(null, null, "fr-FR", "en-US, en;q=0.9", "common", null).locale())
                .isEqualTo("en-US");
    }

    @Test
    void rejectsUnsupportedExplicitLocale() {
        assertThatThrownBy(() -> service.load(
                "console", "fr-FR", null, null, null, null))
                .isInstanceOf(NexusException.class)
                .extracting(error -> ((NexusException) error).code())
                .isEqualTo(I18nStatusCode.LOCALE_NOT_SUPPORTED.fullCode());
    }

    @Test
    void rejectsUnsupportedApplication() {
        assertThatThrownBy(() -> service.load(
                "portal", null, null, null, null, null))
                .isInstanceOf(NexusException.class)
                .extracting(error -> ((NexusException) error).code())
                .isEqualTo(I18nStatusCode.APP_ID_NOT_SUPPORTED.fullCode());
    }

    @Test
    void rejectsUnsupportedNamespace() {
        assertThatThrownBy(() -> service.load(
                "console", null, null, null, "common,unknown", null))
                .isInstanceOf(NexusException.class)
                .extracting(error -> ((NexusException) error).code())
                .isEqualTo(I18nStatusCode.NAMESPACE_NOT_SUPPORTED.fullCode());
    }

    @Test
    void exposesStableContentHashAndImmutableBundles() {
        MessageBundleSetVo first = service.load(
                "console", "zh-CN", null, null, "role", null);
        MessageBundleSetVo second = service.load(
                "console", "zh-CN", null, null, "role", null);

        assertThat(first.bundles().get("role").version())
                .isEqualTo(second.bundles().get("role").version())
                .matches("[0-9a-f]{64}");
        assertThat(first.bundles().get("role").messages())
                .containsKey("role.role-list.body.title");
        assertThatThrownBy(() -> first.bundles().put("x", null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsReleaseOutsideProtocolTokenAlphabet() {
        assertThatThrownBy(() -> service.load(
                "console", "en-US", null, null, "common", "release with spaces"))
                .isInstanceOf(NexusException.class)
                .extracting(error -> ((NexusException) error).code())
                .isEqualTo("AIO010001");
    }

    @Test
    void mergedMessagesContainNoCrossNamespaceKeyConflicts() {
        MessageBundleSetVo result = service.load(
                "console", "zh-CN", null, null, null, null);
        Map<String, String> owners = new java.util.HashMap<>();

        result.bundles().forEach((namespace, bundle) ->
                bundle.messages().keySet().forEach(key ->
                        assertThat(owners.put(key, namespace))
                                .as("duplicate key %s", key)
                                .isNull()));
    }

    private static final class BundleClassLoader extends ClassLoader {

        private static final Map<String, String> RESOURCES = Map.ofEntries(
                Map.entry("i18n/console/namespaces", "common\nrole\ndictionary\n"),
                Map.entry("i18n/console/common/messages.properties", "common.action.save=保存\n"),
                Map.entry("i18n/console/common/messages_en.properties", "common.action.save=Save\n"),
                Map.entry("i18n/console/role/messages.properties",
                        "role.role-list.body.title=角色管理\n"),
                Map.entry("i18n/console/role/messages_en.properties",
                        "role.role-list.body.title=Roles\n"),
                Map.entry("i18n/console/dictionary/messages.properties",
                        "dictionary.dictionary-type-list.body.title=数据字典\n"),
                Map.entry("i18n/console/dictionary/messages_en.properties",
                        "dictionary.dictionary-type-list.body.title=Dictionaries\n")
        );

        private BundleClassLoader() {
            super(null);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            String content = RESOURCES.get(name);
            return content == null
                    ? null
                    : new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        }
    }
}
