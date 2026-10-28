import { isI18nError, resolveRequestLocale } from "@pactor-app/i18n";

import {
  DEFAULT_LOCALE,
  LOCALE_CONFIG,
  LOCALE_COOKIE,
  type ConsoleLocale,
} from "@/pactor/locale";
import { loadMessageBundleSet } from "@/server/i18n";

export const dynamic = "force-dynamic";

/**
 * Next 对外语言包接口，返回 Java Console 端点的 simple-v1 数据体。
 */
export async function GET(request: Request) {
  const url = new URL(request.url);
  const cookieLocale = request.headers.get("cookie")
    ?.split(";")
    .map((part) => part.trim())
    .find((part) => part.startsWith(`${LOCALE_COOKIE}=`))
    ?.slice(LOCALE_COOKIE.length + 1);
  const values = url.searchParams.getAll("locale");
  let locale: ConsoleLocale;
  try {
    locale = resolveRequestLocale({
      queryLocale: values.length > 1 ? values : values[0],
      cookieLocale,
      acceptLanguage: request.headers.get("accept-language") ?? undefined,
    }, LOCALE_CONFIG).locale as ConsoleLocale;
  } catch (error) {
    if (isI18nError(error)) {
      return Response.json(
        { error: error.code, defaultLocale: DEFAULT_LOCALE },
        { status: 400 },
      );
    }
    throw error;
  }
  const namespaces = url.searchParams.get("namespaces")
    ?.split(",")
    .map((item) => item.trim())
    .filter(Boolean);
  try {
    return Response.json(await loadMessageBundleSet(locale, namespaces), {
      headers: { "cache-control": "no-store" },
    });
  } catch {
    return Response.json(
      { error: "Message bundle service is unavailable" },
      { status: 502, headers: { "cache-control": "no-store" } },
    );
  }
}
