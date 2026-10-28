import { cookies } from "next/headers";
import { isI18nError, resolveRequestLocale } from "@pactor-app/i18n";

import { LOCALE_CONFIG, LOCALE_COOKIE } from "@/pactor/locale";

/**
 * 写入 Console 语言偏好；调用方随后整页刷新以重建 sitemap 与页面。
 */
export async function POST(request: Request) {
  const payload = await request.json().catch(() => null) as { locale?: unknown } | null;
  const requested = typeof payload?.locale === "string" ? payload.locale : undefined;
  let locale: string;
  try {
    locale = resolveRequestLocale(
      { queryLocale: requested },
      LOCALE_CONFIG,
    ).locale;
  } catch (error) {
    if (isI18nError(error)) {
      return Response.json({ error: error.code }, { status: 400 });
    }
    throw error;
  }
  if (requested === undefined || requested.trim().length === 0) {
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
