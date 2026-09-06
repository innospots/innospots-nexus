package com.innospots.nexus.base.ui.spec.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.ui.spec.HttpRequest;
import com.innospots.nexus.base.ui.spec.PageDsl;
import com.innospots.nexus.base.ui.spec.action.ActionConfig;
import com.innospots.nexus.base.ui.spec.action.ActionOrList;
import com.innospots.nexus.base.ui.spec.datasource.DataSourceConfig;
import com.innospots.nexus.base.ui.spec.datasource.HttpDataSource;
import com.innospots.nexus.base.ui.spec.datasource.ServiceDataSource;
import com.innospots.nexus.base.ui.spec.datasource.StaticDataSource;
import com.innospots.nexus.base.ui.spec.node.ComponentNode;
import com.innospots.nexus.base.ui.spec.node.ComponentReferenceNode;
import com.innospots.nexus.base.ui.spec.node.DslRenderable;
import com.innospots.nexus.base.ui.spec.node.DslSourceRef;

/**
 * Validates structural and cross-reference rules in a page DSL document.
 *
 * <p>This validator implements the core schema boundary only. It does not prove that
 * components, actions, services, expressions, or permissions exist in the host runtime.</p>
 */
public final class PageDslValidator {

    private static final Set<String> HTTP_METHODS = Set.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");

    /**
     * Validates one complete page DSL document.
     *
     * @param document page DSL to validate
     * @throws com.innospots.nexus.base.exception.NexusException when a structural rule fails
     */
    public void validate(PageDsl document) {
        if (document == null) {
            invalid("PageDsl document is required");
        }
        if (!PageDsl.SPEC_VERSION.equals(document.getDsl())) {
            invalid("PageDsl dsl version must be " + PageDsl.SPEC_VERSION);
        }
        if (document.getPage() == null || !hasText(document.getPage().getId())) {
            invalid("PageDsl page.id is required");
        }
        validateDataSources(document.getDataSources());
        validateNamedActions(document.getActions(), document.getDataSources(), "actions");
        validateComponents(document.getComponents());
        validateRenderable(document.getBody(), document.getComponents(), "body");
        if (document.getChildren() != null) {
            validateChildren(document.getChildren(), document.getComponents(), "children");
        }
    }

    private void validateDataSources(Map<String, DataSourceConfig> dataSources) {
        for (Map.Entry<String, DataSourceConfig> entry : dataSources.entrySet()) {
            String key = entry.getKey();
            DataSourceConfig dataSource = entry.getValue();
            if (!hasText(key) || dataSource == null) {
                invalid("PageDsl dataSources key and definition are required");
            }
            switch (dataSource) {
                case StaticDataSource staticDataSource -> {
                    if (staticDataSource.getValue() == null) {
                        invalid("Static data source value is required: " + key);
                    }
                }
                case ServiceDataSource serviceDataSource -> {
                    if (!hasText(serviceDataSource.getService())) {
                        invalid("Service data source service is required: " + key);
                    }
                }
                case HttpDataSource httpDataSource -> {
                    validateHttpRequest(httpDataSource.getRequest(), "dataSources." + key);
                }
                default -> {
                }
            }
        }
    }

    private void validateNamedActions(
            Map<String, ActionOrList> actions,
            Map<String, DataSourceConfig> dataSources,
            String field
    ) {
        Set<String> actionIds = new HashSet<>();
        for (Map.Entry<String, ActionOrList> entry : actions.entrySet()) {
            if (!hasText(entry.getKey()) || entry.getValue() == null) {
                invalid("PageDsl " + field + " key and definition are required");
            }
            for (ActionConfig action : entry.getValue().actions()) {
                validateAction(action, dataSources, field + "." + entry.getKey(), actionIds);
            }
        }
    }

    private void validateAction(
            ActionConfig action,
            Map<String, DataSourceConfig> dataSources,
            String owner,
            Set<String> actionIds
    ) {
        if (action == null || !hasText(action.getAction())) {
            invalid("PageDsl action registry name is required for " + owner);
        }
        if (hasText(action.getId()) && !actionIds.add(action.getId())) {
            invalid("Duplicate PageDsl action id: " + action.getId());
        }
        validateActionParams(action.getParams(), dataSources, owner);
        if (action.getParams() != null && action.getParams().containsKey("actions")) {
            Object nested = action.getParams().get("actions");
            if (nested instanceof List<?> nestedActions) {
                for (Object item : nestedActions) {
                    if (item instanceof Map<?, ?> map) {
                        ActionConfig nestedAction = mapToAction(map);
                        validateAction(nestedAction, dataSources, owner, actionIds);
                    }
                }
            }
        }
        if (action.getParams() != null) {
            validateBranchActions(action.getParams().get("then"), dataSources, owner, actionIds);
            validateBranchActions(action.getParams().get("else"), dataSources, owner, actionIds);
        }
    }

