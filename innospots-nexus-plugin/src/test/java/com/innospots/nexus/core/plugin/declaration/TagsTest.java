package com.innospots.nexus.core.plugin.declaration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagsTest {

    @Test
    void mergesAndMatchesTags() {
        Tags tags = Tags.of("env", "test").and("region", "cn");
        assertThat(tags.matches(Tags.of("env", "test"))).isTrue();
        assertThat(tags.asMap()).containsEntry("region", "cn");
    }
}
