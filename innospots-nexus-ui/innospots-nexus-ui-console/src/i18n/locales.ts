export const DEFAULT_LOCALE = "zh-CN" as const;
export const LOCALE_COOKIE = "nexus.locale";
export const SUPPORTED_LOCALES = ["zh-CN", "en-US"] as const;

export type ConsoleLocale = (typeof SUPPORTED_LOCALES)[number];

/**
 * 将 BCP 47 语言标签及常见别名归一到 Console 支持的语言。
 */
export function normalizeLocale(value: string | null | undefined): ConsoleLocale | null {
  const normalized = value?.trim().replaceAll("_", "-").toLowerCase();
  if (!normalized) {
    return null;
  }
  if (normalized === "zh" || normalized.startsWith("zh-")) {
    return "zh-CN";
  }
  if (normalized === "en" || normalized.startsWith("en-")) {
    return "en-US";
  }
  return null;
}

/**
 * 按 q 权重解析 Accept-Language，跳过 q=0 与不支持的语言。
 */
export function resolveAcceptLanguage(value: string | null | undefined): ConsoleLocale | null {
  if (!value) {
    return null;
  }
  const candidates = value
    .split(",")
    .map((part, index) => {
      const [tag, ...parameters] = part.trim().split(";");
      const qualityParameter = parameters.find((parameter) => parameter.trim().startsWith("q="));
      const parsed = qualityParameter
        ? Number.parseFloat(qualityParameter.trim().slice(2))
        : 1;
      return {
        locale: normalizeLocale(tag),
        quality: Number.isFinite(parsed) ? parsed : 0,
        index,
      };
    })
    .filter((candidate) => candidate.locale !== null && candidate.quality > 0)
    .sort((left, right) => right.quality - left.quality || left.index - right.index);
  return candidates[0]?.locale ?? null;
}

/**
 * 解析服务端请求语言：显式 query > Cookie > Accept-Language > 应用默认值。
 */
export function resolveRequestLocale(input: {
  queryLocale?: string | null;
  cookieLocale?: string | null;
  acceptLanguage?: string | null;
}): ConsoleLocale {
  return normalizeLocale(input.queryLocale)
    ?? normalizeLocale(input.cookieLocale)
    ?? resolveAcceptLanguage(input.acceptLanguage)
    ?? DEFAULT_LOCALE;
}
