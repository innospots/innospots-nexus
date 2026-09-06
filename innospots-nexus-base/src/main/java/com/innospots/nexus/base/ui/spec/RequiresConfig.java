package com.innospots.nexus.base.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Runtime and component capability requirements declared by {@link PageDsl#requires}.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RequiresConfig {

    /** Required runtime semantic version range, for example {@code >=0.1.0}. */
    private String runtime;

    /** Required component versions keyed by component type name. */
    private Map<String, String> components = new LinkedHashMap<>();
}
