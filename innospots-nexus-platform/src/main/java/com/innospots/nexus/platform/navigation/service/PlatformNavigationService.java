package com.innospots.nexus.platform.navigation.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.console.navigation.service.NavigationMenuAssembler;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipLevel;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 运营平台导航：PLATFORM 会话与归属固化，委托 {@link NavigationMenuAssembler}。
 */
@RequiredArgsConstructor
public class PlatformNavigationService {

    private final NavigationMenuAssembler navigationMenuAssembler;
    private final AuthorizationSubjectResolver subjectResolver;

    public List<NavigationMenuVo> listNavigationMenus() {
        ConsoleOwnership ownership = requirePlatformOwnership();
        return subjectResolver.resolve()
                .map(subject -> navigationMenuAssembler.navigationMenus(ownership, subject))
                .orElseGet(List::of);
    }

    private ConsoleOwnership requirePlatformOwnership() {
        if (ConsoleSessionContext.scope() != ConsoleSessionScope.PLATFORM) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        ConsoleOwnership ownership = ConsoleOwnershipScope.requireOwnership(ConsoleOwnershipLevel.PLATFORM);
        if (ownership.ownerType() != RoleOwnerType.PLATFORM) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        return ownership;
    }
}
