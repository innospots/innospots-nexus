package com.innospots.nexus.spring.console.jaxrs;

import java.lang.reflect.Method;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;

import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.ResourceMethod;

import com.innospots.nexus.console.jaxrs.support.EndpointMethodResolver;

/**
 * Spring Jersey 宿主通过请求路由元数据定位当前 endpoint 方法。
 *
 * @author Smars
 * @date 2026/10/17
 * @see EndpointMethodResolver
 * @see ExtendedUriInfo
 */
public final class JerseyEndpointMethodResolver implements EndpointMethodResolver {

    @Override
    public ResourceInfo resolve(ContainerRequestContext requestContext) {
        if (!(requestContext.getUriInfo() instanceof ExtendedUriInfo uriInfo)) {
            return null;
        }
        ResourceMethod matched = uriInfo.getMatchedResourceMethod();
        if (matched == null) {
            return null;
        }
        Invocable invocable = matched.getInvocable();
        return new ResourceInfo() {
            @Override
            public Method getResourceMethod() {
                return invocable.getHandlingMethod();
            }

            @Override
            public Class<?> getResourceClass() {
                return invocable.getHandler().getHandlerClass();
            }
        };
    }
}
