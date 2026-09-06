package com.innospots.nexus.base.ui.spec.node;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.base.ui.spec.jackson.DslNodeDeserializer;

import lombok.Getter;
import lombok.Setter;

/**
 * Dynamic DSL fragment loaded from service or HTTP sources.
 *
 * <p>Loads UI structure rather than business data. Use {@link #placeholder} to render a
 * fallback node while the remote DSL is loading.</p>
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class DslSourceRef implements DslRenderable {

    private DslSource source;

    @JsonDeserialize(using = DslNodeDeserializer.class)
    private DslNode placeholder;
}
