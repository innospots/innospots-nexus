import { cookies } from "next/headers";

import { LOCALE_COOKIE, normalizeLocale } from "@/i18n/locales";

/**
 * 写入 Console 语言偏好；调用方随后整页刷新以重建 sitemap 与页面。
 */
export async function POST(request: Request) {
  const payload = await request.json().catch(() => null) as { locale?: unknown } | null;
  const locale = normalizeLocale(
    typeof payload?.locale === "string" ? payload.locale : null,
  );
  if (!locale) {
    return Response.json({ error: "Unsupported locale" }, { status: 400 });
  }
  const cookieStore = await cookies();
  cookieStore.set(LOCALE_COOKIE, locale, {
    path: "/",
    sameSite: "lax",
    maxAge: 60 * 60 * 24 * 365,
  });
  return new Response(null, {
    status: 204,
    headers: { "cache-control": "no-store" },
  });
}
