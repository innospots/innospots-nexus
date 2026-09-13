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
 * <p>Structural authority: {@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl}
 * and {@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.validation.PageDslValidator}.</p>
 *
 * @see com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl
 */
package com.innospots.nexus.core.plugin.contribution.console.ui.spec;
