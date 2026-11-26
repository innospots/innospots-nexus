package com.innospots.nexus.ai.runtime;

import java.net.URI;

/**
 * Node runtime 的本地端点。
 *
 * @param baseUri loopback base URI
 */
public record AiEndpoint(URI baseUri) {
}
