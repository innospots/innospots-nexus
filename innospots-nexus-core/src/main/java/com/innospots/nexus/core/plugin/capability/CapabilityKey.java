package com.innospots.nexus.core.plugin.capability;

import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Stable logical identity of a capability API major version.
 *
 * @param name lowercase dotted capability name
 * @param majorVersion positive major API version
 */
public record CapabilityKey(String name, int majorVersion) {

    private static final Pattern NAME_PATTERN = Pattern.compile("[a-z][a-z0-9]*(?:[.-][a-z0-9]+)*");

    /** Validates the stable capability identity. */
    public CapabilityKey {
        if (name == null || !NAME_PATTERN.matcher(name).matches() || majorVersion < 1) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "invalid capability key: " + name + "@" + majorVersion);
        }
    }

    @Override
    public String toString() {
        return name + "@" + majorVersion;
    }
}
