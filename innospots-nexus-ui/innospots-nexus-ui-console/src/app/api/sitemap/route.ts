import { cookies } from "next/headers";

import { readPlatformAccessToken, resolveSessionUser } from "@/backend/auth/accounts";
import { corsPreflight, corsHeaders } from "@/backend/cors";
import { getRequestI18n } from "@/backend/pages/i18n";
import { buildSitemap } from "@/backend/pages/sitemap";
import { verifyPageAccess } from "@/backend/pages/page-access";
import type { SitemapResource } from "pactor-app";

/**
 * Sitemap 端点（design/dynamic-page.md）：
 * 会话感知——同一 URL，匿名与不同角色拿到不同 sitemap。
 * 入口可配置多个此类 URL（src/frontend/config.ts 的 sources）。
 */
export async function GET(request: Request) {
  const store = await cookies();
  const user = resolveSessionUser(store);
  const i18n = await getRequestI18n(request);
  const sitemap = buildSitemap(user, i18n);
  const accessToken = readPlatformAccessToken(store);
  const cookie = request.headers.get("cookie");
  const decisions = new Map<string, Promise<boolean>>();
  const allowed = (pageId: string): Promise<boolean> => {
    let decision = decisions.get(pageId);
    if (!decision) {
      decision = verifyPageAccess({ pageId, user, accessToken, cookie })
        .then((result) => result.allowed);
      decisions.set(pageId, decision);
    }
    return decision;
  };
  async function filterMenus(menus: SitemapResource["menus"]): Promise<SitemapResource["menus"]> {
    const entries = await Promise.all(menus.map(async (item) => {
      if (item.type === "group") {
        const children = await filterMenus(item.children ?? []);
        return children.length ? { ...item, children } : null;
      }
      return !item.pageId || await allowed(item.pageId) ? item : null;
    }));
    return entries.filter((item): item is NonNullable<typeof item> => item !== null);
  }
  sitemap.menus = await filterMenus(sitemap.menus);
  return Response.json(sitemap, {
    headers: { "cache-control": "no-store", ...corsHeaders(request) },
  });
}

/** 预检（跨源带凭据 / 自定义头时） */
export const OPTIONS = corsPreflight;
