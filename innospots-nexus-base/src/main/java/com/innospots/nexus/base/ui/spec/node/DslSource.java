package com.innospots.nexus.base.ui.spec.node;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.innospots.nexus.base.ui.spec.HttpRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dynamic DSL source definition for {@link DslSourceRef}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DslServiceSource.class, name = "service"),
        @JsonSubTypes.Type(value = DslHttpSource.class, name = "http")
})
public sealed interface DslSource permits DslServiceSource, DslHttpSource {

    /**
     * Returns the source type discriminator.
     *
     * @return source type
     */
    String getType();
}
