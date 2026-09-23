package com.innospots.nexus.core.plugin.contribution.console.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionOrList;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson.ActionOrListDeserializer;

import lombok.Getter;
import lombok.Setter;

/**
 * 以动作序列表达的页面生命周期钩子。
 *
 * <p>每个钩子接受一个
 * {@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionConfig}
 * 或有序动作列表，与 {@code ActionOrList} schema 联合类型一致。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LifecycleConfig {

    /** 页面初始化时执行的动作序列。 */
    @JsonDeserialize(using = ActionOrListDeserializer.class)
    private ActionOrList onInit;

    /** 页面加载时执行的动作序列。 */
    @JsonDeserialize(using = ActionOrListDeserializer.class)
    private ActionOrList onLoad;

    /** 页面就绪时执行的动作序列。 */
    @JsonDeserialize(using = ActionOrListDeserializer.class)
    private ActionOrList onReady;

    /** 页面显示时执行的动作序列。 */
    @JsonDeserialize(using = ActionOrListDeserializer.class)
    private ActionOrList onShow;

    /** 页面隐藏时执行的动作序列。 */
    @JsonDeserialize(using = ActionOrListDeserializer.class)
    private ActionOrList onHide;

    /** 页面销毁时执行的动作序列。 */
    @JsonDeserialize(using = ActionOrListDeserializer.class)
    private ActionOrList onDestroy;
}
