/**
 * Render-time transformation pipeline for page DSL documents.
 *
 * <p>Filters run after a document is loaded and before it is returned to the caller. Typical
 * uses include binding request parameters into {@code state}, permission trimming, and other
 * host-specific enrichment.</p>
 */
package com.innospots.nexus.base.ui.spec.filter;
