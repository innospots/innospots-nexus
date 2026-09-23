package com.innospots.nexus.core.plugin.contribution.console.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.permission.PermissionConfig;

import lombok.Getter;
import lombok.Setter;

/**
 * 由 {@link PageDsl#page} 声明的页面标识与展示元数据。
 *
 * <p>{@link #id} 为必填，应使用 kebab-case。{@link #name} 为可选，存在时应使用 camelCase。</p>
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PageMeta {

    /** kebab-case 唯一页面标识，例如 {@code customer-list}。 */
    private String id;

    /** camelCase 程序化页面名称，例如 {@code customerList}。 */
    private String name;

    /** 人类可读的页面标题。 */
    private String title;

    /** 供控制台与工具使用的可选页面描述。 */
    private String description;

    /** 页面模式，如 {@code list}、{@code detail}、{@code form} 或 {@code dashboard}。 */
    private String type;

    /** 可选的页面级访问权限。 */
    private PermissionConfig permission;

    /** 创建空页面元数据。 */
    public PageMeta() {
    }

    /**
     * 创建带必填标识的页面元数据。
     *
     * @param id kebab-case 唯一页面标识
     * @return 页面元数据
     */
    public static PageMeta of(String id) {
        PageMeta meta = new PageMeta();
        meta.id = id;
        return meta;
    }

    /**
     * 创建带标识与标题的页面元数据。
     *
     * @param id 唯一页面标识
     * @param title 展示标题
     * @return 页面元数据
     */
    public static PageMeta of(String id, String title) {
        PageMeta meta = of(id);
        meta.title = title;
        return meta;
    }
}
