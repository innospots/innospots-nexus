package com.innospots.nexus.console.endpoint.grant.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

class EndpointGrantHttpIntrospectionTest {

    @Test
    void resolveRoutesReturnsEmptyWhenResourceOrMethodMissing(TestReporter reporter) {
        Method any = sampleGetMethod();

        List<EndpointGrantHttpIntrospection.HttpRouteBinding> nullClass =
                EndpointGrantHttpIntrospection.resolveRoutes(null, any);
        List<EndpointGrantHttpIntrospection.HttpRouteBinding> nullMethod =
                EndpointGrantHttpIntrospection.resolveRoutes(PathsOnClass.class, null);

        reporter.publishEntry("input", "resourceClass=null, method=" + any);
        reporter.publishEntry("output", String.valueOf(nullClass));
        reporter.publishEntry("input", "resourceClass=PathsOnClass, method=null");
        reporter.publishEntry("output", String.valueOf(nullMethod));

        assertThat(nullClass).isEmpty();
        assertThat(nullMethod).isEmpty();
    }

    @Test
    void resolveRoutesReturnsEmptyWhenMethodNotOnResourceType(TestReporter reporter) {
        Method foreign = String.class.getMethods()[0];

        List<EndpointGrantHttpIntrospection.HttpRouteBinding> routes =
                EndpointGrantHttpIntrospection.resolveRoutes(PathsOnClass.class, foreign);

        reporter.publishEntry("input", "resourceClass=PathsOnClass, method=" + foreign);
        reporter.publishEntry("output", String.valueOf(routes));

        assertThat(routes).isEmpty();
    }

    @Test
    void resolveRoutesReturnsEmptyWhenNoHttpVerb(TestReporter reporter) throws NoSuchMethodException {
        Method method = NoHttpVerbResource.class.getMethod("orphan");

        List<EndpointGrantHttpIntrospection.HttpRouteBinding> routes =
                EndpointGrantHttpIntrospection.resolveRoutes(NoHttpVerbResource.class, method);

        reporter.publishEntry("input", "resourceClass=NoHttpVerbResource, method=orphan");
        reporter.publishEntry("output", String.valueOf(routes));

        assertThat(routes).isEmpty();
    }

    @Test
    void resolveRoutesReadsVerbAndPathFromImplementation(TestReporter reporter) throws NoSuchMethodException {
        Method method = PathsOnClass.class.getMethod("list");

        List<EndpointGrantHttpIntrospection.HttpRouteBinding> routes =
                EndpointGrantHttpIntrospection.resolveRoutes(PathsOnClass.class, method);

        reporter.publishEntry("input", "resourceClass=PathsOnClass, method=list");
        reporter.publishEntry("output", String.valueOf(routes));

        assertThat(routes).hasSize(1);
        assertThat(routes.get(0).httpMethod()).isEqualTo("GET");
        assertThat(routes.get(0).pathTemplate()).isEqualTo("/api/items");
    }

    @Test
    void resolveRoutesReadsPathFromContractInterface(TestReporter reporter) throws NoSuchMethodException {
        Method method = PathOnInterfaceImpl.class.getMethod("detail", String.class);

        List<EndpointGrantHttpIntrospection.HttpRouteBinding> routes =
                EndpointGrantHttpIntrospection.resolveRoutes(PathOnInterfaceImpl.class, method);

        reporter.publishEntry("input", "resourceClass=PathOnInterfaceImpl, method=detail");
        reporter.publishEntry("output", String.valueOf(routes));

        assertThat(routes).singleElement().satisfies(binding -> {
            assertThat(binding.httpMethod()).isEqualTo("GET");
            assertThat(binding.pathTemplate()).isEqualTo("/contract-root/{id}");
        });
    }

    @Test
    void resolveRoutesCollectsMultipleHttpVerbs(TestReporter reporter) throws NoSuchMethodException {
        Method method = MultiVerbResource.class.getMethod("upsert");

        List<EndpointGrantHttpIntrospection.HttpRouteBinding> routes =
                EndpointGrantHttpIntrospection.resolveRoutes(MultiVerbResource.class, method);

        reporter.publishEntry("input", "resourceClass=MultiVerbResource, method=upsert");
        reporter.publishEntry("output", String.valueOf(routes));

        assertThat(routes).hasSize(2);
        assertThat(routes).extracting(EndpointGrantHttpIntrospection.HttpRouteBinding::httpMethod)
                .containsExactly("POST", "PUT");
        assertThat(routes).extracting(EndpointGrantHttpIntrospection.HttpRouteBinding::pathTemplate)
                .containsOnly("/multi");
    }

    @Test
    void httpRouteBindingNormalizesNullFields(TestReporter reporter) {
        EndpointGrantHttpIntrospection.HttpRouteBinding binding =
                new EndpointGrantHttpIntrospection.HttpRouteBinding(null, null);

        reporter.publishEntry("input", "httpMethod=null, pathTemplate=null");
        reporter.publishEntry("output", binding.toString());

        assertThat(binding.httpMethod()).isEmpty();
        assertThat(binding.pathTemplate()).isEmpty();
    }

    @Test
    void joinPathsCoversNormalizationBranches(TestReporter reporter) {
        assertJoin(reporter, "", "", "/");
        assertJoin(reporter, "", "relative", "/relative");
        assertJoin(reporter, "/base", "", "/base");
        assertJoin(reporter, "/base/", "/child", "/base/child");
        assertJoin(reporter, "/base", "child", "/base/child");
        assertJoin(reporter, "/base/", "child", "/base/child");
        assertJoin(reporter, "base", "/child", "/base/child");
        assertJoin(reporter, "/base///", "///child///", "/base///child");
    }

    private static void assertJoin(TestReporter reporter, String base, String relative, String expected) {
        String actual = EndpointGrantHttpIntrospection.joinPaths(base, relative);
        reporter.publishEntry("joinPaths input", "base='" + base + "', relative='" + relative + "'");
        reporter.publishEntry("joinPaths output", actual);
        assertThat(actual).isEqualTo(expected);
    }

    private static Method sampleGetMethod() {
        try {
            return PathsOnClass.class.getMethod("list");
        } catch (NoSuchMethodException ex) {
            throw new AssertionError(ex);
        }
    }

    @Path("/api/items")
    static final class PathsOnClass {

        @GET
        public void list() {
        }
    }

    @Path("/contract-root")
    interface PathOnContract {

        @GET
        @Path("/{id}")
        String detail(String id);
    }

    @Path("/contract-root")
    static final class PathOnInterfaceImpl implements PathOnContract {

        @Override
        public String detail(String id) {
            return id;
        }
    }

    @Path("/multi")
    static final class MultiVerbResource {

        @POST
        @PUT
        public void upsert() {
        }
    }

    @Path("/silent")
    static final class NoHttpVerbResource {

        public void orphan() {
        }
    }
}
