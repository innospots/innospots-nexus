"use client";

import { useCallback } from "react";

import type { I18nContext, I18nParams } from "@pactor-app/core";
import { useI18n } from "@pactor-app/runtime";

import { uiMessageKey } from "./message-key";

const PLACEHOLDER_PATTERN = /\{\{\s*([A-Za-z0-9_.-]+)\s*\}\}/g;

export { uiMessageKey } from "./message-key";

function interpolate(source: string, params?: I18nParams): string {
  if (!params) {
    return source;
  }
  return source.replace(PLACEHOLDER_PATTERN, (match, name: string) => {
    const value = params[name];
    return value === undefined || value === null ? match : String(value);
  });
}

/**
 * 同步翻译代码内文案；词条缺失时保留中文源文本并完成参数插值。
 */
export function translateUi(
  i18n: Pick<I18nContext, "t">,
  source: string,
  params?: I18nParams,
): string {
  const key = uiMessageKey(source);
  const translated = i18n.t(key, params);
  return translated === key ? interpolate(source, params) : translated;
}

/**
 * React 组件使用的静态文案翻译函数。
 */
export function useUiText(): (source: string, params?: I18nParams) => string {
  const i18n = useI18n();
  return useCallback(
    (source: string, params?: I18nParams) => translateUi(i18n, source, params),
    [i18n],
  );
}
