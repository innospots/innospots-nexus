package com.innospots.nexus.base.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    @Test
    void recognizesBlankTextAndDefaultValue() {
        assertThat(StringUtils.isBlank("  ")).isTrue();
        assertThat(StringUtils.isBlank("nexus")).isFalse();
        assertThat(StringUtils.defaultIfBlank(" ", "fallback")).isEqualTo("fallback");
    }

    @Test
    void replacesDollarAndBracePlaceholders() {
        String text = StringUtils.replacePlaceholders(
                "hello ${name}, project={{ projectId }}, missing=${missing}",
                Map.of("name", "nexus", "projectId", 7)
        );

        assertThat(text).isEqualTo("hello nexus, project=7, missing=${missing}");
    }

    @Test
    void nullValuesBecomeEmptyText() {
        Map<String, Object> values = new HashMap<>();
        values.put("name", null);

        String text = StringUtils.replacePlaceholders(
                "name=${name}",
                values
        );

        assertThat(text).isEqualTo("name=");
    }

    @Test
    void convertsBetweenCamelAndUnderscoreNames() {
        assertThat(StringUtils.camelToUnderscore("projectId")).isEqualTo("project_id");
        assertThat(StringUtils.camelToUnderscore("userProjectId")).isEqualTo("user_project_id");
        assertThat(StringUtils.underscoreToCamel("PROJECT_ID")).isEqualTo("projectId");
        assertThat(StringUtils.underscoreToCamel("user_project_id")).isEqualTo("userProjectId");
    }

    @Test
    void createsRandomKeyWithExpectedLengthAndAlphabet() {
        String key = StringUtils.randomKey(16);

        assertThat(key).hasSize(16);
        assertThat(key).matches("[1-9a-z]+");
    }
}
