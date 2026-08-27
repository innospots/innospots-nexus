package com.innospots.nexus.console.extension.discovery;

import java.util.Collection;
import java.util.List;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.extension.contract.ConsoleExtensionProvider;
import com.innospots.nexus.core.extension.declaration.ExtensionDescriptor;
import com.innospots.nexus.core.extension.declaration.ExtensionModuleDeclaration;
import com.innospots.nexus.core.extension.declaration.MenuDeclaration;
import com.innospots.nexus.core.extension.declaration.UiSpecPageDeclaration;

/** Test provider declared through the Java service provider configuration. */
public final class SpiConsoleExtensionProvider implements ConsoleExtensionProvider {

    /** Creates the test SPI provider. */
    public SpiConsoleExtensionProvider() {
    }

    @Override
    public ExtensionDescriptor descriptor() {
        return new ExtensionDescriptor(
                "com.example.spi",
                "1.0.0",
                I18nObject.of("en", "SPI"),
                I18nObject.of("en", "SPI extension"),
                List.of(new ExtensionModuleDeclaration(
                        "spi",
                        I18nObject.of("en", "SPI"),
                        I18nObject.of("en", "SPI module"),
                        List.of(new UiSpecPageDeclaration("home", "/spi", List.of())),
                        List.of(MenuDeclaration.page(
                                "home",
                                I18nObject.of("en", "Home"),
                                null,
                                0,
                                "home")))));
    }

    @Override
    public Collection<Class<?>> endpointTypes() {
        return List.of();
    }
}
