package com.innospots.nexus.core.extension.declaration;

import java.util.List;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * Immutable module boundary containing page ownership and navigation trees.
 * Permission role assignments are managed outside the extension declaration.
 *
 * @param moduleKey globally unique module key
 * @param displayName internationalized display name
 * @param description internationalized description
 * @param pages page declaration roots
 * @param menuTree menu declaration roots
 */
public record ExtensionModuleDeclaration(
        String moduleKey,
        I18nObject displayName,
        I18nObject description,
        List<UiSpecPageDeclaration> pages,
        List<MenuDeclaration> menuTree
) {

    /** Creates a validated module declaration with immutable child lists. */
    public ExtensionModuleDeclaration {
        requireText(moduleKey, "moduleKey");
        if (displayName == null || displayName.isEmpty()) {
            invalid("displayName");
        }
        if (description == null || description.isEmpty()) {
            invalid("description");
        }
        displayName = I18nObject.of(displayName);
        description = I18nObject.of(description);
        pages = pages == null ? List.of() : List.copyOf(pages);
        menuTree = menuTree == null ? List.of() : List.copyOf(menuTree);
    }

    /** Returns the stable module resource ID exposed to the permission catalog. */
    public String resourceKey() {
        return "module:" + moduleKey;
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            invalid(field);
        }
    }

    private static void invalid(String field) {
        throw NexusException.build(
                NexusStatusCode.INVALID_PARAMETER.fullCode(),
                field + " is required");
    }
}
