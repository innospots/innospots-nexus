package com.innospots.nexus.core.plugin.capability;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Immutable, deterministically ordered routing tags.
 */
public final class Tags {

    private static final Tags EMPTY = new Tags(Map.of());

    private final Map<String, String> values;

    private Tags(Map<String, String> values) {
        TreeMap<String, String> sorted = new TreeMap<>();
        values.forEach((name, value) -> {
            Tag tag = new Tag(name, value);
            sorted.put(tag.name(), tag.value());
        });
        // Map.copyOf does not promise iteration order; preserve the sorted order explicitly for stable diagnostics.
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(sorted));
    }

    /** Returns empty tags for an unconstrained lookup. */
    public static Tags empty() {
        return EMPTY;
    }

    /** Creates tags containing one attribute. */
    public static Tags of(String name, String value) {
        if (name == null || value == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "tag name and value must not be null");
        }
        return new Tags(Map.of(name, value));
    }

    /** Creates tags from an attribute map. */
    public static Tags from(Map<String, String> values) {
        if (values == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID, "tags must not be null");
        }
        return values.isEmpty() ? EMPTY : new Tags(values);
    }

    /** Returns a new tag set with one additional non-conflicting attribute. */
    public Tags and(String name, String value) {
        Tag tag = new Tag(name, value);
        String existing = values.get(tag.name());
        if (existing != null && !existing.equals(tag.value())) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "tag already has another value: " + name);
        }
        TreeMap<String, String> copy = new TreeMap<>(values);
        copy.put(tag.name(), tag.value());
        return new Tags(copy);
    }

    /** Finds a tag value. */
    public Optional<String> get(String name) {
        return Optional.ofNullable(values.get(name));
    }

    /** Returns whether this provider tag set contains every requested tag. */
    public boolean matches(Tags required) {
        if (required == null) {
            return false;
        }
        return values.entrySet().containsAll(required.values.entrySet());
    }

    /** Returns the immutable tag map. */
    public Map<String, String> asMap() {
        return values;
    }

    /** Returns whether no tags are present. */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Tags tags && values.equals(tags.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
