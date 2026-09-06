package com.innospots.nexus.base.ui.spec.node;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Service-backed dynamic DSL source.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class DslServiceSource implements DslSource {

    private String type = "service";
    private String service;
    private Map<String, Object> params = new LinkedHashMap<>();
}
