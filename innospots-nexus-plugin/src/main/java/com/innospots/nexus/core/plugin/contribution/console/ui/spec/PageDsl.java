package com.innospots.nexus.core.plugin.contribution.console.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionOrList;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource.DataSourceConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson.ActionOrListMapDeserializer;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson.ChildrenDeserializer;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson.DslRenderableDeserializer;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson.DslRenderableMapDeserializer;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.Children;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson.DslNodeDeserializer;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslRenderable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Pactor Page DSL 1.0 的根文档。
 *
 * <p>对应规范中描述的顶层 YAML 对象：</p>
 * <pre>
 * dsl, requires, page, meta, state, dataSources, actions, components,
 * lifecycle, body, children
 * </pre>
 *
 * <p>完整页面优先使用 {@code body}，部分 DSL 片段使用根级 {@code children}。本类型的可变集合
 * 用于 Jackson 绑定与程序化组装；诸如 {@link #state()} 的访问器方法返回防御性副本。</p>
 *
 * @see PageMeta
 * @see com.innospots.nexus.core.plugin.contribution.console.ui.spec.validation.PageDslValidator
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PageDsl {

    /** DSL 规范版本，必须为 {@link #SPEC_VERSION}。 */
    public static final String SPEC_VERSION = "1.0";

    /** 必填 DSL 版本字段（{@code dsl: '1.0'}）。 */
    private String dsl;

    /** 运行时与组件能力要求。 */
    private RequiresConfig requires;

    /** 必填的页面标识与展示元数据。 */
    private PageMeta page;

    /** 供工具使用的文档元数据；运行时不得依赖此对象。 */
    private Map<String, Object> meta = new LinkedHashMap<>();

    /** 页面初始状态，运行时通过 {@link #bindState(Map)} 更新。 */
    private Map<String, Object> state = new LinkedHashMap<>();

    /** 表达式中以 {@code ${data.name}} 引用的命名数据源。 */
    private Map<String, DataSourceConfig> dataSources = new LinkedHashMap<>();

    /** 通过 {@code call} 动作调用的命名可复用动作序列。 */
    @JsonDeserialize(using = ActionOrListMapDeserializer.class)
    private Map<String, ActionOrList> actions = new LinkedHashMap<>();

    /** 由 {@code component} 节点引用的命名可复用 UI 片段。 */
    @JsonDeserialize(using = DslRenderableMapDeserializer.class)
    private Map<String, DslRenderable> components = new LinkedHashMap<>();

    /** 页面生命周期钩子，如 {@code onInit} 与 {@code onLoad}。 */
    private LifecycleConfig lifecycle;

    /** 完整页面的首选 UI 根节点。 */
    @JsonDeserialize(using = DslNodeDeserializer.class)
    private com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslNode body;

    /** 无 {@code body} 时，部分 DSL 文档的替代根节点。 */
    @JsonDeserialize(using = ChildrenDeserializer.class)
    private Children children;

    /** 创建空页面 DSL，用于反序列化或组装。 */
    public PageDsl() {
    }

    /**
     * 创建空的页面 DSL 文档。
     *
     * @return 空文档
     */
    public static PageDsl create() {
        return new PageDsl();
    }

    /**
     * 创建带必填版本与页面元数据的页面 DSL。
     *
     * @param page 页面元数据
     * @return 页面 DSL
     */
    public static PageDsl of(PageMeta page) {
        PageDsl document = new PageDsl();
        document.dsl = SPEC_VERSION;
        document.page = page;
        return document;
    }

    /**
     * 将运行时值浅合并到页面状态。
     *
     * <p>与 Pactor {@code setState} 语义一致：嵌套对象整体替换，而非深度合并。</p>
     *
     * @param values 待合并的状态值
     */
    public void bindState(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        state.putAll(values);
    }

    /**
     * 返回初始状态映射的不可变视图。
     *
     * @return 页面状态
     */
    public Map<String, Object> state() {
        if (state == null || state.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(state));
    }

    /**
     * 返回命名数据源的不可变视图。
     *
     * @return 数据源
     */
    public Map<String, DataSourceConfig> dataSources() {
        return Map.copyOf(dataSources);
    }

    /**
     * 返回命名页面动作的不可变视图。
     *
     * @return 页面动作
     */
    public Map<String, ActionOrList> actions() {
        return Map.copyOf(actions);
    }

    /**
     * 返回命名可复用组件的不可变视图。
     *
     * @return 命名组件
     */
    public Map<String, DslRenderable> components() {
        return Map.copyOf(components);
    }
}
