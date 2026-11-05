import { cookies } from "next/headers";

import { resolveSessionUser, readPlatformAccessToken } from "@/backend/auth/accounts";
import { corsHeaders, corsPreflight } from "@/backend/cors";
import { getRequestI18n } from "@/backend/pages/i18n";
import { verifyPageAccess } from "@/backend/pages/page-access";
import { findPageDslById, getLazyPage } from "@/backend/pages/sitemap";
import { applyActionAccess, collectActionRequests, stripAccessDeclarations } from "@/backend/pages/action-access";
import type { PageDsl } from "@pactor-app/core";

/**
 * 懒加载页面端点：sitemap 中未内联 pageDsl 的页面按需获取。
 * 返回 PageResource envelope（id / version / dslVersion / schema），
 * 接口侧鉴权——前端隐藏菜单不等于安全。
 */
export async function GET(
  request: Request,
  context: RouteContext<"/api/public/pages/[id]">,
) {
  const { id } = await context.params;
  const store = await cookies();
  const user = resolveSessionUser(store);
  const i18n = await getRequestI18n(request);
  const source = findPageDslById(id);
  const requests = source ? collectActionRequests(source, source.actions ?? {}) : [];
  const access = await verifyPageAccess({
    pageId: id,
    user,
    accessToken: readPlatformAccessToken(store),
    cookie: request.headers.get("cookie"),
    requests,
  });
  if (!access.allowed) {
    return Response.json(
      {
        error: access.reason,
        reason: access.reason,
        redirectTo: access.redirectTo,
      },
      { status: 403, headers: corsHeaders(request) },
    );
  }
  const entry = getLazyPage(id, user, i18n);
  if (!entry) {
    return Response.json(
      {
        error: i18n.messages["common.ui.the-page-does-not-exist-or-you-do-not-have-permission"]
          ?? "common.ui.the-page-does-not-exist-or-you-do-not-have-permission",
      },
      { status: 404, headers: corsHeaders(request) },
    );
  }
  const failureMessage = i18n.locale === "en-US"
    ? "Permission check failed; affected actions are unavailable."
    : undefined;
  const permissions = access.requestPermissions ?? {};
  const groups = access.grantGroups ?? [];
  const incomplete = requests.some(({ method, url }) => typeof permissions[`${method}:${url}`] !== "boolean")
    || !Array.isArray(access.grantGroups);
  const pageDsl = access.publicPage ? stripAccessDeclarations(entry.pageDsl) as PageDsl
    : applyActionAccess(entry.pageDsl, { allowed: permissions, grantGroups: groups }, incomplete, failureMessage);
  return Response.json(
    {
      id,
      resourceType: "page",
      version: entry.version,
      dslVersion: "1.0",
      representation: "localized",
      localization: {
        locale: i18n.locale,
        defaultLocale: i18n.defaultLocale,
        release: i18n.release,
      },
      schema: pageDsl,
      updatedAt: new Date().toISOString(),
    },
    { headers: { "cache-control": "no-store", ...corsHeaders(request) } },
  );
}

/** 预检（跨源带凭据 / 自定义头时） */
export const OPTIONS = corsPreflight;
