package com.innospots.nexus.console.extension.domain.model;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.core.extension.contract.ConsoleExtensionProvider;
import com.innospots.nexus.core.extension.declaration.ExtensionDescriptor;
import com.innospots.nexus.console.extension.domain.enums.ExtensionState;

/** Immutable snapshot of an extension registration and lifecycle state. */
public record ExtensionRegistration(
        ConsoleExtensionProvider provider,
        boolean enabled,
        ExtensionState state
) {

    /** Creates a validated registration snapshot. */
    public ExtensionRegistration {
        if (provider == null || provider.descriptor() == null || state == null) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    "provider, descriptor and state are required");
        }
    }

    /** Returns the stable extension key. */
    public String extensionKey() {
        return provider.descriptor().extensionKey();
    }

    /** Returns the descriptor snapshot. */
    public ExtensionDescriptor descriptor() {
        return provider.descriptor();
    }
}
