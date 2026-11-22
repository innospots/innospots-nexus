import { cookies } from "next/headers";
import { NextResponse } from "next/server";
import type { SitemapResource } from "pactor-app";

import { ACCESS_TOKEN_COOKIE, PLATFORM_LOGIN_COOKIE, readPlatformAccessToken, resolveSessionUser } from "@/backend/auth/accounts";
import { serverBaseUrl } from "@/backend/config";
import { corsPreflight, corsHeaders } from "@/backend/cors";
import { getRequestI18n } from "@/backend/pages/i18n";
import { buildSitemap, findPageEntryById } from "@/backend/pages/sitemap";

type NavigationMenu = {
  menuKey: string;
  menuName: string;
  pageKey?: string | null;
  routePath?: string | null;
  externalUrl?: string | null;
  icon?: string | null;
  openMode?: string | null;
  children?: NavigationMenu[];
};

function convertNavigation(items: NavigationMenu[]): SitemapResource["menus"] {
  return items.flatMap<SitemapResource["menus"][number]>((item) => {
    const children = convertNavigation(item.children ?? []);
    const base = {
      id: item.menuKey,
      title: item.menuName,
      ...(item.icon ? { icon: item.icon } : {}),
    };
    if (children.length) return [{ ...base, type: "group" as const, children }];
    if (item.pageKey && findPageEntryById(item.pageKey)) {
      return [{ ...base, type: "page" as const, pageId: item.pageKey, layout: "admin", ...(item.routePath ? { path: item.routePath } : {}) }];
    }
    if (item.externalUrl) {
      return [{ ...base, type: "link" as const, path: item.externalUrl, external: true, ...(item.openMode === "NEW_WINDOW" ? { target: "_blank" } : {}) }];
    }
    return [];
  });
}

/** 后端按当前主体裁剪导航，Next.js 负责合并页面路由及布局。 */
export async function GET(request: Request) {
  const store = await cookies();
  const user = resolveSessionUser(store);
  const i18n = await getRequestI18n(request);
  let menus: SitemapResource["menus"] = [];
  if (user) {
    try {
      const accessToken = readPlatformAccessToken(store);
      const response = await fetch(`${serverBaseUrl}/api/d/platform/navigation/menus`, {
        headers: { authorization: `Bearer ${accessToken}` },
        cache: "no-store",
      });
      if (response.status === 401) {
        const result = NextResponse.json(buildSitemap(null, i18n, []), {
          headers: { "cache-control": "no-store", ...corsHeaders(request) },
        });
        const expired = { httpOnly: true, sameSite: "lax" as const, path: "/", maxAge: 0 };
        result.cookies.set(ACCESS_TOKEN_COOKIE, "", expired);
        result.cookies.set(PLATFORM_LOGIN_COOKIE, "", expired);
        return result;
      }
      const envelope = await response.json();
      if (!response.ok || envelope.success === false || !Array.isArray(envelope.data)) {
        throw new Error("后端导航接口返回异常");
      }
      menus = convertNavigation(envelope.data as NavigationMenu[]);
    } catch {
      return Response.json({ error: "无法加载后端导航菜单" }, {
        status: 502,
        headers: { "cache-control": "no-store", ...corsHeaders(request) },
      });
    }
  }
  return Response.json(buildSitemap(user, i18n, menus), {
    headers: { "cache-control": "no-store", ...corsHeaders(request) },
  });
}

export const OPTIONS = corsPreflight;
