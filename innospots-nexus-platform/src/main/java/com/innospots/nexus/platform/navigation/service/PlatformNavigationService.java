package com.innospots.nexus.platform.navigation.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.console.navigation.service.NavigationMenuAssembler;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.platform.scope.PlatformConsoleSession;

/**
 * 运营平台导航：PLATFORM 会话与归属固化，委托 {@link NavigationMenuAssembler}。
 */
@RequiredArgsConstructor
public class PlatformNavigationService {

    private final NavigationMenuAssembler navigationMenuAssembler;
    private final AuthorizationSubjectResolver subjectResolver;

    public List<NavigationMenuVo> listNavigationMenus() {
        var ownership = PlatformConsoleSession.requireOwnership();
        return subjectResolver.resolve()
                .map(subject -> navigationMenuAssembler.navigationMenus(ownership, subject))
                .orElseGet(List::of);
    }
}
