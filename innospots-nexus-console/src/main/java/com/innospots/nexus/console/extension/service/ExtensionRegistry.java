package com.innospots.nexus.console.extension.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.core.extension.contract.ConsoleExtensionProvider;
import com.innospots.nexus.core.extension.declaration.ExtensionDescriptor;
import com.innospots.nexus.core.extension.declaration.ExtensionModuleDeclaration;
import com.innospots.nexus.core.extension.declaration.MenuDeclaration;
import com.innospots.nexus.core.extension.declaration.UiSpecPageDeclaration;
import com.innospots.nexus.console.extension.domain.entity.ExtensionInstallationEntity;
import com.innospots.nexus.console.extension.domain.enums.ExtensionState;
import com.innospots.nexus.console.extension.domain.model.ExtensionRegistration;
import com.innospots.nexus.console.extension.repository.ExtensionInstallationRepository;

/** In-memory extension registration and activation boundary. */
public final class ExtensionRegistry {

    private final Map<String, Entry> entries = new LinkedHashMap<>();
    private final ExtensionInstallationRepository installationRepository;

    /** Creates an in-memory registry without persistence integration. */
    public ExtensionRegistry() {
        this(null);
    }

    /** Creates a registry that persists installation and lifecycle changes. */
    public ExtensionRegistry(ExtensionInstallationRepository installationRepository) {
        this.installationRepository = installationRepository;
    }

    /** Registers a discovered provider as enabled but not active. */
    public synchronized ExtensionRegistration register(ConsoleExtensionProvider provider) {
        requireProvider(provider);
        String extensionKey = provider.descriptor().extensionKey();
        if (entries.containsKey(extensionKey)) {
            fail("Duplicate extension key: " + extensionKey);
        }
        validateModuleKeys(provider.descriptor(), null);
        ExtensionInstallationEntity installation = installationRepository == null
                ? null
                : installationRepository.register(provider);
        boolean enabled = installation == null || installation.isEnabled();
        ExtensionState state = enabled ? ExtensionState.REGISTERED : ExtensionState.DISABLED;
        entries.put(extensionKey, new Entry(provider, enabled, state));
        return snapshot(entries.get(extensionKey));
    }

    /** Activates an enabled extension after validating its complete resource tree. */
    public synchronized ExtensionRegistration activate(String extensionKey) {
        Entry entry = requireEntry(extensionKey);
        if (!entry.enabled) {
            fail("Extension is disabled: " + extensionKey);
        }
        try {
            validateDescriptor(entry.provider.descriptor(), extensionKey);
            entry.state = ExtensionState.ACTIVE;
            persistState(entry, null);
        } catch (RuntimeException exception) {
            entry.state = ExtensionState.FAILED;
            persistState(entry, exception.getMessage());
            throw exception;
        }
        return snapshot(entry);
    }

    /** Disables an extension and removes it from the active descriptor view. */
    public synchronized ExtensionRegistration disable(String extensionKey) {
        Entry entry = requireEntry(extensionKey);
        entry.enabled = false;
        entry.state = ExtensionState.DISABLED;
        persistState(entry, null);
        return snapshot(entry);
    }

    /** Re-enables a disabled extension without activating it automatically. */
    public synchronized ExtensionRegistration enable(String extensionKey) {
        Entry entry = requireEntry(extensionKey);
        entry.enabled = true;
        entry.state = ExtensionState.REGISTERED;
        persistState(entry, null);
        return snapshot(entry);
    }

    /** Returns the current state for a registered extension. */
    public synchronized ExtensionState status(String extensionKey) {
        return requireEntry(extensionKey).state;
    }

    /** Returns an immutable snapshot of the requested registration. */
    public synchronized ExtensionRegistration registration(String extensionKey) {
        return snapshot(requireEntry(extensionKey));
    }

    /** Returns descriptors of active extensions in registration order. */
    public synchronized List<ExtensionDescriptor> activeDescriptors() {
        List<ExtensionDescriptor> descriptors = new ArrayList<>();
        for (Entry entry : entries.values()) {
            if (entry.state == ExtensionState.ACTIVE) {
                descriptors.add(entry.provider.descriptor());
            }
        }
        return List.copyOf(descriptors);
    }

    /** Returns active providers so an application adapter can register endpoints. */
    public synchronized List<ConsoleExtensionProvider> activeProviders() {
        List<ConsoleExtensionProvider> providers = new ArrayList<>();
        for (Entry entry : entries.values()) {
            if (entry.state == ExtensionState.ACTIVE) {
                providers.add(entry.provider);
            }
        }
        return List.copyOf(providers);
    }

