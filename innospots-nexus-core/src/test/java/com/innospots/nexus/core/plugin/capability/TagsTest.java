package com.innospots.nexus.core.plugin.capability;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.support.PluginTestLog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TagsTest {

    private final PluginTestLog log = new PluginTestLog(TagsTest.class, "tags");

    @Test
    void matchesSubsetTagsDeterministically() {
        Tags provider = Tags.of("channel", "wecom")
                .and("provider", "tencent")
                .and("mode", "app");

        log.info("provider tags=%s", provider);
        log.info("request channel=wecom matches=%s", provider.matches(Tags.of("channel", "wecom")));
        log.info("request mode=app matches=%s", provider.matches(Tags.of("mode", "app")));
        log.info("request provider=custom matches=%s", provider.matches(Tags.of("provider", "custom")));

        assertThat(provider.matches(Tags.of("channel", "wecom"))).isTrue();
        assertThat(provider.matches(Tags.of("mode", "app"))).isTrue();
        assertThat(provider.matches(Tags.empty())).isTrue();
        assertThat(provider.matches(Tags.of("provider", "custom"))).isFalse();
        assertThat(provider.matches(null)).isFalse();
    }

    @Test
    void keepsTagsSortedAndImmutable() {
        Tags tags = Tags.of("zeta", "last")
                .and("alpha", "first")
                .and("beta", "middle");

        log.dumpMap("sorted tags", tags.asMap());

        assertThat(tags.asMap().keySet()).containsExactly("alpha", "beta", "zeta");
        assertThatThrownBy(() -> tags.asMap().put("gamma", "new"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsConflictingTagValuesAndInvalidParts() {
        Tags base = Tags.of("channel", "wecom");

        assertThatThrownBy(() -> base.and("channel", "dingtalk"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("channel");

        assertThatThrownBy(() -> Tags.of("Channel", "wecom"))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> Tags.from(null))
                .isInstanceOf(NexusException.class);
        assertThat(Tags.from(Map.of("channel", "WeCom")).get("channel"))
                .contains("WeCom");
        assertThatThrownBy(() -> Tags.from(Map.of("channel.name", "")))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void treatsEmptyTagsAsUniversalMatch() {
        Tags empty = Tags.empty();

        log.info("empty tags=%s isEmpty=%s", empty, empty.isEmpty());

        assertThat(empty.isEmpty()).isTrue();
        assertThat(empty.matches(Tags.empty())).isTrue();
        assertThat(Tags.of("channel", "wecom").matches(empty)).isTrue();
    }
}
