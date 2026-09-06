package com.innospots.nexus.base.ui.spec.permission;

import com.innospots.nexus.base.ui.spec.PageMeta;
import com.innospots.nexus.base.ui.spec.action.ActionConfig;
import com.innospots.nexus.base.ui.spec.node.ComponentNode;
import com.innospots.nexus.base.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link PermissionConfigDeserializer} for string, array, and object forms.
 */
class PermissionConfigDeserializerTest {

    @Test
    void deserializesSinglePermissionCode() {
        PermissionConfig permission = PageDslYamlTestSupport.read(
                "customer:view",
                PermissionConfig.class);

        assertThat(permission.getKind()).isEqualTo(PermissionConfig.PermissionKind.CODE);
        assertThat(permission.getCode()).isEqualTo("customer:view");
        assertThat(permission.getDenied()).isEqualTo(PermissionDenied.HIDDEN);
    }

    @Test
    void deserializesAnyOfPermissionCodes() {
        PermissionConfig permission = PageDslYamlTestSupport.read("""
                - customer:edit
                - customer:admin
                """, PermissionConfig.class);

        assertThat(permission.getKind()).isEqualTo(PermissionConfig.PermissionKind.ANY_OF);
        assertThat(permission.getAnyOf()).containsExactly("customer:edit", "customer:admin");
    }

    @Test
    void deserializesDetailedPermissionObject() {
        PermissionConfig permission = PageDslYamlTestSupport.read("""
                code: customer:delete
                denied: disabled
                """, PermissionConfig.class);

        assertThat(permission.getKind()).isEqualTo(PermissionConfig.PermissionKind.DETAILED);
        assertThat(permission.getCode()).isEqualTo("customer:delete");
        assertThat(permission.getDenied()).isEqualTo(PermissionDenied.DISABLED);
    }

    @Test
    void defaultsDeniedToHiddenWhenOmitted() {
        PermissionConfig permission = PageDslYamlTestSupport.read("""
                code: customer:export
                """, PermissionConfig.class);

        assertThat(permission.getDenied()).isEqualTo(PermissionDenied.HIDDEN);
    }

    @Test
    void acceptsLowerCaseDeniedValue() {
        PermissionConfig permission = PageDslYamlTestSupport.read("""
                code: customer:export
                denied: hidden
                """, PermissionConfig.class);

        assertThat(permission.getDenied()).isEqualTo(PermissionDenied.HIDDEN);
    }

    @Test
    void deserializesNullPermission() {
        PermissionConfig permission = PageDslYamlTestSupport.read("null", PermissionConfig.class);

        assertThat(permission).isNull();
    }

    @Test
    void bindsPermissionOnPageMeta() {
        PageMeta meta = PageDslYamlTestSupport.read("""
                id: permission-demo
                permission: customer:view
                """, PageMeta.class);

        assertThat(meta.getPermission().getCode()).isEqualTo("customer:view");
    }

    @Test
    void bindsPermissionOnComponentNode() {
        ComponentNode node = PageDslYamlTestSupport.read("""
                type: Button
                permission:
                  code: customer:edit
                  denied: disabled
                """, ComponentNode.class);

        assertThat(node.getPermission().getKind()).isEqualTo(PermissionConfig.PermissionKind.DETAILED);
        assertThat(node.getPermission().getDenied()).isEqualTo(PermissionDenied.DISABLED);
    }

    @Test
    void bindsPermissionOnActionConfig() {
        ActionConfig action = PageDslYamlTestSupport.read("""
                action: reload
                permission: customer:view
                """, ActionConfig.class);

        assertThat(action.getPermission().getCode()).isEqualTo("customer:view");
    }
}
