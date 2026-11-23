import { cookies } from "next/headers";

import { resolveSessionUser } from "@/backend/auth/accounts";
import { corsPreflight } from "@/backend/cors";
import { findPageEntry, getLazyPage } from "@/backend/pages/sitemap";

/**
 * 懒加载页面端点：sitemap 中不内联 pageDsl，按需获取。
 * 匿名访问非 public 页面返回 401，由前端跳转登录页。
 */
export async function GET(
  _request: Request,
  context: { params: Promise<{ id: string }> },
) {
  const { id } = await context.params;
  const store = await cookies();
  const user = resolveSessionUser(store);
  const entry = findPageEntry(id);
  if (!entry) {
    return Response.json({ error: "not_found" }, { status: 404 });
  }
  const page = getLazyPage(id, user);
  if (!page) {
    return Response.json({ error: "unauthorized" }, { status: 401 });
  }
  return Response.json(
    {
      id,
      resourceType: "page",
      version: page.version,
      dslVersion: "1.0",
      schema: page.pageDsl,
      updatedAt: new Date().toISOString(),
    },
    { headers: { "cache-control": "no-store" } },
  );
}

export const OPTIONS = corsPreflight;
