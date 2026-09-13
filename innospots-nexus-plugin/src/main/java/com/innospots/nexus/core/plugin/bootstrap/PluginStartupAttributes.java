package com.innospots.nexus.core.plugin.bootstrap;

import com.innospots.nexus.core.bootstrap.NexusStartupContext;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;

/**
 * Startup attribute keys and helpers for the plugin subsystem.
 */
public final class PluginStartupAttributes {

    public static final String INSTALLATION_MANAGER = "plugin.installationManager";

    private PluginStartupAttributes() {
    }

    /**
     * Returns the installation manager attached by {@link PluginHostStartupTask}.
     *
     * @param context startup context
     * @return installation manager when plugin host startup completed
     */
    public static PluginInstallationManager installationManager(NexusStartupContext context) {
        return context.getAttribute(INSTALLATION_MANAGER, PluginInstallationManager.class).orElse(null);
    }
}
