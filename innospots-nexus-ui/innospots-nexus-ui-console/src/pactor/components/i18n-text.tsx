"use client";

type I18nValue = string | Record<string, string> | null | undefined;

const PREFERRED_LOCALES = ["zh-CN", "zh", "en", "en-US"];

/**
 * 从 API 返回的 I18nObject（或历史纯字符串）解析当前展示文案。
 */
export function resolveI18nText(value: I18nValue, preferredLocales: string[] = PREFERRED_LOCALES): string {
  if (value == null) {
    return "";
  }
  if (typeof value === "string") {
    return value;
  }
  for (const locale of preferredLocales) {
    const candidate = value[locale];
    if (candidate != null && candidate.trim().length > 0) {
      return candidate.trim();
    }
  }
  for (const candidate of Object.values(value)) {
    if (candidate != null && candidate.trim().length > 0) {
      return candidate.trim();
    }
  }
  return "";
}

export interface I18nTextProps {
  value?: I18nValue;
}

/** 表格/链接等只读场景下的国际化文案。 */
export function I18nText({ value }: I18nTextProps) {
  return <>{resolveI18nText(value)}</>;
}
