package com.innospots.nexus.console.extension.domain.enums;

/** Lifecycle states tracked for an installed extension. */
public enum ExtensionState {
    DISCOVERED,
    REGISTERED,
    DISABLED,
    ACTIVE,
    FAILED,
    MISSING
}
