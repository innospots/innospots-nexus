package com.innospots.nexus.core.extension.declaration;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExtensionDeclarationTest {

    @Test
    void copiesModulePagesAndMenusIntoImmutableCollections() {
        UiSpecPageDeclaration page = new UiSpecPageDeclaration(
                "order-list",
                "/sales/orders",
                List.of());
        MenuDeclaration menu = MenuDeclaration.page(
                "order-list",
                I18nObject.of("en", "Orders"),
                "orders",
                10,
                "order-list");
        ExtensionModuleDeclaration module = new ExtensionModuleDeclaration(
                "sales",
                I18nObject.of("en", "Sales"),
                I18nObject.of("en", "Sales management"),
                List.of(page),
                List.of(menu));

        assertThat(module.pages()).containsExactly(page);
        assertThat(module.menuTree()).containsExactly(menu);
        assertThat(module.resourceKey()).isEqualTo("module:sales");
        assertThat(menu.resourceKey("sales")).isEqualTo("menu:sales.order-list");
        assertThatThrownBy(() -> module.pages().add(page))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> module.menuTree().add(menu))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsMenuNodeWithBothPageAndChildren() {
        assertThatThrownBy(() -> new MenuDeclaration(
                "invalid",
                I18nObject.of("en", "Invalid"),
                null,
                0,
                "order-list",
                List.of(MenuDeclaration.page(
                        "child",
                        I18nObject.of("en", "Child"),
                        null,
                        0,
                        "child"))))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void rejectsMenuNodeWithoutPageOrChildren() {
        assertThatThrownBy(() -> new MenuDeclaration(
                "invalid",
                I18nObject.of("en", "Invalid"),
                null,
                0,
                null,
                List.of()))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void rejectsInvalidPagePath() {
        assertThatThrownBy(() -> new UiSpecPageDeclaration(
                "order-list",
                "sales/orders",
                List.of()))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void rejectsMalformedOrRepeatedPagePathVariables() {
        assertThatThrownBy(() -> new UiSpecPageDeclaration(
                "order-detail",
                "/sales/orders/{orderId}/items/{orderId}",
                List.of()))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> new UiSpecPageDeclaration(
                "order-detail",
                "/sales/orders/order-{orderId}",
                List.of()))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void buildsQualifiedPageResourceKey() {
        UiSpecPageDeclaration page = new UiSpecPageDeclaration(
                "order-list",
                "/sales/orders",
                List.of());

        assertThat(page.resourceKey("sales")).isEqualTo("page:sales.order-list");
    }
}
