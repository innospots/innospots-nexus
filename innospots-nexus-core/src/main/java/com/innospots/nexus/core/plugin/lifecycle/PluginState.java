package com.innospots.nexus.core.plugin.lifecycle;

/** Public coarse-grained lifecycle state of a managed plugin. */
public enum PluginState {
    DISCOVERED,
    DESCRIBED,
    WAITING,
    STARTING,
    ACTIVE,
    STOPPING,
    STOPPED,
    FAILED
}
