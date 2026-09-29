package com.innospots.nexus.console.sitemap.endpoint;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.console.sitemap.config.SitemapYamlConfig;
import com.innospots.nexus.console.sitemap.domain.vo.SitemapResource;
import com.innospots.nexus.console.sitemap.loader.SitemapConfigLoader;
import com.innospots.nexus.console.sitemap.parser.SitemapYamlParser;
import com.innospots.nexus.console.sitemap.service.SitemapMapper;
import com.innospots.nexus.console.sitemap.service.SitemapService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link NexusSitemapEndpoint} 对 {@code ui-pages/nexus/sitemap.yaml} 的渲染验证。
 */
class NexusSitemapEndpointTest {

    private NexusSitemapEndpoint endpoint;

    @BeforeEach
    void setUp() {
        SitemapService service = new SitemapService(
                new SitemapConfigLoader(
                        SitemapYamlConfig.defaults(),
                        new SitemapYamlParser(),
                        getClass().getClassLoader()),
                new SitemapMapper());
        endpoint = new NexusSitemapEndpoint(service);
    }

    @Test
    void exposesPublicNexusSitemapPath() throws NoSuchMethodException {
        assertThat(NexusSitemapEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(ConsoleConstant.PUBLIC_API_PREFIX + "/sitemap/nexus");
        assertThat(NexusSitemapEndpoint.class.getDeclaredMethod("renderHttp").getAnnotation(GET.class))
                .isNotNull();
    }

    @Test
    void renderLoadsNexusSitemapYaml() {
        SitemapResource resource = endpoint.render();

        assertThat(resource.getId()).isEqualTo("site");
        assertThat(resource.getResourceType()).isEqualTo("sitemap");
        assertThat(resource.getDslVersion()).isEqualTo("1.0");
        assertThat(resource.getLayout()).isEqualTo("admin");
        assertThat(resource.getAuth().getLoginPath()).isEqualTo("/login");
        assertThat(resource.getPages()).hasSize(6);
        assertThat(resource.getMenus()).hasSize(6);
        assertThat(resource.getLayouts()).containsKeys("admin", "login", "blank");
        assertThat(resource.getLayouts().get("admin").getBody().getType()).isEqualTo("Layout");
        assertThat(resource.getLayouts().get("login").getBody().getType()).isEqualTo("Flex");

        List<String> pageIds = resource.getPages().stream()
                .map(page -> page.getId())
                .toList();
        assertThat(pageIds).containsExactly(
                "nexus-menu-main",
                "nexus-dictionary-main",
                "nexus-role-main",
                "nexus-permission-main",
                "nexus-logger-main",
                "nexus-plugin-main");

        assertThat(resource.getPages().getFirst().getPath())
                .isEqualTo("/page/nexus/menu/nexus-menu-main");
        assertThat(resource.getMenus().getFirst().getPageId()).isEqualTo("nexus-menu-main");
    }

    @Test
    void renderHttpWrapsResourceInSuccessResponse() {
        R<SitemapResource> response = endpoint.renderHttp();

        assertThat(response.success()).isTrue();
        assertThat(response.code()).isEqualTo(R.OK);
        assertThat(response.data().getId()).isEqualTo("site");
        assertThat(response.data().getPages()).hasSize(6);
        assertThat(response.data().getMenus().getFirst().getId()).isEqualTo("nexus-menu-main");
    }
}
