package com.innospots.nexus.base.ui.spec.permission;

/**
 * Permission denial behavior when access is not granted.
 */
public enum PermissionDenied {

    /** Hide the protected UI element. */
    HIDDEN,

    /** Show the element but prevent interaction. */
    DISABLED,

    /** Show the element in read-only mode. */
    READONLY
}
