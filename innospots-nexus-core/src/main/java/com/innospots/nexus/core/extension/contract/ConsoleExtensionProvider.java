package com.innospots.nexus.core.extension.contract;

import java.util.Collection;

import com.innospots.nexus.core.extension.declaration.ExtensionDescriptor;

/**
 * Runtime-neutral contribution contract for an installable console extension.
 * Implementations provide the descriptor and standard REST endpoint types that
 * an application adapter should register.
 */
public interface ConsoleExtensionProvider {

    /** Returns the immutable extension descriptor. */
    ExtensionDescriptor descriptor();

    /** Returns endpoint types contributed by this extension. */
    Collection<Class<?>> endpointTypes();
}
