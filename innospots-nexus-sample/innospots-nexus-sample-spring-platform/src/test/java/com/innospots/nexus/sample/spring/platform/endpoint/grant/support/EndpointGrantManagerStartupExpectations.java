package com.innospots.nexus.sample.spring.platform.endpoint.grant.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;

import com.innospots.nexus.spring.core.jaxrs.NexusJaxRsConfiguration;

import com.innospots.nexus.console.catalog.endpoint.ConsoleCatalogEndpoint;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantDescriptor;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.core.endpoint.grant.EndpointModuleGrantCatalog;
import com.innospots.nexus.core.endpoint.grant.support.EndpointGrantAnnotations;
import com.innospots.nexus.core.endpoint.grant.support.EndpointGrantHttpIntrospection;
import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;
import com.innospots.nexus.console.endpoint.MainRootEndpoint;
import com.innospots.nexus.console.plugin.endpoint.PluginManagementEndpoint;
import com.innospots.nexus.platform.dictionary.endpoint.PlatformDictionaryTypeEndpoint;
import com.innospots.nexus.platform.user.endpoint.PlatformUserEndpoint;

/**
 * 与 {@link com.innospots.nexus.spring.console.config.ConsoleEndpointGrantConfiguration}
 * 使用相同扫描规则，独立计算启动后应有的 grant 目录（不 mock）。
 */
public final class EndpointGrantManagerStartupExpectations {

    public static final Set<Class<?>> REQUIRED_RESOURCE_TYPES = Set.of(
            PlatformDictionaryTypeEndpoint.class,
            PlatformUserEndpoint.class,
            ConsoleCatalogEndpoint.class,
            MainRootEndpoint.class,
            PluginManagementEndpoint.class);

    private EndpointGrantManagerStartupExpectations() {
    }

    public static Set<Class<?>> collectGrantResourceTypes(ApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory =
                (ConfigurableListableBeanFactory) context.getAutowireCapableBeanFactory();
        Set<Class<?>> resourceTypes = new LinkedHashSet<>();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            Class<?> resourceClass = NexusJaxRsConfiguration.resolveEndpointClass(beanFactory, beanName);
            if (resourceClass == null) {
                continue;
            }
            if (EndpointGrantAnnotations.isGrantResourceType(resourceClass)) {
                resourceTypes.add(resourceClass);
            }
        }
        return resourceTypes;
    }

    public static Map<String, EndpointModuleGrantCatalog> computeExpectedCatalogs(ApplicationContext context) {
        EndpointGrantManager scratch = new EndpointGrantManager();
        for (Class<?> resourceClass : collectGrantResourceTypes(context)) {
            scratch.registerResource(resourceClass);
        }
        return scratch.allModuleCatalogs();
    }

    public static List<EndpointGrantDescriptor> flattenCatalog(EndpointModuleGrantCatalog catalog) {
        List<EndpointGrantDescriptor> merged = new ArrayList<>(catalog.readOnly().size() + catalog.operable().size());
        merged.addAll(catalog.readOnly());
        merged.addAll(catalog.operable());
        return merged;
    }

    public static Map<String, Long> countDescriptorsByModule(Map<String, EndpointModuleGrantCatalog> catalogs) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Map.Entry<String, EndpointModuleGrantCatalog> entry : catalogs.entrySet()) {
            counts.put(entry.getKey(), (long) flattenCatalog(entry.getValue()).size());
        }
        return counts;
    }

    public static Set<String> descriptorKeys(Iterable<EndpointGrantDescriptor> descriptors) {
        Set<String> keys = new TreeSet<>();
        for (EndpointGrantDescriptor descriptor : descriptors) {
            keys.add(descriptorKey(descriptor));
        }
        return keys;
    }

    public static String descriptorKey(EndpointGrantDescriptor descriptor) {
        return descriptor.moduleKey()
                + "|"
                + descriptor.name()
                + "|"
                + descriptor.httpMethod()
                + "|"
                + descriptor.path()
                + "|"
                + descriptor.resourceClass()
                + "|"
                + descriptor.resourceMethod();
    }

    public static Set<String> allDescriptorKeys(Map<String, EndpointModuleGrantCatalog> catalogs) {
        Set<String> keys = new TreeSet<>();
        for (EndpointModuleGrantCatalog catalog : catalogs.values()) {
            keys.addAll(descriptorKeys(flattenCatalog(catalog)));
        }
        return keys;
    }

    public static List<String> formatCatalogSummary(Map<String, EndpointModuleGrantCatalog> catalogs) {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, EndpointModuleGrantCatalog> entry : catalogs.entrySet()) {
            EndpointModuleGrantCatalog catalog = entry.getValue();
            lines.add(String.format(
                    "module=%s readOnly=%d operable=%d ignoreAuth=%d ignorePermission=%d",
                    entry.getKey(),
                    catalog.readOnly().size(),
                    catalog.operable().size(),
                    catalog.ignoreAuth().size(),
                    catalog.ignorePermission().size()));
        }
        return lines;
    }

    /**
     * 对单个资源类枚举将纳入 manager 的 descriptor（与 {@link EndpointGrantManager} 注册逻辑一致）。
     */
    public static List<EndpointGrantDescriptor> descriptorsForResource(Class<?> resourceClass) {
        EndpointGrantManager scratch = new EndpointGrantManager();
        scratch.registerResource(resourceClass);
        List<EndpointGrantDescriptor> descriptors = new ArrayList<>();
        for (EndpointModuleGrantCatalog catalog : scratch.allModuleCatalogs().values()) {
            descriptors.addAll(flattenCatalog(catalog));
        }
        return descriptors;
    }

    public static boolean wouldRegister(Method method, Class<?> resourceClass) {
        ResolvedEndpointGrant grant = EndpointGrantAnnotations.resolve(method, resourceClass);
        if (grant == null || grant.moduleKey().isBlank()) {
            return false;
        }
        return !EndpointGrantHttpIntrospection.resolveRoutes(resourceClass, method).isEmpty();
    }

    public static long countRegisteredMethods(Class<?> resourceClass) {
        long count = 0;
        for (Method method : resourceClass.getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            if (wouldRegister(method, resourceClass)) {
                List<EndpointGrantHttpIntrospection.HttpRouteBinding> bindings =
                        EndpointGrantHttpIntrospection.resolveRoutes(resourceClass, method);
                count += bindings.size();
            }
        }
        return count;
    }

    public static long totalDescriptorCount(Map<String, EndpointModuleGrantCatalog> catalogs) {
        return catalogs.values().stream()
                .mapToLong(catalog -> flattenCatalog(catalog).size())
                .sum();
    }

    public static Set<String> allGrantNames(Map<String, EndpointModuleGrantCatalog> catalogs) {
        Set<String> names = new TreeSet<>();
        for (EndpointModuleGrantCatalog catalog : catalogs.values()) {
            flattenCatalog(catalog).stream().map(EndpointGrantDescriptor::name).forEach(names::add);
        }
        return names;
    }

    public static Stream<EndpointGrantDescriptor> streamAll(Map<String, EndpointModuleGrantCatalog> catalogs) {
        return catalogs.values().stream().flatMap(catalog -> flattenCatalog(catalog).stream());
    }
}
