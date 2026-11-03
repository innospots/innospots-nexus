"use client";

import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { Suspense, useCallback, useMemo } from "react";
import { SitemapApp } from "pactor-app";

import { apiBaseUrl, pageEndpoint, sitemapSources } from "./config";
import { pactorActions } from "./actions";
import { pactorComponents } from "./components";
import { nexusHttpHooks } from "./http-hooks";
import { translateGate } from "./i18n/messages";
import type { ConsoleLocale } from "./locale";
import { NavigationLayoutProvider } from "./navigation-layout-context";
import {
  PageAccessDenied,
  PageAccessGateLoading,
  PageAccessPending,
  usePageAccessGate,
} from "./page-access";

/**
 * SitemapApp + catalog 页面访问门禁：路由变化先 POST /api/page-access/verify；
 * 懒加载 YAML 页 GET /api/public/pages/{id} 在服务端同样校验（403）。
 */

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
  const replace = useCallback((to: string) => router.replace(to), [router]);
  const navigate = useCallback((to: string) => router.push(to), [router]);

  const { committed, status, retry } = usePageAccessGate(
    pathname,
    search,
    replace,
    locale,
  );

  const committedQuery = useMemo(
    () => Object.fromEntries(new URLSearchParams(committed?.search ?? "")),
    [committed?.search],
  );

  const denied = status.status === "denied";
  const redirecting = denied && status.redirectTo !== undefined;

  if (committed === null) {
    if (denied && !redirecting) {
      return (
        <PageAccessDenied
          locale={locale}
          reason={status.reason}
          onRetry={retry}
          onHome={() => router.replace("/")}
        />
      );
    }
    return (
      <PageAccessGateLoading
        text={translateGate(
          locale,
          redirecting ? "gate.redirecting" : "gate.checking",
        )}
      />
    );
  }

  const backToCommitted = () => {
    replace(
      `${committed.pathname}${committed.search === "" ? "" : `?${committed.search}`}`,
    );
  };

  return (
    <NavigationLayoutProvider>
      <div style={{ display: denied ? "none" : "contents" }}>
        <SitemapApp
          sources={sitemapSources}
          baseUrl={apiBaseUrl}
          pathname={committed.pathname}
          query={committedQuery}
          navigate={navigate}
          components={pactorComponents}
          actions={pactorActions}
          pageEndpoint={pageEndpoint}
          httpAdapter={nexusHttpHooks}
          locale={locale}
          defaultLocale={defaultLocale}
          messages={messages}
        />
      </div>
      {status.status === "checking" ? <PageAccessPending locale={locale} /> : null}
      {redirecting ? (
        <PageAccessGateLoading text={translateGate(locale, "gate.redirecting")} />
      ) : null}
      {denied && !redirecting ? (
        <PageAccessDenied
          locale={locale}
          reason={status.reason}
          onRetry={retry}
          onBack={backToCommitted}
          onHome={() => router.replace("/")}
        />
      ) : null}
    </NavigationLayoutProvider>
  );
}
