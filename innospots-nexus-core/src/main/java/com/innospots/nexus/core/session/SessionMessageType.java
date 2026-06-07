package com.innospots.nexus.core.session;

/**
 * Message type classification for {@link SessionMessage}.
 * <p>{@link #fromMimeType(String)} provides heuristic mapping from
 * MIME types to message display types.</p>
 */
public enum SessionMessageType {
    TABLE,
    CHART,
    TEXT,
    MARKDOWN,
    SLICE,
    IMAGE,
    VIDEO,
    FILE,
    EMBED,
    JSON;

    /**
     * Maps a MIME type string to the closest message type.
     * Defaults to {@link #TEXT} when no match is found.
     */
    public static SessionMessageType fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return TEXT;
        }
        String value = mimeType.toLowerCase();
        if (value.contains("image")) {
            return IMAGE;
        }
        if (value.contains("video")) {
            return VIDEO;
        }
        if (value.contains("application") || value.contains("text")) {
            return FILE;
        }
        return TEXT;
    }
}
