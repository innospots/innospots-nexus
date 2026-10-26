import "server-only";

import { cookies, headers } from "next/headers";

import {
  DEFAULT_LOCALE,
  LOCALE_COOKIE,
  type ConsoleLocale,
  normalizeLocale,
  resolveRequestLocale,
} from "@/i18n/locales";
import { serverBaseUrl } from "@/pactor/config";

const APP_ID = "console";
const FORMAT = "simple-v1";
const RELEASE = process.env.NEXT_PUBLIC_CONSOLE_RELEASE ?? "console-ui";
const CACHE_TTL_MS = 5 * 60 * 1000;
const FETCH_TIMEOUT_MS = 3000;

export interface MessageBundle {
  version: string;
  messages: Record<string, string>;
}

export interface MessageBundleSet {
  appId: string;
  locale: ConsoleLocale;
  release: string;
  format: "simple-v1";
  bundles: Record<string, MessageBundle>;
}

export interface RequestI18n {
  locale: ConsoleLocale;
  defaultLocale: typeof DEFAULT_LOCALE;
  release: string;
  messages: Record<string, string>;
  bundleSet: MessageBundleSet;
  fallback: boolean;
}

interface CacheEntry {
  expiresAt: number;
  etag?: string;
  value: MessageBundleSet;
}

const globalForI18n = globalThis as unknown as {
  __nexusI18nBundleCache?: Map<string, CacheEntry>;
};
const bundleCache = globalForI18n.__nexusI18nBundleCache
  ?? new Map<string, CacheEntry>();
globalForI18n.__nexusI18nBundleCache = bundleCache;

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function parseBundleSet(value: unknown, locale: ConsoleLocale): MessageBundleSet | null {
  if (!isRecord(value) || value.format !== FORMAT) {
    return null;
  }
  if (
    value.appId !== APP_ID
    || normalizeLocale(typeof value.locale === "string" ? value.locale : null) !== locale
    || typeof value.release !== "string"
    || !isRecord(value.bundles)
  ) {
    return null;
  }
  const bundles: Record<string, MessageBundle> = {};
  for (const [namespace, rawBundle] of Object.entries(value.bundles)) {
    if (!isRecord(rawBundle) || typeof rawBundle.version !== "string" || !isRecord(rawBundle.messages)) {
      return null;
    }
    const messages: Record<string, string> = {};
    for (const [key, message] of Object.entries(rawBundle.messages)) {
      if (typeof message !== "string" || !key.startsWith(`${namespace}.`)) {
        return null;
      }
      messages[key] = message;
    }
    bundles[namespace] = { version: rawBundle.version, messages };
  }
  return {
    appId: APP_ID,
    locale,
    release: value.release,
    format: FORMAT,
    bundles,
  };
}

function unwrapEnvelope(value: unknown): unknown {
  return isRecord(value) && "data" in value ? value.data : value;
}

function emptyBundleSet(locale: ConsoleLocale): MessageBundleSet {
  return {
    appId: APP_ID,
    locale,
    release: RELEASE,
    format: FORMAT,
    bundles: {},
  };
}

async function fetchBundleSet(
  locale: ConsoleLocale,
  namespaces?: string[],
): Promise<MessageBundleSet> {
  const cacheKey = `${locale}|${namespaces?.join(",") ?? "*"}`;
  const cached = bundleCache.get(cacheKey);
  if (cached && cached.expiresAt > Date.now()) {
    return cached.value;
  }

  const url = new URL("/api/public/i18n/bundles", serverBaseUrl);
  url.searchParams.set("appId", APP_ID);
  url.searchParams.set("locale", locale);
  url.searchParams.set("release", RELEASE);
  if (namespaces?.length) {
    url.searchParams.set("namespaces", namespaces.join(","));
  }
  const requestHeaders = new Headers({ "accept-language": locale });
  if (cached?.etag) {
    requestHeaders.set("if-none-match", cached.etag);
  }
  const response = await fetch(url, {
    headers: requestHeaders,
    cache: "no-store",
    signal: AbortSignal.timeout(FETCH_TIMEOUT_MS),
  });
  if (response.status === 304 && cached) {
    cached.expiresAt = Date.now() + CACHE_TTL_MS;
    return cached.value;
  }
  if (!response.ok) {
    throw new Error(`Message bundle endpoint returned HTTP ${response.status}`);
  }
  const parsed = parseBundleSet(unwrapEnvelope(await response.json()), locale);
  if (!parsed) {
    throw new Error("Message bundle endpoint returned an invalid simple-v1 payload");
  }
  bundleCache.set(cacheKey, {
    value: parsed,
    expiresAt: Date.now() + CACHE_TTL_MS,
    etag: response.headers.get("etag") ?? undefined,
  });
  return parsed;
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
  return fetchBundleSet(locale, namespaces);
}

/**
 * 从当前 Next 请求解析语言并加载语言包；后端不可用时回退为空词条，
 * 页面继续使用 YAML 中的中文源文本。
 */
export async function getRequestI18n(request?: Request): Promise<RequestI18n> {
  const cookieStore = await cookies();
  const headerStore = await headers();
  const requestUrl = request ? new URL(request.url) : null;
  const locale = resolveRequestLocale({
    queryLocale: requestUrl?.searchParams.get("locale"),
    cookieLocale: cookieStore.get(LOCALE_COOKIE)?.value,
    acceptLanguage: request?.headers.get("accept-language")
      ?? headerStore.get("accept-language"),
  });
  try {
    const bundleSet = await fetchBundleSet(locale);
    return {
      locale,
      defaultLocale: DEFAULT_LOCALE,
      release: bundleSet.release,
      messages: flattenMessages(bundleSet),
      bundleSet,
      fallback: false,
    };
  } catch {
    const bundleSet = emptyBundleSet(locale);
    return {
      locale,
      defaultLocale: DEFAULT_LOCALE,
      release: RELEASE,
      messages: {},
      bundleSet,
      fallback: true,
    };
  }
}
