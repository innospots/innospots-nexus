package com.innospots.nexus.core.plugin.capability;

import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * One immutable routing attribute.
 *
 * @param name tag name
 * @param value tag value
 */
public record Tag(String name, String value) {

    private static final Pattern PART_PATTERN = Pattern.compile("[a-z][a-z0-9]*(?:-[a-z0-9]+)*");

    /** Validates the lowercase kebab-case name and value. */
    public Tag {
        if (name == null || value == null
                || !PART_PATTERN.matcher(name).matches()
                || !PART_PATTERN.matcher(value).matches()) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "tag name and value must use lowercase kebab-case");
        }
    }
}
