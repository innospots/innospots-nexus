package com.innospots.nexus.console.role.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.mapstruct.BaseMapperConfig;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.role.domain.vo.RoleBindingVo;
import com.innospots.nexus.console.role.domain.vo.RoleOptionVo;
import com.innospots.nexus.console.role.domain.vo.RoleVo;

/**
 * 角色实体与控制台 VO 的 MapStruct 转换。
 */
@Mapper(config = BaseMapperConfig.class)
public interface RoleConverter {

    RoleConverter INSTANCE = Mappers.getMapper(RoleConverter.class);

    @Mapping(target = "ownerType", source = "entity.ownerType")
    @Mapping(target = "securityRealm", source = "entity.securityRealm")
    @Mapping(target = "status", source = "entity.status")
    @Mapping(target = "memberCount", source = "memberCount")
    RoleVo toVo(RoleEntity entity, long memberCount);

    RoleOptionVo toOption(RoleEntity entity);

    @Mapping(target = "subjectType", source = "subjectType")
    RoleBindingVo toBindingVo(RoleBindingEntity entity);

    default RoleOwnerType mapOwnerType(String ownerType) {
        if (ownerType == null || ownerType.isBlank()) {
            return null;
        }
        return RoleOwnerType.valueOf(ownerType);
    }

    default SecurityRealm mapSecurityRealm(String securityRealm) {
        if (securityRealm == null || securityRealm.isBlank()) {
            return null;
        }
        return SecurityRealm.valueOf(securityRealm);
    }

    default BasicStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return BasicStatus.ENABLED;
        }
        return BasicStatus.valueOf(status);
    }

    default String formatStatus(BasicStatus status) {
        return status == null ? BasicStatus.ENABLED.name() : status.name();
    }

    default RoleBindingSubjectType mapSubjectType(String subjectType) {
        if (subjectType == null || subjectType.isBlank()) {
            return null;
        }
        return RoleBindingSubjectType.valueOf(subjectType);
    }
}
