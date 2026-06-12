package com.innospots.nexus.console.api;

import com.innospots.nexus.console.endpoint.ConsoleEndpoint;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleEndpointTest {

    @Test
    void consoleEndpointUsesJakartaJaxRsContract() throws NoSuchMethodException {
        assertThat(ConsoleEndpoint.class.getAnnotation(Path.class).value()).isEqualTo("/console");
        assertThat(ConsoleEndpoint.class.getAnnotation(Produces.class).value()).containsExactly(MediaType.APPLICATION_JSON);
        assertThat(ConsoleEndpoint.class.getAnnotation(Consumes.class).value()).containsExactly(MediaType.APPLICATION_JSON);
        assertThat(ConsoleEndpoint.class.getMethod("status").getAnnotation(GET.class)).isNotNull();
        assertThat(ConsoleEndpoint.class.getMethod("status").getAnnotation(Path.class).value()).isEqualTo("/status");
    }
}
