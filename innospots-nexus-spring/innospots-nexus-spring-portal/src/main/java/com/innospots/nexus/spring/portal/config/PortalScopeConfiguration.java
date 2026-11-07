package com.innospots.nexus.spring.portal.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jersey.autoconfigure.ResourceConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.jaxrs.filter.WorkspaceScopeResolver;
import com.innospots.nexus.console.jaxrs.filter.WorkspaceScopeSessionFilter;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.scope.cookie.WorkspaceCookieNames;
import com.innospots.nexus.console.scope.directory.PortalProjectScopeDirectory;
import com.innospots.nexus.console.scope.directory.PortalTenantScopeDirectory;
import com.innospots.nexus.console.scope.directory.PortalWorkspaceScopeDirectory;
import com.innospots.nexus.console.scope.domain.session.SessionScopeBinder;
import com.innospots.nexus.console.scope.project.dao.ProjectDao;
import com.innospots.nexus.console.scope.project.operator.ProjectOperator;
import com.innospots.nexus.console.scope.service.ConsoleTenantContextService;
import com.innospots.nexus.console.scope.service.ConsoleWorkspaceContextService;
import com.innospots.nexus.console.scope.service.ConsoleWorkspaceScopeResolver;
import com.innospots.nexus.console.scope.service.DefaultSessionScopeBinder;
import com.innospots.nexus.console.scope.tenant.dao.TenantDao;
import com.innospots.nexus.console.scope.tenant.dao.TenantMemberDao;
import com.innospots.nexus.console.scope.workspace.dao.WorkspaceDao;
import com.innospots.nexus.console.scope.workspace.dao.WorkspaceMemberDao;
import com.innospots.nexus.console.scope.workspace.operator.WorkspaceOperator;

/**
 * portal 作用域（租户 → 工作区 → 项目）的 Spring 装配。
 *
 * <p>租户、工作区与项目标识来自客户端 cookie，逐请求校验后绑定到会话；
 * 令牌只携带身份与租户。</p>
 *
 * @author Smars
 * @date 2026/11/07
 */
