package com.innospots.nexus.base.status;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * Interface for a composable status code consisting of a 3-letter module
 * code, a {@link StatusCategory} (2 digits), and a 4-digit local code.
 * The full code is {@code module + category + localCode}.
 */
public interface StatusCode {

    String module();

    StatusCategory category();

    String localCode();

    I18nObject message();

    I18nObject advice();

    int httpStatusCode();

    String name();

    default String bisCode() {
        StatusCodeRules.requireValid(module(), category(), localCode());
        return module() + category().code() + localCode();
    }

    default String fullCode() {
        return bisCode();
    }

    default String summary() {
        String message = message() == null ? null : message().defaultValue();
        String advice = advice() == null ? null : advice().defaultValue();
        if (advice == null || advice.isBlank()) {
            return message;
        }
        return message + ", " + advice;
    }
}
