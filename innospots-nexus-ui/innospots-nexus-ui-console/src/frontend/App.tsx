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
        <SitemapApp
          key={pathname}
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
