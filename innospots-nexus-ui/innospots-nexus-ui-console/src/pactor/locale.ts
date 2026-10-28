import type { LocaleResolverConfig } from "@pactor-app/i18n";

export const DEFAULT_LOCALE = "zh-CN" as const;
export const LOCALE_COOKIE = "locale";
export const SUPPORTED_LOCALES = ["zh-CN", "en-US"] as const;

export type ConsoleLocale = (typeof SUPPORTED_LOCALES)[number];

/**
 * Console 语言配置；语言协商由 Pactor 的 resolveRequestLocale 统一执行。
 */
export const LOCALE_CONFIG: LocaleResolverConfig = {
  supportedLocales: [...SUPPORTED_LOCALES],
  defaultLocale: DEFAULT_LOCALE,
  aliases: {
    zh: "zh-CN",
    en: "en-US",
  },
};
