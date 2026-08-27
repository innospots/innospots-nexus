package com.innospots.nexus.console.extension.discovery;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.core.extension.contract.ConsoleExtensionProvider;

/**
 * Discovers console extension providers through the Java service provider
 * mechanism and validates the resulting provider collection.
 */
public final class ExtensionProviderDiscovery {

    private static final String SPI_SOURCE =
            "META-INF/services/"
                    + "com.innospots.nexus.core.extension.contract.ConsoleExtensionProvider";

    private ExtensionProviderDiscovery() {
    }

    /**
     * Discovers providers declared in the service configuration visible to the
     * supplied class loader.
     *
     * @param classLoader class loader used for SPI discovery; the thread
     *                    context class loader is used when absent
     * @return deduplicated providers in discovery order
     */
    public static List<ConsoleExtensionProvider> discover(ClassLoader classLoader) {
        Map<Class<?>, ConsoleExtensionProvider> byType = new LinkedHashMap<>();
        ClassLoader loader = classLoader == null
                ? Thread.currentThread().getContextClassLoader()
                : classLoader;
        if (loader != null) {
            try {
                for (ConsoleExtensionProvider provider
                        : ServiceLoader.load(ConsoleExtensionProvider.class, loader)) {
                    byType.putIfAbsent(provider.getClass(), provider);
                }
            } catch (ServiceConfigurationError error) {
                throw NexusException.build(
                        NexusStatusCode.CONFIG_ERROR.fullCode(),
                        "Invalid console extension SPI configuration: " + SPI_SOURCE,
                        error);
            }
        }
        validateExtensionKeys(byType.values());
        return List.copyOf(byType.values());
    }

    private static void validateExtensionKeys(Collection<ConsoleExtensionProvider> providers) {
        Map<String, Class<?>> keys = new LinkedHashMap<>();
        for (ConsoleExtensionProvider provider : providers) {
            if (provider.descriptor() == null) {
                throw NexusException.build(
                        NexusStatusCode.CONFIG_ERROR.fullCode(),
                        "Extension provider returned no descriptor");
            }
            Class<?> previous = keys.putIfAbsent(
                    provider.descriptor().extensionKey(), provider.getClass());
            if (previous != null && previous != provider.getClass()) {
                throw NexusException.build(
                        NexusStatusCode.CONFIG_ERROR.fullCode(),
                        "Duplicate extension key: " + provider.descriptor().extensionKey());
            }
        }
    }
}
