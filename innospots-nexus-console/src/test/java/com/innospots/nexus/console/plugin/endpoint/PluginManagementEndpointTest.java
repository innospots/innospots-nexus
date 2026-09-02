package com.innospots.nexus.console.plugin.endpoint;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import com.innospots.nexus.base.domain.response.R;

import static org.assertj.core.api.Assertions.assertThat;

/** 插件管理接口的路径、HTTP 方法和卸载边界测试。 */
class PluginManagementEndpointTest {

    @Test
    void exposesOnlyThePlannedPluginManagementOperations() throws NoSuchMethodException {
        assertThat(PluginManagementEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo("/console/plugins");
        assertThat(PluginManagementEndpoint.class.getDeclaredMethod("list")
                .getAnnotation(GET.class)).isNotNull();
        assertThat(PluginManagementEndpoint.class.getDeclaredMethod("get", String.class)
                .getAnnotation(Path.class).value()).isEqualTo("/{pluginId}");

        assertThat(operation("install").getAnnotation(POST.class)).isNotNull();
        assertThat(operation("enable").getAnnotation(Path.class).value()).isEqualTo("/{pluginId}/enable");
        assertThat(operation("disable").getAnnotation(Path.class).value()).isEqualTo("/{pluginId}/disable");
        assertThat(operation("retry").getAnnotation(Path.class).value()).isEqualTo("/{pluginId}/retry");
        assertThat(Arrays.stream(PluginManagementEndpoint.class.getDeclaredMethods())
                .filter(method -> method.getAnnotation(DELETE.class) != null))
                .isEmpty();
    }

    @Test
    void operationsReturnTheCommonResponseWrapper() {
        assertThat(Arrays.stream(PluginManagementEndpoint.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("list")
                        || method.getName().equals("get")
                        || method.getName().equals("install")
                        || method.getName().equals("enable")
                        || method.getName().equals("disable")
                        || method.getName().equals("retry"))
                .map(Method::getReturnType))
                .containsOnly(R.class);
    }

    private static Method operation(String name) throws NoSuchMethodException {
        return PluginManagementEndpoint.class.getDeclaredMethod(name, String.class);
    }
}
