package com.innospots.nexus.quarkus.console.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogEndpoint;
import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogOperator;

@ApplicationScoped
public class ConsoleOpenApiBeans {

    @Produces
    @Singleton
    OpenApiCatalogOperator openApiCatalogOperator() {
        return new OpenApiCatalogOperator();
    }

    @Produces
    @Singleton
    OpenApiCatalogEndpoint openApiCatalogEndpoint(OpenApiCatalogOperator openApiCatalogOperator) {
        return new OpenApiCatalogEndpoint(openApiCatalogOperator);
    }
}