@Configuration
@MapperScan(
        basePackages = {
            "com.innospots.nexus.console.scope.tenant.dao",
            "com.innospots.nexus.console.scope.workspace.dao",
            "com.innospots.nexus.console.scope.project.dao",
            "com.innospots.nexus.console.role.dao"
        },
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
@ConditionalOnProperty(prefix = "nexus.console.auth.rsa", name = "private-key")
public class PortalScopeConfiguration {

    /**
     * 工作区数据操作器。
     *
     * @param workspaceDao 工作区 DAO
     * @return 工作区操作器
     */
    @Bean
    WorkspaceOperator workspaceOperator(WorkspaceDao workspaceDao) {
        return new WorkspaceOperator(workspaceDao);
    }

    /**
     * 项目数据操作器。
     *
     * @param projectDao 项目 DAO
     * @return 项目操作器
     */
    @Bean
    ProjectOperator projectOperator(ProjectDao projectDao) {
        return new ProjectOperator(projectDao);
    }

    /**
     * 租户作用域目录。
     *
     * @param tenantDao 租户 DAO
     * @return 租户目录
     */
    @Bean
    PortalTenantScopeDirectory tenantScopeDirectory(TenantDao tenantDao) {
        return new PortalTenantScopeDirectory(tenantDao);
    }

    /**
     * 工作区作用域目录。
     *
     * @param workspaceOperator 工作区操作器
     * @return 工作区目录
     */
    @Bean
    PortalWorkspaceScopeDirectory workspaceScopeDirectory(WorkspaceOperator workspaceOperator) {
        return new PortalWorkspaceScopeDirectory(workspaceOperator);
    }

    /**
     * 项目作用域目录。
     *
     * @param projectOperator 项目操作器
     * @return 项目目录
     */
    @Bean
    PortalProjectScopeDirectory projectScopeDirectory(ProjectOperator projectOperator) {
        return new PortalProjectScopeDirectory(projectOperator);
    }

    /**
     * 会话作用域绑定：令牌只绑定身份与租户。
     *
     * @param tenantScopeDirectory 租户目录
     * @return 会话绑定器
     */
    @Bean
    SessionScopeBinder sessionScopeBinder(PortalTenantScopeDirectory tenantScopeDirectory) {
        return new DefaultSessionScopeBinder(tenantScopeDirectory);
    }

    /**
     * 租户上下文解析（租户 + 成员 + 角色编码）。
     *
     * @param tenantDao       租户 DAO
     * @param tenantMemberDao 租户成员 DAO
     * @param roleBindingDao  角色绑定 DAO
     * @param roleDao         角色 DAO
     * @return 租户上下文服务
     */
    @Bean
    ConsoleTenantContextService consoleTenantContextService(
            TenantDao tenantDao,
            TenantMemberDao tenantMemberDao,
            RoleBindingDao roleBindingDao,
            RoleDao roleDao) {
        return new ConsoleTenantContextService(
                tenantDao, tenantMemberDao, roleBindingDao, roleDao);
    }

    /**
     * 工作区上下文解析（工作区 + 成员 + 角色编码）。
     *
     * @param workspaceOperator   工作区操作器
     * @param workspaceMemberDao  工作区成员 DAO
     * @param roleBindingDao      角色绑定 DAO
     * @param roleDao             角色 DAO
     * @return 工作区上下文服务
     */
    @Bean
    ConsoleWorkspaceContextService consoleWorkspaceContextService(
            WorkspaceOperator workspaceOperator,
            WorkspaceMemberDao workspaceMemberDao,
            RoleBindingDao roleBindingDao,
            RoleDao roleDao) {
        return new ConsoleWorkspaceContextService(
                workspaceOperator, workspaceMemberDao, roleBindingDao, roleDao);
    }

    /**
     * 工作区作用域解析器：租户与工作区标识来自 cookie，逐请求校验。
     *
     * @param tenantContextService    租户上下文解析
     * @param workspaceContextService 工作区上下文解析
     * @param projectOperator         项目归属校验
     * @return 作用域解析器
     */
    @Bean
    WorkspaceScopeResolver workspaceScopeResolver(
            ConsoleTenantContextService tenantContextService,
            ConsoleWorkspaceContextService workspaceContextService,
            ProjectOperator projectOperator) {
        return new ConsoleWorkspaceScopeResolver(
                tenantContextService,
                workspaceContextService,
                projectOperator::findSnapshot,
                new ConsoleWorkspaceScopeResolver.Names(
                        WorkspaceCookieNames.TENANT,
                        WorkspaceCookieNames.WORKSPACE,
                        WorkspaceCookieNames.PROJECT));
    }

    /**
     * 补绑租户 / 工作区 / 项目作用域，早于资源权限校验执行。
     *
     * @param workspaceScopeResolver 作用域解析器
     * @return 作用域过滤器
     */
    @Bean
    WorkspaceScopeSessionFilter workspaceScopeSessionFilter(
            WorkspaceScopeResolver workspaceScopeResolver) {
        return new WorkspaceScopeSessionFilter(workspaceScopeResolver);
    }

    /**
     * 把工作区作用域过滤器显式注册到 Jersey。
     *
     * <p>{@code @Provider} 能被自动发现，但端口配置与过滤器都在本类内声明，
     * 显式注册可以保证注册顺序与作用域一致，也便于在测试中替换解析器。</p>
     *
     * @param workspaceScopeSessionFilter 工作区作用域过滤器
     * @return Jersey 资源定制器
     */
    @Bean
    ResourceConfigCustomizer portalWorkspaceScopeJerseyCustomizer(
            WorkspaceScopeSessionFilter workspaceScopeSessionFilter) {
        return resourceConfig -> resourceConfig.register(workspaceScopeSessionFilter);
    }
}
