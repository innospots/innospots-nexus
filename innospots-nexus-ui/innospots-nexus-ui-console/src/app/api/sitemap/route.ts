import { cookies } from "next/headers";

import { resolveSessionUser } from "@/backend/auth/accounts";
import { corsPreflight, corsHeaders } from "@/backend/cors";
import { getRequestI18n } from "@/backend/pages/i18n";
import { buildSitemap } from "@/backend/pages/sitemap";

/**
 * Sitemap 端点（design/dynamic-page.md）：
 * 会话感知——同一 URL，匿名与不同角色拿到不同 sitemap。
 * 入口可配置多个此类 URL（src/frontend/config.ts 的 sources）。
 */
export async function GET(request: Request) {
  const store = await cookies();
  const user = resolveSessionUser(store);
  const i18n = await getRequestI18n(request);
  return Response.json(buildSitemap(user, i18n), {
    headers: { "cache-control": "no-store", ...corsHeaders(request) },
  });
}

/** 预检（跨源带凭据 / 自定义头时） */
export const OPTIONS = corsPreflight;
