package com.innospots.nexus.sample.spring.platform.jaxrs.web.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.TestPropertySource;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@TestPropertySource(properties = {
        "spring.jersey.type=servlet",
        "spring.datasource.url=jdbc:h2:mem:jaxrs-web-it;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=never",
        "nexus.console.web.enabled=true",
        "nexus.console.web.security.enabled=true",
        "nexus.console.web.security.dev-session.enabled=false"
})
public @interface ConsoleJaxRsWebIntegrationTestProperties {
}
