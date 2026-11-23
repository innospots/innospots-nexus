"use client";

import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { Suspense, useMemo } from "react";
import { SitemapApp } from "pactor-app";

import { pactorActions } from "./actions";
import { pactorComponents } from "./components";
import { apiBaseUrl, pageEndpoint, sitemapSources } from "./config";
import { nexusHttpHooks } from "./http-hooks";

export function PactorApp() {
  return (
    <Suspense fallback={null}>
      <PactorAppInner />
    </Suspense>
  );
}

function PactorAppInner() {
  const pathname = usePathname() ?? "/";
  const searchParams = useSearchParams();
  const search = searchParams.toString();
  const router = useRouter();
  const query = useMemo(
    () => Object.fromEntries(new URLSearchParams(search)),
    [search],
  );

  return (
    <SitemapApp
      sources={sitemapSources}
      baseUrl={apiBaseUrl}
      pathname={pathname}
      query={query}
      navigate={(to) => router.push(to)}
      components={pactorComponents}
      actions={pactorActions}
      pageEndpoint={pageEndpoint}
      httpAdapter={nexusHttpHooks}
      loading={<PortalLoading />}
    />
  );
}

function PortalLoading() {
  return (
    <div className="portal-loading">
      <span className="portal-loading-dot" />
      <span>加载中…</span>
    </div>
  );
}
