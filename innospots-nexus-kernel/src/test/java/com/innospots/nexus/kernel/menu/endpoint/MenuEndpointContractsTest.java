package com.innospots.nexus.kernel.menu.endpoint;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.kernel.menu.domain.enums.MenuOpenMode;
import com.innospots.nexus.kernel.menu.domain.enums.MenuType;
import com.innospots.nexus.kernel.menu.domain.request.MenuCreateRequest;
import com.innospots.nexus.kernel.menu.domain.request.MenuOrderRequest;
import com.innospots.nexus.kernel.menu.domain.request.MenuStatusUpdateRequest;
import com.innospots.nexus.kernel.menu.domain.request.MenuTreeRequest;
import com.innospots.nexus.kernel.menu.domain.request.MenuUpdateRequest;
import com.innospots.nexus.kernel.menu.domain.vo.MenuOptionVo;
import com.innospots.nexus.kernel.menu.domain.vo.MenuVo;
import com.innospots.nexus.kernel.menu.domain.vo.NavigationMenuVo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MenuEndpointContractsTest {

    @Test
    void menuEndpointOwnsManagementOperations() throws NoSuchMethodException {
        assertThat(MenuEndpoint.class.isInterface()).isFalse();
        assertThat(MenuEndpoint.class.getAnnotation(Path.class).value()).isEqualTo("/console/menus");
        assertHttpMethod(MenuEndpoint.class, "listMenuTree", GET.class, MenuTreeRequest.class);
        assertHttpMethod(MenuEndpoint.class, "getMenu", GET.class, String.class);
        assertHttpMethod(MenuEndpoint.class, "createMenu", POST.class, MenuCreateRequest.class);
        assertHttpMethod(MenuEndpoint.class, "updateMenu", PUT.class, String.class, MenuUpdateRequest.class);
        assertHttpMethod(MenuEndpoint.class, "updateMenuStatus",
                PUT.class, String.class, MenuStatusUpdateRequest.class);
        assertHttpMethod(MenuEndpoint.class, "reorderMenus", PUT.class, MenuOrderRequest.class);
        assertHttpMethod(MenuEndpoint.class, "deleteMenu", DELETE.class, String.class);
        assertHttpMethod(MenuEndpoint.class, "listMenuOptions", GET.class);
    }

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
    void endpointMethodsRemainExplicitlyUnimplemented() {
        MenuEndpoint menuEndpoint = new MenuEndpoint();
        NavigationMenuEndpoint navigationEndpoint = new NavigationMenuEndpoint();

        assertThatThrownBy(() -> menuEndpoint.listMenuTree(new MenuTreeRequest()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> menuEndpoint.getMenu("menu-1"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> menuEndpoint.createMenu(sampleCreateRequest()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> menuEndpoint.updateMenu("menu-1", sampleUpdateRequest()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> menuEndpoint.updateMenuStatus(
                "menu-1", new MenuStatusUpdateRequest(BasicStatus.DISABLED)))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> menuEndpoint.reorderMenus(
                new MenuOrderRequest(null, List.of("menu-1"))))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> menuEndpoint.deleteMenu("menu-1"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(menuEndpoint::listMenuOptions)
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(navigationEndpoint::listNavigationMenus)
                .isInstanceOf(UnsupportedOperationException.class);
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
                MenuOpenMode.INTERNAL, null);

        assertThat(orderRequest.menuIds()).isEqualTo(List.of());
        assertThat(menu.children()).isEqualTo(List.of());
        assertThat(navigation.children()).isEqualTo(List.of());
    }

    private static MenuCreateRequest sampleCreateRequest() {
        return new MenuCreateRequest(
                null, "home", "Home", MenuType.PAGE, "/home", "home-page",
                null, null, "home", MenuOpenMode.INTERNAL, true, 1);
    }

    private static MenuUpdateRequest sampleUpdateRequest() {
        return new MenuUpdateRequest(
                null, "Home", MenuType.PAGE, "/home", "home-page",
                null, null, "home", MenuOpenMode.INTERNAL, true, 1);
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
