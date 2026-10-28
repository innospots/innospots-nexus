import "server-only";

import { cookies, headers } from "next/headers";
import {
  ApiMessageProvider,
  isI18nError,
  resolveRequestLocale,
  type LanguageBundles,
} from "@pactor-app/i18n";
import type { MessageBundleSet } from "@pactor-app/core";

import {
  DEFAULT_LOCALE,
  LOCALE_CONFIG,
  LOCALE_COOKIE,
  type ConsoleLocale,
} from "@/pactor/locale";
import { serverBaseUrl } from "@/pactor/config";

const APP_ID = "console";
const RELEASE = process.env.NEXT_PUBLIC_CONSOLE_RELEASE ?? "console-ui";

export interface RequestI18n {
  locale: ConsoleLocale;
  defaultLocale: typeof DEFAULT_LOCALE;
  release: string;
  messages: Record<string, string>;
  bundleSet: MessageBundleSet;
  bundles: MessageBundleSet["bundles"];
  languageBundles: LanguageBundles;
  fallback: boolean;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

/**
 * 后端语言包为 R<T> envelope；Provider 只消费 Pactor simple-v1 data。
 */
const envelopeFetch = async (
  url: string,
  init?: { headers?: Record<string, string> },
) => {
  const response = await fetch(url, {
    headers: init?.headers,
    cache: "no-store",
    signal: AbortSignal.timeout(3000),
  });
  const text = await response.text();
  let body: unknown = text;
  try {
    body = JSON.parse(text);
  } catch {
    // 非 JSON 原样交给 ApiMessageProvider 做协议校验。
  }
  const data = isRecord(body) && "data" in body ? body.data : body;
  return {
    status: response.status,
    headers: response.headers,
    text: async () => JSON.stringify(data),
  };
};

const provider = new ApiMessageProvider({
  endpoint: `${serverBaseUrl}/api/public/i18n/bundles`,
  fetch: envelopeFetch,
  headers: { accept: "application/json" },
});

async function fetchBundleSet(
  locale: ConsoleLocale,
  namespaces: string[] = [],
): Promise<MessageBundleSet> {
  return provider.load({
    appId: APP_ID,
    locale,
    namespaces,
    release: RELEASE,
  });
}

export function flattenMessages(bundleSet: MessageBundleSet): Record<string, string> {
  const messages: Record<string, string> = {};
  for (const bundle of Object.values(bundleSet.bundles)) {
    Object.assign(messages, bundle.messages);
  }
  return messages;
}

export async function loadMessageBundleSet(
  locale: ConsoleLocale,
  namespaces?: string[],
): Promise<MessageBundleSet> {
  return fetchBundleSet(locale, namespaces ?? []);
}

/**
 * 从当前 Next 请求解析语言并加载语言包；后端不可用时回退为空词条，
 * 页面继续使用 YAML 中的中文源文本。
 */
export async function getRequestI18n(request?: Request): Promise<RequestI18n> {
  const cookieStore = await cookies();
  const headerStore = await headers();
  const requestUrl = request ? new URL(request.url) : null;
  const queryLocales = requestUrl?.searchParams.getAll("locale") ?? [];
  const resolved = resolveRequestLocale({
    queryLocale: queryLocales.length > 1 ? queryLocales : queryLocales[0],
    cookieLocale: cookieStore.get(LOCALE_COOKIE)?.value,
    acceptLanguage: request?.headers.get("accept-language")
      ?? headerStore.get("accept-language")
      ?? undefined,
  }, LOCALE_CONFIG);
  const locale = resolved.locale as ConsoleLocale;
  try {
    const bundleSet = await fetchBundleSet(locale);
    const fallbacks = locale === DEFAULT_LOCALE
      ? []
      : [await fetchBundleSet(DEFAULT_LOCALE)];
    return {
      locale,
      defaultLocale: DEFAULT_LOCALE,
      release: bundleSet.release,
      messages: flattenMessages(bundleSet),
      bundleSet,
      bundles: bundleSet.bundles,
      languageBundles: { current: bundleSet, fallbacks },
      fallback: false,
    };
  } catch (error) {
    if (
      isI18nError(error)
      && error.code !== "MESSAGE_FETCH_FAILED"
      && error.code !== "MESSAGE_PROTOCOL_ERROR"
    ) {
      throw error;
    }
    const bundleSet: MessageBundleSet = {
      appId: APP_ID,
      locale,
      release: RELEASE,
      format: "simple-v1",
      bundles: {},
    };
    return {
      locale,
      defaultLocale: DEFAULT_LOCALE,
      release: RELEASE,
      messages: {},
      bundleSet,
      bundles: {},
      languageBundles: { current: bundleSet, fallbacks: [] },
      fallback: true,
    };
  }
}
