import { DEFAULT_LOCALE, normalizeLocale, resolveRequestLocale } from "@/i18n/locales";
import { loadMessageBundleSet } from "@/server/i18n";

export const dynamic = "force-dynamic";

/**
 * Next 对外语言包接口，返回 Java Console 端点的 simple-v1 数据体。
 */
export async function GET(request: Request) {
  const url = new URL(request.url);
  const locale = resolveRequestLocale({
    queryLocale: url.searchParams.get("locale"),
    cookieLocale: request.headers.get("cookie")
      ?.split(";")
      .map((part) => part.trim())
      .find((part) => part.startsWith("nexus.locale="))
      ?.slice("nexus.locale=".length),
    acceptLanguage: request.headers.get("accept-language"),
  });
  if (normalizeLocale(url.searchParams.get("locale")) === null && url.searchParams.has("locale")) {
    return Response.json(
      { error: "Unsupported locale", defaultLocale: DEFAULT_LOCALE },
      { status: 400 },
    );
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
