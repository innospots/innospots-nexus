package com.innospots.nexus.quarkus.service.test.fixture;

import com.innospots.nexus.quarkus.service.config.ServiceRuntimeHolder;
import com.innospots.nexus.service.adapter.test.scenario.AdapterScenarioPaths;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;

import org.jboss.resteasy.reactive.RestStreamElementType;

import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * adapter-test 流式夹具端点。
 */
@ApplicationScoped
@Path("")
public class SampleStreamResource {

    private final ServiceRuntimeHolder serviceRuntimeHolder;

    public SampleStreamResource(ServiceRuntimeHolder serviceRuntimeHolder) {
        this.serviceRuntimeHolder = serviceRuntimeHolder;
    }

    @GET
    @Path(AdapterScenarioPaths.STREAM_SSE)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Multi<String> streamSse() {
        return Multi.createFrom().items(
                "event: message\ndata: hello\n\n",
                "event: complete\ndata: done\n\n");
    }

    @GET
    @Path(AdapterScenarioPaths.STREAM_CANCEL)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Multi<String> streamCancel() {
        CancellationToken cancellation = serviceRuntimeHolder.contexts().requireCurrent().cancellation();
        return Multi.createFrom().emitter(emitter -> {
            Thread worker = new Thread(() -> {
                try {
                    for (int index = 0; index < 100; index++) {
                        if (cancellation.isCancelled()) {
                            break;
                        }
                        emitter.emit("event: message\ndata: chunk-" + index + "\n\n");
                        Thread.sleep(200L);
                    }
                    emitter.complete();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    emitter.fail(ex);
                }
            }, "adapter-stream-cancel");
            worker.setDaemon(true);
            worker.start();
        });
    }
}
