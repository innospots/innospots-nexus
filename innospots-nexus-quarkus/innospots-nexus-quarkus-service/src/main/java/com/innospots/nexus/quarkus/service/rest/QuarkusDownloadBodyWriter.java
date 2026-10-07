package com.innospots.nexus.quarkus.service.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.innospots.nexus.service.transfer.content.BinarySources;
import com.innospots.nexus.service.transfer.download.DefaultDownloadPlanner;
import com.innospots.nexus.service.transfer.download.DownloadRequests;
import com.innospots.nexus.service.transfer.download.DownloadResource;
import com.innospots.nexus.service.transfer.download.DownloadTransferSupport;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

/**
 * JAX-RS {@link DownloadResource} 写回器。
 */
@Provider
public final class QuarkusDownloadBodyWriter implements MessageBodyWriter<DownloadResource> {

    private final DownloadTransferSupport transferSupport = new DownloadTransferSupport(new DefaultDownloadPlanner());

    @Context
    ContainerRequestContext requestContext;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return DownloadResource.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(
            DownloadResource resource,
            Class<?> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException, WebApplicationException {
        Map<String, List<String>> headers = requestHeaders();
        DownloadTransferSupport.PreparedDownload prepared = transferSupport.prepare(
                DownloadRequests.from(requestContext.getMethod(), headers),
                resource);
        httpHeaders.clear();
        prepared.plan().headers().forEach(httpHeaders::putSingle);
        if (resource.filename() != null && !resource.filename().isBlank()) {
            httpHeaders.putSingle("Content-Disposition", "attachment; filename=\"" + resource.filename() + "\"");
        }
        if (prepared.body() == null) {
            return;
        }
        BinarySources.writeTo(prepared.body(), entityStream);
    }

    private Map<String, List<String>> requestHeaders() {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        if (requestContext == null) {
            return headers;
        }
        requestContext.getHeaders().forEach((name, values) -> {
            List<String> copied = new ArrayList<>();
            if (values != null) {
                copied.addAll(values);
            }
            headers.put(name.toLowerCase(Locale.ROOT), List.copyOf(copied));
        });
        return headers;
    }
}
