package com.innospots.nexus.platform.navigation.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.console.navigation.service.NavigationMenuComposer;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.platform.scope.PlatformConsoleSession;

/**
 * 运营平台导航：持久化菜单与 meta-sitemap 默认项组合。
 */
@RequiredArgsConstructor
public class PlatformNavigationService {

    private final NavigationMenuComposer navigationMenuComposer;
    private final AuthorizationSubjectResolver subjectResolver;

    public List<NavigationMenuVo> listNavigationMenus() {
        var ownership = PlatformConsoleSession.requireOwnership();
        return subjectResolver.resolve()
                .map(subject -> navigationMenuComposer.compose(ownership, subject))
                .orElseGet(List::of);
    }
}
