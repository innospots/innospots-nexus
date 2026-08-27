package com.innospots.nexus.console.extension.service;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.extension.contract.ConsoleExtensionProvider;
import com.innospots.nexus.core.extension.declaration.ExtensionDescriptor;
import com.innospots.nexus.core.extension.declaration.ExtensionModuleDeclaration;
import com.innospots.nexus.core.extension.declaration.MenuDeclaration;
import com.innospots.nexus.core.extension.declaration.UiSpecPageDeclaration;
import com.innospots.nexus.console.extension.discovery.ExtensionProviderDiscovery;
import com.innospots.nexus.console.extension.domain.enums.ExtensionState;
import com.innospots.nexus.console.extension.domain.model.ExtensionRegistration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExtensionRegistryTest {

    @Test
    void registersExtensionAsEnabledButNotActive() {
        ExtensionRegistry registry = new ExtensionRegistry();

        ExtensionRegistration registration = registry.register(new SalesExtension());

        assertThat(registration.enabled()).isTrue();
        assertThat(registration.state()).isEqualTo(ExtensionState.REGISTERED);
        assertThat(registry.status("com.example.sales"))
                .isEqualTo(ExtensionState.REGISTERED);
    }

    @Test
    void activatesAndDisablesExtensionAtomically() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(new SalesExtension());

        registry.activate("com.example.sales");
        assertThat(registry.status("com.example.sales")).isEqualTo(ExtensionState.ACTIVE);
        assertThat(registry.activeDescriptors()).hasSize(1);
        assertThat(registry.activeProviders()).hasSize(1);

        registry.disable("com.example.sales");
        assertThat(registry.status("com.example.sales")).isEqualTo(ExtensionState.DISABLED);
        assertThat(registry.activeDescriptors()).isEmpty();
        assertThat(registry.activeProviders()).isEmpty();
    }

    @Test
    void rejectsDuplicateExtensionKey() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(new SalesExtension());

        assertThatThrownBy(() -> registry.register(new SalesExtension()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void rejectsMenuReferenceToUnknownPageWhenActivating() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(new InvalidMenuExtension());

        assertThatThrownBy(() -> registry.activate("com.example.invalid-menu"))
                .isInstanceOf(RuntimeException.class);
        assertThat(registry.status("com.example.invalid-menu"))
                .isEqualTo(ExtensionState.FAILED);
    }

    @Test
    void rejectsConflictingPagePathTemplatesWithinModule() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(new ConflictingPathExtension());

        assertThatThrownBy(() -> registry.activate("com.example.conflicting-path"))
                .isInstanceOf(RuntimeException.class);
        assertThat(registry.status("com.example.conflicting-path"))
                .isEqualTo(ExtensionState.FAILED);
    }

    @Test
    void discoversSpiProvidersWithoutDuplicateTypes() {
        List<ConsoleExtensionProvider> providers = ExtensionProviderDiscovery.discover(
                getClass().getClassLoader());

        assertThat(providers).extracting(provider -> provider.descriptor().extensionKey())
                .containsExactly("com.example.spi");
    }

    private static ExtensionDescriptor createDescriptor(String extensionKey, String moduleKey) {
        return new ExtensionDescriptor(
                extensionKey,
                "1.0.0",
                I18nObject.of("en", moduleKey),
                I18nObject.of("en", moduleKey + " extension"),
                List.of(new ExtensionModuleDeclaration(
                        moduleKey,
                        I18nObject.of("en", moduleKey),
                        I18nObject.of("en", moduleKey + " module"),
                        List.of(new UiSpecPageDeclaration(
                                "home",
                                "/" + moduleKey,
                                List.of())),
                        List.of(MenuDeclaration.page(
                                "home",
                                I18nObject.of("en", "Home"),
                                null,
                                0,
                                "home")))));
    }

    public static final class SalesExtension implements ConsoleExtensionProvider {

        @Override
        public ExtensionDescriptor descriptor() {
            return createDescriptor("com.example.sales", "sales");
        }

        @Override
        public Collection<Class<?>> endpointTypes() {
            return List.of();
        }
    }

    private static final class InvalidMenuExtension implements ConsoleExtensionProvider {

        @Override
        public ExtensionDescriptor descriptor() {
            return new ExtensionDescriptor(
                    "com.example.invalid-menu",
                    "1.0.0",
                    I18nObject.of("en", "Invalid"),
                    I18nObject.of("en", "Invalid extension"),
                    List.of(new ExtensionModuleDeclaration(
                            "invalid",
                            I18nObject.of("en", "Invalid"),
                            I18nObject.of("en", "Invalid module"),
                            List.of(new UiSpecPageDeclaration("home", "/invalid", List.of())),
                            List.of(MenuDeclaration.page(
                                    "missing",
                                    I18nObject.of("en", "Missing"),
                                    null,
                                    0,
                                    "missing")))));
        }

        @Override
        public Collection<Class<?>> endpointTypes() {
            return List.of();
        }
    }

    private static final class ConflictingPathExtension implements ConsoleExtensionProvider {

        @Override
        public ExtensionDescriptor descriptor() {
            return new ExtensionDescriptor(
                    "com.example.conflicting-path",
                    "1.0.0",
                    I18nObject.of("en", "Conflicting"),
                    I18nObject.of("en", "Conflicting extension"),
                    List.of(new ExtensionModuleDeclaration(
                            "sales",
                            I18nObject.of("en", "Sales"),
                            I18nObject.of("en", "Sales module"),
                            List.of(
                                    new UiSpecPageDeclaration("first", "/orders/{id}", List.of()),
                                    new UiSpecPageDeclaration("second", "/orders/{orderId}", List.of())),
                            List.of())));
        }

        @Override
        public Collection<Class<?>> endpointTypes() {
            return List.of();
        }
    }
}
