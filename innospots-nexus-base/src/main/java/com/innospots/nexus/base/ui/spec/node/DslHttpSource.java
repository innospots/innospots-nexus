package com.innospots.nexus.base.ui.spec.node;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.ui.spec.HttpRequest;

import lombok.Getter;
import lombok.Setter;

/**
 * HTTP-backed dynamic DSL source.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class DslHttpSource implements DslSource {

    private String type = "http";
    private HttpRequest request;
}