    private void validateDescriptor(ExtensionDescriptor descriptor, String extensionKey) {
        validateModuleKeys(descriptor, extensionKey);
        for (ExtensionModuleDeclaration module : descriptor.modules()) {
            Map<String, UiSpecPageDeclaration> pages = new HashMap<>();
            Map<String, UiSpecPageDeclaration> paths = new HashMap<>();
            collectPages(module, module.pages(), pages, paths, new HashSet<>());
            validateMenus(module, module.menuTree(), pages);
        }
    }

    private void validateModuleKeys(ExtensionDescriptor descriptor, String currentExtensionKey) {
        Set<String> moduleKeys = new HashSet<>();
        for (ExtensionModuleDeclaration module : descriptor.modules()) {
            if (!moduleKeys.add(module.moduleKey())) {
                fail("Duplicate module key: " + module.moduleKey());
            }
            for (Entry entry : entries.values()) {
                if (currentExtensionKey != null
                        && entry.provider.descriptor().extensionKey().equals(currentExtensionKey)) {
                    continue;
                }
                for (ExtensionModuleDeclaration existing : entry.provider.descriptor().modules()) {
                    if (existing.moduleKey().equals(module.moduleKey())) {
                        fail("Duplicate module key: " + module.moduleKey());
                    }
                }
            }
        }
    }

    private void collectPages(
            ExtensionModuleDeclaration module,
            List<UiSpecPageDeclaration> declarations,
            Map<String, UiSpecPageDeclaration> pages,
            Map<String, UiSpecPageDeclaration> paths,
            Set<String> ancestors
    ) {
        for (UiSpecPageDeclaration page : declarations) {
            if (!ancestors.add(page.pageKey())) {
                fail("Page tree cycle: " + module.moduleKey() + "." + page.pageKey());
            }
            if (pages.putIfAbsent(page.pageKey(), page) != null) {
                fail("Duplicate page key: " + module.moduleKey() + "." + page.pageKey());
            }
            String pathIdentity = pathIdentity(page.pagePath());
            if (paths.putIfAbsent(pathIdentity, page) != null) {
                fail("Conflicting page path: " + module.moduleKey() + " " + page.pagePath());
            }
            collectPages(module, page.children(), pages, paths, ancestors);
            ancestors.remove(page.pageKey());
        }
    }

    private static String pathIdentity(String path) {
        StringBuilder identity = new StringBuilder();
        for (String segment : path.substring(1).split("/", -1)) {
            if (segment.startsWith("{") && segment.endsWith("}")) {
                identity.append("{}");
            } else {
                identity.append(segment);
            }
            identity.append('/');
        }
        return identity.toString();
    }

    private void validateMenus(
            ExtensionModuleDeclaration module,
            List<MenuDeclaration> menus,
            Map<String, UiSpecPageDeclaration> pages
    ) {
        Set<String> menuKeys = new HashSet<>();
        validateMenus(module, menus, pages, menuKeys);
    }

    private void validateMenus(
            ExtensionModuleDeclaration module,
            List<MenuDeclaration> menus,
            Map<String, UiSpecPageDeclaration> pages,
            Set<String> menuKeys
    ) {
        for (MenuDeclaration menu : menus) {
            if (!menuKeys.add(menu.menuKey())) {
                fail("Duplicate menu key: " + module.moduleKey() + "." + menu.menuKey());
            }
            if (menu.pageKey() != null && !pages.containsKey(menu.pageKey())) {
                fail("Unknown page referenced by menu: " + module.moduleKey() + "."
                        + menu.pageKey());
            }
            validateMenus(module, menu.children(), pages, menuKeys);
        }
    }

    private Entry requireEntry(String extensionKey) {
        Entry entry = entries.get(extensionKey);
        if (entry == null) {
            fail("Unknown extension: " + extensionKey);
        }
        return entry;
    }

    private ExtensionRegistration snapshot(Entry entry) {
        return new ExtensionRegistration(entry.provider, entry.enabled, entry.state);
    }

    private void persistState(Entry entry, String errorMessage) {
        if (installationRepository != null) {
            installationRepository.updateState(
                    entry.provider.descriptor().extensionKey(),
                    entry.state,
                    entry.enabled,
                    errorMessage);
        }
    }

    private static void requireProvider(ConsoleExtensionProvider provider) {
        if (provider == null || provider.descriptor() == null) {
            fail("Extension provider and descriptor are required");
        }
    }

    private static void fail(String message) {
        throw NexusException.build(NexusStatusCode.CONFIG_ERROR.fullCode(), message);
    }

    private static final class Entry {

        private final ConsoleExtensionProvider provider;
        private boolean enabled;
        private ExtensionState state;

        private Entry(ConsoleExtensionProvider provider, boolean enabled, ExtensionState state) {
            this.provider = provider;
            this.enabled = enabled;
            this.state = state;
        }
    }
}
