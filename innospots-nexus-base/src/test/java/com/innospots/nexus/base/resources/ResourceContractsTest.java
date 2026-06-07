package com.innospots.nexus.base.resources;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceContractsTest {

    @Test
    void definesMetaResourceAndResourceEventContracts() {
        MetaResource meta = new MetaResource(
                "res-1",
                "report.txt",
                "text/plain",
                "workflow",
                "flow-1",
                5,
                "/tmp/report.txt",
                "local",
                Instant.now()
        );
        ResourceEvent event = new ResourceEvent(meta);

        assertThat(event.eventType()).isEqualTo("resource.meta.saved");
        assertThat(event.metaResource()).isEqualTo(meta);
    }

    @Test
    void definesFileResourceShape() {
        FileResource resource = new FileResource(
                "report",
                "report.txt",
                "text/plain",
                new ByteArrayInputStream(new byte[]{1, 2, 3}),
                true
        );

        assertThat(resource.fileName()).isEqualTo("report.txt");
        assertThat(resource.contentType()).isEqualTo("text/plain");
        assertThat(resource.saveMeta()).isTrue();
    }
}
