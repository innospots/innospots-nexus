package com.innospots.nexus.core.openapi.catalog;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.core.openapi.OpenApiCatalogPaths;

/**
 * 构建期 OpenAPI 规范目录：列表与按 specId 读取 YAML。
 */
@Path(OpenApiCatalogPaths.SPECS_BASE)
@Tag(name = "OpenApiCatalog", description = "OpenAPI 规范目录")
public final class OpenApiCatalogEndpoint {

    private final OpenApiCatalogOperator catalogOperator;

    public OpenApiCatalogEndpoint(OpenApiCatalogOperator catalogOperator) {
        this.catalogOperator = catalogOperator;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "openApiSpecList", summary = "OpenAPI 规范列表")
    public R<List<OpenApiSpecItemVo>> listSpecs() {
        return R.ok(catalogOperator.listSpecs());
    }

    @GET
    @Path("/{specId}")
    @Produces({"application/yaml", "text/yaml"})
    @Operation(operationId = "openApiSpecDetail", summary = "OpenAPI 规范明细")
    public Response getSpec(@PathParam("specId") String specId) {
        return Response.ok(catalogOperator.readYaml(specId)).type("application/yaml").build();
    }
}
