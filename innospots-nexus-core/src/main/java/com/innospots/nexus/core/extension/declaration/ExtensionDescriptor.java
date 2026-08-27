package com.innospots.nexus.core.extension.declaration;

import java.util.List;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * Immutable identity and module declaration for an installable extension.
 * The descriptor contains no role, user, or authorization assignment.
 *
 * @param extensionKey stable globally unique extension key
 * @param version extension version
 * @param displayName internationalized display name
 * @param description internationalized description
 * @param modules modules contributed by the extension
 */
public record ExtensionDescriptor(
        String extensionKey,
        String version,
        I18nObject displayName,
        I18nObject description,
        List<ExtensionModuleDeclaration> modules
) {

    /** Creates a validated descriptor with defensive copies. */
    public ExtensionDescriptor {
        requireText(extensionKey, "extensionKey");
        requireText(version, "version");
        if (displayName == null || displayName.isEmpty()) {
            invalid("displayName");
        }
        if (description == null || description.isEmpty()) {
            invalid("description");
        }
        if (modules == null || modules.isEmpty()) {
            invalid("modules");
        }
        displayName = I18nObject.of(displayName);
        description = I18nObject.of(description);
        modules = modules == null ? List.of() : List.copyOf(modules);
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
