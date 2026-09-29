package com.innospots.nexus.console.ui.spec;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageDslPageRefTest {

    @Test
    void encodesAndDecodesBuiltinPageKey() {
        String pageKey = PageDslPageRef.encode("nexus", "menu", "main");

        assertThat(pageKey).isEqualTo("nexus-menu-main");
        PageDslPageRef ref = PageDslPageRef.decode(pageKey);
        assertThat(ref.pageKey()).isEqualTo("nexus-menu-main");
        assertThat(ref.domainKey()).isEqualTo("nexus");
        assertThat(ref.moduleKey()).isEqualTo("menu");
        assertThat(ref.pageSuffix()).isEqualTo("main");
    }

    @Test
    void decodesFixtureSalesPageKey() {
        PageDslPageRef ref = PageDslPageRef.decode("sales-sales-order-list");

        assertThat(ref.pageKey()).isEqualTo("sales-sales-order-list");
        assertThat(ref.domainKey()).isEqualTo("sales");
        assertThat(ref.moduleKey()).isEqualTo("sales");
        assertThat(ref.pageSuffix()).isEqualTo("order-list");
    }
}
