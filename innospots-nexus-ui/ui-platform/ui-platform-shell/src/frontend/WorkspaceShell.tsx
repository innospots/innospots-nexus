"use client";

import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { Suspense, useMemo } from "react";
import { Login } from "@pactor-app/blocks";
import {
  AppShell,
  NavigationShell,
  mergeModuleActions,
  mergeModuleComponents,
  ShellLoading,
  ShellUserMenu,
  SkinGallery,
  SkinSwitcher,
} from "@pactor-app/shell/client";
import { NavigationLayoutProvider } from "@innospots/ui-platform-app/client";
import { SitemapApp } from "pactor-app";

import { moduleExtensions } from "@generated/pactor/client-registry";

import { createShellFetcher } from "./nav-visibility";
import { nexusHttpHooks } from "./nexus-http-hooks";

/**
 * 工作区外壳：sitemap 模式。
 *
 * 与 `PactorShellHost` 的差异：Shell 需要把已装配模块的语言包（messages）
 * 透传给 SitemapApp，否则模块自定义组件里的 `useI18n().t()` 只会显示 key。
 * 组件合并、懒加载端点与内置组件注册保持与 PactorShellHost 一致。
 */
const builtinComponents = {
  NavigationShell,
  SkinGallery,
  Login,
  UserMenu: ShellUserMenu,
  SkinSwitcher,
};

const shellFetcher = createShellFetcher();
const pageEndpoint = (pageId: string) =>
  `/api/runtime/pages/${encodeURIComponent(pageId)}`;

interface WorkspaceShellProps {
  locale: string;
  messages: Record<string, string>;
}

function ShellHost({ locale, messages }: WorkspaceShellProps) {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const router = useRouter();

  const query = useMemo(
    () => Object.fromEntries(searchParams.entries()),
    [searchParams],
  );
  const components = useMemo(
    () => ({
      ...builtinComponents,
      ...mergeModuleComponents(moduleExtensions),
    }),
    [],
  );
  const actions = useMemo(
    () => ({ ...mergeModuleActions(moduleExtensions) }),
    [],
  );

  return (
    <SitemapApp
      sources={["/api/runtime/bootstrap"]}
      baseUrl=""
      pathname={pathname}
      query={query}
      navigate={(to) => router.push(to)}
      components={components}
      actions={actions}
      httpAdapter={nexusHttpHooks}
      fetcher={shellFetcher}
      pageEndpoint={pageEndpoint}
      locale={locale}
      defaultLocale="zh-CN"
      messages={messages}
      loading={<ShellLoading />}
    />
  );
}

export function WorkspaceShell(props: WorkspaceShellProps) {
  return (
    <NavigationLayoutProvider themeSync={false}>
      <AppShell
        menu={{ mode: "sitemap" }}
        layout={{ variant: "side-fixed" }}
        renderSubApp={() => (
          <Suspense fallback={null}>
            <ShellHost {...props} />
          </Suspense>
        )}
      />
    </NavigationLayoutProvider>
  );
}
