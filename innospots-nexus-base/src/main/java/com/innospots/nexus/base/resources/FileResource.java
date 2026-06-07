package com.innospots.nexus.base.resources;

import java.io.InputStream;

/**
 * A file resource with its content stream and metadata flags.
 * When {@code saveMeta} is true, the resource store persists metadata
 * alongside the binary content.
 */
public record FileResource(
        String name,
        String fileName,
        String contentType,
        InputStream inputStream,
        boolean saveMeta
) {
}
