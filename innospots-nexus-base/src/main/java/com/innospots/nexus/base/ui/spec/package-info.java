/**
 * Pactor Page DSL 1.0 core model and infrastructure.
 *
 * <p>This package defines the serializable page DSL contract consumed by management-console
 * renderers. It validates structural shape only; component props, action semantics, expression
 * syntax, and service bindings are validated by runtime registries and linters.</p>
 *
 * <p>Recommended resource layout for page YAML:</p>
 * <pre>
 * ui-pages/{moduleKey}/{pageId}.yaml
 * </pre>
 *
 * <p>JSON Schema authoritative source:
 * {@code skills/ui/ui-reference/references/pactor-page-dsl.schema.yaml}</p>
 *
 * @see com.innospots.nexus.base.ui.spec.PageDsl
 */
package com.innospots.nexus.base.ui.spec;
