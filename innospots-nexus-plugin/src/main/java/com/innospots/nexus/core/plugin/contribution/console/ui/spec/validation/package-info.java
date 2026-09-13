/**
 * Core structural validation for page DSL documents.
 *
 * <p>Validates schema shape, required fields, and in-document references such as component
 * names and data source keys. Does not validate component props, registered actions, services,
 * expressions, or real permission existence — those belong to runtime registries and linters.</p>
 */
package com.innospots.nexus.core.plugin.contribution.console.ui.spec.validation;
