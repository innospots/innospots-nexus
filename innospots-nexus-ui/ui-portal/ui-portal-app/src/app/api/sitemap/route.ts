import { cookies } from "next/headers";

import { resolveSessionUser } from "@/backend/auth/accounts";
import { corsPreflight } from "@/backend/cors";
import { buildSitemap } from "@/backend/pages/sitemap";

/** 当前会话的 sitemap（页面清单 + 导航）。 */
export async function GET() {
  const store = await cookies();
  const user = resolveSessionUser(store);
  return Response.json(buildSitemap(user), {
    headers: { "cache-control": "no-store" },
  });
}

export const OPTIONS = corsPreflight;
