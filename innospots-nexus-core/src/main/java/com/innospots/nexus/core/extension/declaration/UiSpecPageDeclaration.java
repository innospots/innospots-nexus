package com.innospots.nexus.core.extension.declaration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * Immutable identity and route declaration for a page backed by a UiSpec.
 * UiSpec content and its loading location are owned by the rendering runtime.
 *
 * @param pageKey module-local stable page key and UiSpec page ID
 * @param pagePath leading-slash path template
 * @param children direct child page declarations
 */
public record UiSpecPageDeclaration(
        String pageKey,
        String pagePath,
        List<UiSpecPageDeclaration> children
) {

    /** Creates a validated page declaration with immutable child pages. */
    public UiSpecPageDeclaration {
        requireText(pageKey, "pageKey");
        requireText(pagePath, "pagePath");
        if (!pagePath.startsWith("/")) {
            invalid("pagePath must start with '/'");
        }
        if (pagePath.contains("?") || pagePath.contains("#")) {
            invalid("pagePath must not contain query or fragment");
        }
        pagePath = normalizePath(pagePath);
        validatePath(pagePath);
        children = children == null ? List.of() : List.copyOf(children);
    }

    /** Returns the stable page resource ID for a module. */
    public String resourceKey(String moduleKey) {
        requireText(moduleKey, "moduleKey");
        return "page:" + moduleKey + "." + pageKey;
    }

    private static void validatePath(String path) {
        Set<String> variables = new HashSet<>();
        String value = path.length() == 1 ? "" : path.substring(1);
        if (value.isEmpty()) {
            return;
        }
        for (String segment : value.split("/", -1)) {
            boolean hasOpeningBrace = segment.indexOf('{') >= 0;
            boolean hasClosingBrace = segment.indexOf('}') >= 0;
            if (!hasOpeningBrace && !hasClosingBrace) {
                continue;
            }
            if (segment.length() < 3 || !segment.startsWith("{") || !segment.endsWith("}")) {
                invalid("pagePath variables must occupy a complete path segment");
            }
            String variable = segment.substring(1, segment.length() - 1);
            if (!variable.matches("[A-Za-z][A-Za-z0-9_]*") || !variables.add(variable)) {
                invalid("pagePath variable names must be unique and valid");
            }
        }
    }

    private static String normalizePath(String path) {
        return path.length() > 1 && path.endsWith("/")
                ? path.substring(0, path.length() - 1)
                : path;
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            invalid(field + " is required");
        }
    }

    private static void invalid(String message) {
        throw NexusException.build(
                NexusStatusCode.INVALID_PARAMETER.fullCode(),
                message);
    }
}