    private void validateBranchActions(
            Object branch,
            Map<String, DataSourceConfig> dataSources,
            String owner,
            Set<String> actionIds
    ) {
        if (branch instanceof List<?> actions) {
            for (Object item : actions) {
                if (item instanceof Map<?, ?> map) {
                    validateAction(mapToAction(map), dataSources, owner, actionIds);
                }
            }
        }
    }

    private void validateActionParams(
            Map<String, Object> params,
            Map<String, DataSourceConfig> dataSources,
            String owner
    ) {
        if (params == null) {
            return;
        }
        Object dataSource = params.get("dataSource");
        if (dataSource instanceof String key && !dataSources.containsKey(key)) {
            invalid("Unknown data source referenced by action " + owner + ": " + key);
        }
        if ("request".equals(params.get("action")) || params.containsKey("url")) {
            validateInlineRequest(params, owner);
        }
    }

    private void validateInlineRequest(Map<String, Object> params, String owner) {
        Object method = params.get("method");
        Object url = params.get("url");
        if (method != null && !HTTP_METHODS.contains(String.valueOf(method).toUpperCase())) {
            invalid("Unsupported HTTP method for action " + owner);
        }
        if (url != null && !hasText(String.valueOf(url))) {
            invalid("Invalid request url for action " + owner);
        }
    }

    private void validateComponents(Map<String, DslRenderable> components) {
        for (Map.Entry<String, DslRenderable> entry : components.entrySet()) {
            if (!hasText(entry.getKey()) || entry.getValue() == null) {
                invalid("PageDsl components key and definition are required");
            }
            validateRenderable(entry.getValue(), components, "components." + entry.getKey());
        }
    }

    private void validateChildren(
            com.innospots.nexus.base.ui.spec.node.Children children,
            Map<String, DslRenderable> components,
            String owner
    ) {
        if (!children.isArray()) {
            validateRenderable(children.sourceRef(), components, owner);
            return;
        }
        for (DslRenderable child : children.items()) {
            validateRenderable(child, components, owner);
        }
    }

    private void validateRenderable(
            DslRenderable renderable,
            Map<String, DslRenderable> components,
            String owner
    ) {
        if (renderable == null) {
            return;
        }
        if (renderable instanceof ComponentNode componentNode) {
            if (!hasText(componentNode.getType())) {
                invalid("Component type is required for " + owner);
            }
            if (componentNode.getChildren() != null) {
                validateChildren(componentNode.getChildren(), components, owner + ".children");
            }
            return;
        }
        if (renderable instanceof ComponentReferenceNode referenceNode) {
            if (!hasText(referenceNode.getComponent())) {
                invalid("Component reference name is required for " + owner);
            }
            if (!components.containsKey(referenceNode.getComponent())) {
                invalid("Unknown component reference in " + owner + ": " + referenceNode.getComponent());
            }
            if (referenceNode.getChildren() != null) {
                validateChildren(referenceNode.getChildren(), components, owner + ".children");
            }
            return;
        }
        if (renderable instanceof DslSourceRef sourceRef) {
            if (sourceRef.getSource() == null) {
                invalid("Dynamic DSL source is required for " + owner);
            }
            if (sourceRef.getPlaceholder() != null) {
                validateRenderable(sourceRef.getPlaceholder(), components, owner + ".placeholder");
            }
        }
    }

    private void validateHttpRequest(HttpRequest request, String owner) {
        if (request == null) {
            invalid("HTTP request is required for " + owner);
        }
        if (!hasText(request.getUrl())) {
            invalid("HTTP request url is required for " + owner);
        }
        if (request.getMethod() != null
                && !HTTP_METHODS.contains(request.getMethod().toUpperCase())) {
            invalid("Unsupported HTTP method for " + owner);
        }
    }

    private ActionConfig mapToAction(Map<?, ?> map) {
        ActionConfig action = new ActionConfig();
        Object id = map.get("id");
        if (id != null) {
            action.setId(String.valueOf(id));
        }
        Object actionName = map.get("action");
        if (actionName != null) {
            action.setAction(String.valueOf(actionName));
        }
        Object params = map.get("params");
        if (params instanceof Map<?, ?> paramMap) {
            Map<String, Object> actual = new java.util.LinkedHashMap<>();
            paramMap.forEach((key, value) -> actual.put(String.valueOf(key), value));
            action.setParams(actual);
        }
        return action;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static void invalid(String message) {
        throw NexusException.build(NexusStatusCode.CONFIG_ERROR.fullCode(), message);
    }
}
