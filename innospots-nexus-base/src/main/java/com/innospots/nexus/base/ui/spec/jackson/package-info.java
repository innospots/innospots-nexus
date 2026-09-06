/**
 * Jackson adapters for Pactor DSL union types.
 *
 * <p>Several schema fields accept multiple YAML shapes (for example {@code actions} as one
 * object or an array, or {@code children} as an array or a dynamic source reference). These
 * deserializers implement that polymorphism without weakening the Java domain model.</p>
 */
package com.innospots.nexus.base.ui.spec.jackson;
