package com.innospots.nexus.console.endpoint.grant.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

class EndpointGrantPathMatcherTest {

    @Test
    void matchesRejectsBlankOrNullInputs(TestReporter reporter) {
        assertMatch(reporter, null, "/a", false);
        assertMatch(reporter, "  ", "/a", false);
        assertMatch(reporter, "/a", null, false);
        assertMatch(reporter, "/a", "  ", false);
    }

    @Test
    void matchesJaxRsSingleSegmentPlaceholder(TestReporter reporter) {
        String template = "/api/test/dictionary/types/{dictionaryTypeId}";
        assertMatch(reporter, template, "/api/test/dictionary/types/abc", true);
        assertMatch(reporter, template, "api/test/dictionary/types/abc", true);
        assertMatch(reporter, template, "/api/test/dictionary/types/abc/extra", false);
        assertMatch(reporter, template, "/api/test/dictionary/types", false);
    }

    @Test
    void matchesNormalizesTemplateWithoutLeadingSlash(TestReporter reporter) {
        assertMatch(reporter, "health", "/health", true);
        assertMatch(reporter, "health", "health", true);
    }

    @Test
    void matchesMultiplePlaceholders(TestReporter reporter) {
        String template = "/orgs/{orgId}/projects/{projectId}";
        assertMatch(reporter, template, "/orgs/o1/projects/p9", true);
        assertMatch(reporter, template, "/orgs/o1/projects/p9/logs", false);
    }

    @Test
    void matchesLiteralPath(TestReporter reporter) {
        assertMatch(reporter, "/api/test/dictionary/types", "/api/test/dictionary/types", true);
        assertMatch(reporter, "/api/test/dictionary/types", "/api/test/dictionary/types/extra", false);
    }

    private static void assertMatch(
            TestReporter reporter,
            String template,
            String requestPath,
            boolean expected
    ) {
        boolean actual = EndpointGrantPathMatcher.matches(template, requestPath);
        reporter.publishEntry("input", "template='" + template + "', requestPath='" + requestPath + "'");
        reporter.publishEntry("output", "matches=" + actual + ", expected=" + expected);
        assertThat(actual).isEqualTo(expected);
    }
}
