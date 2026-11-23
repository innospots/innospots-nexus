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
import { SitemapApp } from "pactor-app";

import { moduleExtensions } from "@generated/pactor/client-registry";

import { nexusHttpHooks } from "./nexus-http-hooks";

/**
 * 工作区外壳：sitemap 模式。
 *
 * 页面与导航由 Shell 装配产物提供，模块只贡献组件与 API。
 */
const builtinComponents = {
  NavigationShell,
  SkinGallery,
  Login,
  UserMenu: ShellUserMenu,
  SkinSwitcher,
};

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
  const actions = useMemo(() => mergeModuleActions(moduleExtensions), []);

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
    <AppShell
      menu={{ mode: "sitemap" }}
      layout={{ variant: "side-fixed" }}
      renderSubApp={() => (
        <Suspense fallback={null}>
          <ShellHost {...props} />
        </Suspense>
      )}
    />
  );
}
