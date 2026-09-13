package com.innospots.nexus.core.plugin.contribution.console.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.permission.PermissionConfig;

import lombok.Getter;
import lombok.Setter;

/**
 * Page identity and display metadata declared by {@link PageDsl#page}.
 *
 * <p>{@link #id} is required and should use kebab-case. {@link #name} is optional and should
 * use camelCase when present.</p>
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PageMeta {

    /** Unique page identifier in kebab-case, for example {@code customer-list}. */
    private String id;

    /** Programmatic page name in camelCase, for example {@code customerList}. */
    private String name;

    /** Human-readable page title. */
    private String title;

    /** Optional page description for consoles and tooling. */
    private String description;

    /** Page mode such as {@code list}, {@code detail}, {@code form}, or {@code dashboard}. */
    private String type;

    /** Optional page-level access permission. */
    private PermissionConfig permission;

    /** Creates empty page metadata. */
    public PageMeta() {
    }

    /**
     * Creates page metadata with the required identifier.
     *
     * @param id unique page id in kebab-case
     * @return page metadata
     */
    public static PageMeta of(String id) {
        PageMeta meta = new PageMeta();
        meta.id = id;
        return meta;
    }

    /**
     * Creates page metadata with identifier and title.
     *
     * @param id unique page id
     * @param title display title
     * @return page metadata
     */
    public static PageMeta of(String id, String title) {
        PageMeta meta = of(id);
        meta.title = title;
        return meta;
    }
}
