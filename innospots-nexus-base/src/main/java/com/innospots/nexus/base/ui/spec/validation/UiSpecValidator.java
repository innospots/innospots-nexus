package com.innospots.nexus.base.ui.spec.validation;

import java.util.Map;
import java.util.Set;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.ui.spec.ApiRequest;
import com.innospots.nexus.base.ui.spec.UiSpec;
import com.innospots.nexus.base.ui.spec.action.UiAction;
import com.innospots.nexus.base.ui.spec.component.UiComponentSpec;
import com.innospots.nexus.base.ui.spec.datasource.UiDatasource;

/** Validates cross-references and security-relevant fields in a page specification. */
public final class UiSpecValidator {

    private static final Set<String> HTTP_METHODS = Set.of(
            "GET", "POST", "PUT", "PATCH", "DELETE");

    /** Validates one complete page specification. */
    public void validate(UiSpec spec) {
        if (spec == null || spec.pageInfo() == null || !hasText(spec.pageInfo().pageId())) {
            invalid("UiSpec pageInfo.pageId is required");
        }
        validateDatasources(spec.datasources());
        validateActions(spec.actionDefinitions(), spec.datasources(), "actionDefinitions");
        validateActions(spec.aiActions(), spec.datasources(), "aiActions");
        validateComponents(spec.components(), spec.datasources());
    }

    private void validateDatasources(Map<String, UiDatasource> datasources) {
        for (Map.Entry<String, UiDatasource> entry : datasources.entrySet()) {
            String key = entry.getKey();
            UiDatasource datasource = entry.getValue();
            if (!hasText(key) || datasource == null) {
                invalid("UiSpec datasource key and definition are required");
            }
            if (!hasText(datasource.getMethod())
                    || !HTTP_METHODS.contains(datasource.getMethod().toUpperCase())) {
                invalid("Unsupported datasource method: " + key);
            }
            if (!hasText(datasource.getUrl())) {
                invalid("Datasource URL is required: " + key);
            }
        }
    }

    private void validateActions(
            Map<String, UiAction> actions,
            Map<String, UiDatasource> datasources,
            String field
    ) {
        for (Map.Entry<String, UiAction> entry : actions.entrySet()) {
            UiAction action = entry.getValue();
            if (!hasText(entry.getKey()) || action == null || !entry.getKey().equals(action.actionId())) {
                invalid("UiSpec " + field + " key must match actionId: " + entry.getKey());
            }
            if (hasText(action.datasourceKey()) && !datasources.containsKey(action.datasourceKey())) {
                invalid("Unknown datasource referenced by action " + entry.getKey()
                        + ": " + action.datasourceKey());
            }
            validateRequest(action.request(), "action " + entry.getKey());
            for (UiAction child : action.children()) {
                validateChildAction(child, datasources, entry.getKey());
            }
        }
    }

    private void validateChildAction(
            UiAction action,
            Map<String, UiDatasource> datasources,
            String parentKey
    ) {
        if (action == null || !hasText(action.actionId())) {
            invalid("Invalid child action: " + parentKey);
        }
        if (hasText(action.datasourceKey()) && !datasources.containsKey(action.datasourceKey())) {
            invalid("Unknown datasource referenced by action " + action.actionId()
                    + ": " + action.datasourceKey());
        }
        validateRequest(action.request(), "action " + action.actionId());
        for (UiAction child : action.children()) {
            validateChildAction(child, datasources, action.actionId());
        }
    }

    private void validateComponents(
            Map<String, UiComponentSpec> components,
            Map<String, UiDatasource> datasources
    ) {
        for (Map.Entry<String, UiComponentSpec> entry : components.entrySet()) {
            UiComponentSpec component = entry.getValue();
            if (!hasText(entry.getKey())
                    || component == null
                    || !entry.getKey().equals(component.componentId())) {
                invalid("UiSpec component key must match componentId: " + entry.getKey());
            }
            if (hasText(component.datasource()) && !datasources.containsKey(component.datasource())) {
                invalid("Unknown datasource referenced by component " + entry.getKey()
                        + ": " + component.datasource());
            }
        }
    }

    private void validateRequest(ApiRequest request, String owner) {
        if (request == null) {
            return;
        }
        if (!hasText(request.method())
                || !HTTP_METHODS.contains(request.method().toUpperCase())
                || !hasText(request.uri())) {
            invalid("Invalid API request for " + owner);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static void invalid(String message) {
        throw NexusException.build(NexusStatusCode.CONFIG_ERROR.fullCode(), message);
    }
}
