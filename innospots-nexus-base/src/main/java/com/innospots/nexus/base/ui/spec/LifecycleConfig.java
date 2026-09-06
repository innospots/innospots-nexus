package com.innospots.nexus.base.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.base.ui.spec.action.ActionOrList;
import com.innospots.nexus.base.ui.spec.jackson.ActionOrListDeserializer;

import lombok.Getter;
import lombok.Setter;

/**
 * Page lifecycle hooks expressed as action sequences.
 *
 * <p>Each hook accepts one {@link com.innospots.nexus.base.ui.spec.action.ActionConfig} or an
 * ordered action list, matching the {@code ActionOrList} schema union.</p>
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LifecycleConfig {

    @JsonDeserialize(using = ActionOrListDeserializer.class)
    private ActionOrList onInit;

    @JsonDeserialize(using = ActionOrListDeserializer.class)
    private ActionOrList onLoad;

    @JsonDeserialize(using = ActionOrListDeserializer.class)
    private ActionOrList onReady;

    @JsonDeserialize(using = ActionOrListDeserializer.class)
    private ActionOrList onShow;

    @JsonDeserialize(using = ActionOrListDeserializer.class)
    private ActionOrList onHide;

    @JsonDeserialize(using = ActionOrListDeserializer.class)
    private ActionOrList onDestroy;
}
