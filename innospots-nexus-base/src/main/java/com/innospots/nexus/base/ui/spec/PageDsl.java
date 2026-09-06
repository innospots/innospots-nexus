package com.innospots.nexus.base.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.base.ui.spec.action.ActionOrList;
import com.innospots.nexus.base.ui.spec.datasource.DataSourceConfig;
import com.innospots.nexus.base.ui.spec.jackson.ActionOrListMapDeserializer;
import com.innospots.nexus.base.ui.spec.jackson.ChildrenDeserializer;
import com.innospots.nexus.base.ui.spec.jackson.DslRenderableDeserializer;
import com.innospots.nexus.base.ui.spec.jackson.DslRenderableMapDeserializer;
import com.innospots.nexus.base.ui.spec.node.Children;
import com.innospots.nexus.base.ui.spec.jackson.DslNodeDeserializer;
import com.innospots.nexus.base.ui.spec.node.DslRenderable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Root document for Pactor Page DSL 1.0.
 *
 * <p>Maps to the top-level YAML object described in the specification:</p>
 * <pre>
 * dsl, requires, page, meta, state, dataSources, actions, components,
 * lifecycle, body, children
 * </pre>
 *
 * <p>Prefer {@code body} for full pages and root-level {@code children} for partial DSL
 * fragments. The mutable collections on this type exist for Jackson binding and programmatic
 * assembly; accessor methods such as {@link #state()} return defensive copies.</p>
 *
 * @see PageMeta
 * @see com.innospots.nexus.base.ui.spec.validation.PageDslValidator
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PageDsl {

    /** DSL specification version. Must be {@link #SPEC_VERSION}. */
    public static final String SPEC_VERSION = "1.0";

    /** Required DSL version field ({@code dsl: '1.0'}). */
    private String dsl;

    /** Runtime and component capability requirements. */
    private RequiresConfig requires;

    /** Required page identity and display metadata. */
    private PageMeta page;

    /** Document metadata for tooling; runtime must not depend on this object. */
    private Map<String, Object> meta = new LinkedHashMap<>();

    /** Initial page state. Updated at runtime through {@link #bindState(Map)}. */
    private Map<String, Object> state = new LinkedHashMap<>();

    /** Named data sources referenced as {@code ${data.name}} in expressions. */
    private Map<String, DataSourceConfig> dataSources = new LinkedHashMap<>();

    /** Named reusable action sequences invoked through {@code call} actions. */
    @JsonDeserialize(using = ActionOrListMapDeserializer.class)
    private Map<String, ActionOrList> actions = new LinkedHashMap<>();

    /** Named reusable UI fragments referenced by {@code component} nodes. */
    @JsonDeserialize(using = DslRenderableMapDeserializer.class)
    private Map<String, DslRenderable> components = new LinkedHashMap<>();

    /** Page lifecycle hooks such as {@code onInit} and {@code onLoad}. */
    private LifecycleConfig lifecycle;

    /** Preferred UI root for complete pages. */
    @JsonDeserialize(using = DslNodeDeserializer.class)
    private com.innospots.nexus.base.ui.spec.node.DslNode body;

    /** Alternative root for partial DSL documents when {@code body} is absent. */
    @JsonDeserialize(using = ChildrenDeserializer.class)
    private Children children;

    /** Creates an empty page DSL for deserialization or assembly. */
    public PageDsl() {
    }

    /**
     * Creates an empty page DSL document.
     *
     * @return empty document
     */
    public static PageDsl create() {
        return new PageDsl();
    }

    /**
     * Creates a page DSL with the required version and page metadata.
     *
     * @param page page metadata
     * @return page DSL
     */
    public static PageDsl of(PageMeta page) {
        PageDsl document = new PageDsl();
        document.dsl = SPEC_VERSION;
        document.page = page;
        return document;
    }

    /**
     * Shallow-merges runtime values into the page state.
     *
     * <p>Matches Pactor {@code setState} semantics: nested objects are replaced as a whole
     * rather than deep-merged.</p>
     *
     * @param values state values to merge
     */
    public void bindState(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        state.putAll(values);
    }

    /**
     * Returns an immutable view of the initial state map.
     *
     * @return page state
     */
    public Map<String, Object> state() {
        if (state == null || state.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(state));
    }

    /**
     * Returns an immutable view of named data sources.
     *
     * @return data sources
     */
    public Map<String, DataSourceConfig> dataSources() {
        return Map.copyOf(dataSources);
    }

    /**
     * Returns an immutable view of named page actions.
     *
     * @return page actions
     */
    public Map<String, ActionOrList> actions() {
        return Map.copyOf(actions);
    }

    /**
     * Returns an immutable view of named reusable components.
     *
     * @return named components
     */
    public Map<String, DslRenderable> components() {
        return Map.copyOf(components);
    }
}
