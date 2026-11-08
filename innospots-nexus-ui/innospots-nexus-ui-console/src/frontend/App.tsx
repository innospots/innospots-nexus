"use client";

import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { Suspense, useCallback, useMemo } from "react";
import { SitemapApp } from "pactor-app";

import { apiBaseUrl, pageEndpoint, sitemapSources } from "./config";
import { pactorActions } from "./actions";
import { pactorComponents } from "./components";
import { nexusHttpHooks } from "./http-hooks";
import type { ConsoleLocale } from "./locale";
import { NavigationLayoutProvider } from "./navigation-layout-context";
/** 页面 DSL 在 Next.js 服务端完成访问检查。 */

export function PactorApp({
  locale,
  defaultLocale,
  messages,
}: {
  locale: ConsoleLocale;
  defaultLocale: ConsoleLocale;
  messages: Record<string, string>;
}) {
  return (
    <Suspense fallback={null}>
      <PactorAppInner
        locale={locale}
        defaultLocale={defaultLocale}
        messages={messages}
      />
    </Suspense>
  );
}

function PactorAppInner({
  locale,
  defaultLocale,
  messages,
}: {
  locale: ConsoleLocale;
  defaultLocale: ConsoleLocale;
  messages: Record<string, string>;
}) {
  const pathname = usePathname() ?? "/";
  const searchParams = useSearchParams();
  const search = searchParams.toString();
  const router = useRouter();
  const navigate = useCallback((to: string) => router.push(to), [router]);

  const query = useMemo(() => Object.fromEntries(new URLSearchParams(search)), [search]);

  return (
    <NavigationLayoutProvider>
        {/*
         * 不要给 SitemapApp 加 key={pathname}: 它会使整个应用（含导航壳）在每次路由变化时
         * 重挂载，重新拉取 sitemap 并进入 loading，造成页面闪动。SitemapApp 内部已按
         * pathname 重新解析路由，外层 admin 布局保持常驻。
         */}
        <SitemapApp
          sources={sitemapSources}
          baseUrl={apiBaseUrl}
          pathname={pathname}
          query={query}
          navigate={navigate}
          components={pactorComponents}
          actions={pactorActions}
          pageEndpoint={pageEndpoint}
          httpAdapter={nexusHttpHooks}
          locale={locale}
          defaultLocale={defaultLocale}
          messages={messages}
        />
    </NavigationLayoutProvider>
  );
}
