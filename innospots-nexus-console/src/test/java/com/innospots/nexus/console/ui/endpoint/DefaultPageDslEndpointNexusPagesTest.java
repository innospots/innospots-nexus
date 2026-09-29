package com.innospots.nexus.console.ui.endpoint;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;
import com.innospots.nexus.console.ui.domain.request.PageDslRenderRequest;
import com.innospots.nexus.console.ui.spec.PageDsl;
import com.innospots.nexus.console.ui.spec.config.PageDslConfig;
import com.innospots.nexus.console.ui.spec.filter.PageDslFilterChain;
import com.innospots.nexus.console.ui.spec.loader.ClasspathPageDslLoader;
import com.innospots.nexus.console.ui.spec.parser.JacksonPageDslParser;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DefaultPageDslEndpoint} 对内置 {@code ui-pages/nexus} YAML 的加载与渲染验证。
 */
class DefaultPageDslEndpointNexusPagesTest {

    private static final String DOMAIN = ConsoleModuleDescriptor.BUILTIN_DOMAIN_KEY;

    private DefaultPageDslEndpoint endpoint;

    @BeforeEach
    void setUp() {
        PageDslConfig config = PageDslConfig.defaults();
        endpoint = new DefaultPageDslEndpoint(
                new ClasspathPageDslLoader(
                        config,
                        new JacksonPageDslParser(config),
                        getClass().getClassLoader()),
                PageDslFilterChain.create());
    }

    @ParameterizedTest
    @MethodSource("nexusBuiltinPages")
    void rendersNexusPageFromClasspath(String moduleKey, String pageKey) {
        PageDsl rendered = endpoint.render(DOMAIN, moduleKey, pageKey, Map.of());

        assertThat(rendered.getDsl()).isEqualTo(PageDsl.SPEC_VERSION);
        assertThat(rendered.getPage().getId()).isEqualTo(pageKey);
        assertThat(rendered.getPage().getType()).isEqualTo("general");
        assertThat(rendered.components()).isEmpty();
        assertThat(rendered.getBody()).isNull();
    }

    @ParameterizedTest
    @MethodSource("nexusBuiltinPages")
    void httpRenderResolvesCompositePageKey(String moduleKey, String pageKey) throws Exception {
        PageDslRenderRequest request = pageKeyRequest(pageKey);

        R<PageDsl> response = endpoint.render(request, null);

        assertThat(response.success()).isTrue();
        assertThat(response.data().getPage().getId()).isEqualTo(pageKey);
        assertThat(response.data().getPage().getType()).isEqualTo("general");
    }

    private static Stream<Arguments> nexusBuiltinPages() {
        return Stream.of(
                Arguments.of("menu", composite("menu")),
                Arguments.of("dictionary", composite("dictionary")),
                Arguments.of("logger", composite("logger")),
                Arguments.of("permission", composite("permission")),
                Arguments.of("plugin", composite("plugin")),
                Arguments.of("role", composite("role")));
    }

    private static String composite(String moduleKey) {
        return ConsoleModuleDescriptor.compositePageKey(DOMAIN, moduleKey, "main");
    }

    private static PageDslRenderRequest pageKeyRequest(String pageKey) throws Exception {
        PageDslRenderRequest request = new PageDslRenderRequest();
        Field field = PageDslRenderRequest.class.getDeclaredField("pageKey");
        field.setAccessible(true);
        field.set(request, pageKey);
        return request;
    }
}
