package com.innospots.nexus.console.menu.endpoint;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.menu.domain.enums.MenuOpenMode;
import com.innospots.nexus.console.menu.domain.enums.MenuType;
import com.innospots.nexus.console.menu.domain.request.MenuCreateRequest;
import com.innospots.nexus.console.menu.domain.request.MenuOrderRequest;
import com.innospots.nexus.console.menu.domain.request.MenuStatusUpdateRequest;
import com.innospots.nexus.console.menu.domain.request.MenuTreeRequest;
import com.innospots.nexus.console.menu.domain.request.MenuUpdateRequest;
import com.innospots.nexus.console.menu.domain.vo.MenuOptionVo;
import com.innospots.nexus.console.menu.domain.vo.MenuVo;
import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.console.navigation.endpoint.NavigationMenuEndpoint;

import static org.assertj.core.api.Assertions.assertThat;

class NavigationMenuEndpointContractsTest {

    @Test
    void navigationEndpointRemainsReadOnlyAndSeparate() throws NoSuchMethodException {
        assertThat(NavigationMenuEndpoint.class.isInterface()).isFalse();
        assertThat(NavigationMenuEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo("/console/navigation/menus");
        assertHttpMethod(NavigationMenuEndpoint.class, "listNavigationMenus", GET.class);
        assertThat(Arrays.stream(NavigationMenuEndpoint.class.getMethods())
                .filter(method -> method.getDeclaringClass().equals(NavigationMenuEndpoint.class)))
                .allMatch(method -> method.getAnnotation(GET.class) != null);
    }

    @Test
    void requestsAndViewsExposeStableMenuData() {
        assertRecordComponents(MenuCreateRequest.class,
                "parentId", "menuKey", "menuName", "menuType", "routePath",
                "componentKey", "redirectPath", "externalUrl", "icon",
                "openMode", "visible", "sortOrder");
        assertRecordComponents(MenuUpdateRequest.class,
                "parentId", "menuName", "menuType", "routePath",
                "componentKey", "redirectPath", "externalUrl", "icon",
                "openMode", "visible", "sortOrder");
        assertRecordComponents(MenuStatusUpdateRequest.class, "status");
        assertRecordComponents(MenuOrderRequest.class, "parentId", "menuIds");
        assertRecordComponents(MenuTreeRequest.class, "input", "menuType", "status", "visible");

        assertThat(MenuVo.class.isRecord()).isTrue();
        assertThat(MenuOptionVo.class.isRecord()).isTrue();
        assertThat(NavigationMenuVo.class.isRecord()).isTrue();
    }

    @Test
    void treeCollectionsDefensivelyCopyInput() {
        MenuOrderRequest orderRequest = new MenuOrderRequest(null, null);
        MenuVo menu = new MenuVo(
                "menu-1", null, "home", "Home", MenuType.PAGE, "/home",
                "home-page", null, null, "home", MenuOpenMode.INTERNAL,
                true, BasicStatus.ENABLED, 1, true, null, null, null);
        NavigationMenuVo navigation = new NavigationMenuVo(
                "home", "Home", "/home", "home-page", null, "home",
                MenuOpenMode.INTERNAL, "menu-1", "plugin-1", "home", "home.home-main", null);

        assertThat(orderRequest.menuIds()).isEqualTo(List.of());
        assertThat(menu.children()).isEqualTo(List.of());
        assertThat(navigation.children()).isEqualTo(List.of());
    }

    private static void assertHttpMethod(
            Class<?> endpointType,
            String methodName,
            Class<? extends java.lang.annotation.Annotation> httpAnnotation,
            Class<?>... parameterTypes
    ) throws NoSuchMethodException {
        Method method = endpointType.getMethod(methodName, parameterTypes);
        assertThat(method.getAnnotation(httpAnnotation)).isNotNull();
    }

    private static void assertRecordComponents(Class<?> recordType, String... names) {
        assertThat(recordType.isRecord()).isTrue();
        assertThat(Arrays.stream(recordType.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly(names);
    }
}
