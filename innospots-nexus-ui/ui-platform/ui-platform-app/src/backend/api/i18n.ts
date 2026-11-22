/**
 * 语言偏好写入（BFF）。
 *
 * 与独立开发时的 `/api/i18n/locale` 行为一致：校验 locale 后写 Cookie，
 * 调用方随后整页刷新以重建 sitemap 与页面。
 */
import { isI18nError, resolveRequestLocale } from "@pactor-app/i18n";

import { LOCALE_CONFIG, LOCALE_COOKIE } from "@/frontend/locale";

import { jsonResponse, serializeCookie, withCookies } from "./http";

export async function writeLocaleCookie(request: Request): Promise<Response> {
  const payload = (await request.json().catch(() => null)) as {
    locale?: unknown;
  } | null;
  const requested =
    typeof payload?.locale === "string" ? payload.locale : undefined;
  if (requested === undefined || requested.trim().length === 0) {
    return jsonResponse(request, { error: "Unsupported locale" }, { status: 400 });
  } else {
    let locale: string;
    try {
      locale = resolveRequestLocale(
        { queryLocale: requested },
        LOCALE_CONFIG,
      ).locale;
    } catch (error) {
      if (isI18nError(error)) {
        return jsonResponse(request, { error: error.code }, { status: 400 });
      }
      throw error;
    }
    return withCookies(
      new Response(null, {
        status: 204,
        headers: { "cache-control": "no-store" },
      }),
      [
        serializeCookie(LOCALE_COOKIE, locale, {
          path: "/",
          sameSite: "lax",
          maxAge: 60 * 60 * 24 * 365,
        }),
      ],
    );
  }
}
