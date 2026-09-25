package com.innospots.nexus.sample.spring.platform.jaxrs.web.support;

import java.lang.annotation.Inherited;

import org.springframework.test.context.TestPropertySource;

@Inherited
@TestPropertySource(properties = {
        "spring.jersey.type=filter",
        "spring.datasource.url=jdbc:h2:mem:jaxrs-web-it;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=never",
        "nexus.console.web.enabled=true"
})
public @interface ConsoleJaxRsWebIntegrationTestProperties {
}
