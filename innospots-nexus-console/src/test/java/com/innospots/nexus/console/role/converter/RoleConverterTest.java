package com.innospots.nexus.console.role.converter;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.role.domain.vo.RoleBindingVo;
import com.innospots.nexus.console.role.domain.vo.RoleOptionVo;
import com.innospots.nexus.console.role.domain.vo.RoleVo;

import static org.assertj.core.api.Assertions.assertThat;

class RoleConverterTest {

    private final RoleConverter converter = RoleConverter.INSTANCE;

    @Test
    void mapsRoleEntityToVoWithMemberCount() {
        RoleEntity entity = sampleRole();
        entity.setCreatedAt(LocalDateTime.of(2026, 9, 1, 10, 0));
        entity.setUpdatedAt(LocalDateTime.of(2026, 9, 2, 11, 0));

        RoleVo vo = converter.toVo(entity, 3L);

        assertThat(vo.roleId()).isEqualTo("rol-1");
        assertThat(vo.ownerType()).isEqualTo(RoleOwnerType.WORKSPACE);
        assertThat(vo.securityRealm()).isEqualTo(SecurityRealm.TENANT);
        assertThat(vo.status()).isEqualTo(BasicStatus.ENABLED);
        assertThat(vo.memberCount()).isEqualTo(3L);
        assertThat(vo.createdAt()).isEqualTo(entity.getCreatedAt());
    }

    @Test
    void mapsRoleEntityToOption() {
        RoleOptionVo option = converter.toOption(sampleRole());

        assertThat(option.roleId()).isEqualTo("rol-1");
        assertThat(option.roleCode()).isEqualTo("admin");
        assertThat(option.administrator()).isFalse();
    }

    @Test
    void mapsBindingEntityToVo() {
        RoleBindingEntity entity = new RoleBindingEntity();
        entity.setBindingId("rbn-1");
        entity.setRoleId("rol-1");
        entity.setSubjectType(RoleBindingSubjectType.USER.name());
        entity.setSubjectId("user-1");
        entity.setCreatedAt(LocalDateTime.of(2026, 9, 3, 8, 0));

        RoleBindingVo vo = converter.toBindingVo(entity);

        assertThat(vo.bindingId()).isEqualTo("rbn-1");
        assertThat(vo.subjectType()).isEqualTo(RoleBindingSubjectType.USER);
        assertThat(vo.subjectId()).isEqualTo("user-1");
    }

    private static RoleEntity sampleRole() {
        RoleEntity entity = new RoleEntity();
        entity.setRoleId("rol-1");
        entity.setRoleName("Admin");
        entity.setRoleCode("admin");
        entity.setOwnerType(RoleOwnerType.WORKSPACE.name());
        entity.setOwnerId("ws-1");
        entity.setSecurityRealm(SecurityRealm.TENANT.name());
        entity.setStatus(BasicStatus.ENABLED.name());
        entity.setSortOrder(1);
        entity.setBuiltIn(false);
        entity.setAdministrator(false);
        return entity;
    }
}
