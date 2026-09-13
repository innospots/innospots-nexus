package com.innospots.nexus.service.runtime.context;

import com.innospots.nexus.service.contract.context.ServiceContext;

/**
 * Captured previous context used to restore a thread after an install.
 *
 * @param previous previous context, {@code null} when the thread had none
 * @author Smars
 * @date 2026/09/13
 * @see ThreadBoundServiceContext
 */
public record ContextSnapshot(ServiceContext previous) {
}
