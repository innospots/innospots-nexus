package com.innospots.nexus.console.ui.endpoint;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.ui.domain.request.PageDslRenderRequest;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DefaultPageDslEndpoint} 的 JAX-RS / OpenAPI 契约测试。
 */
class DefaultPageDslEndpointTest {

    @Test
    void exposesPlannedRenderOperation() throws NoSuchMethodException {
        assertThat(DefaultPageDslEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo("/api/public/ui/pages/{domainKey}/{moduleKey}/{pageKey}");
        assertThat(DefaultPageDslEndpoint.class.getDeclaredMethod(
                        "render",
                        PageDslRenderRequest.class,
                        UriInfo.class)
                .getAnnotation(GET.class)).isNotNull();
        assertThat(DefaultPageDslEndpoint.class.getDeclaredMethod(
                        "render",
                        PageDslRenderRequest.class,
                        UriInfo.class)
                .getParameters()[0].getAnnotation(BeanParam.class)).isNotNull();
    }

    @Test
    void httpRenderReturnsCommonResponseWrapper() throws NoSuchMethodException {
        assertThat(DefaultPageDslEndpoint.class.getDeclaredMethod(
                        "render",
                        PageDslRenderRequest.class,
                        UriInfo.class)
                .getReturnType()).isEqualTo(R.class);
    }
}
