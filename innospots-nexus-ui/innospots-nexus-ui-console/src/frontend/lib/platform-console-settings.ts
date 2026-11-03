/**
 * 平台控制台界面设置 API（/api/d/platform/settings/*）。
 */

import { resolveSkin, selectSkin, type SkinValue } from "../skin";

export type PlatformNavigationLayoutMode = "RAIL_DUAL" | "SINGLE_GROUP";

export type PlatformConsoleTheme =
  | "ZHU_CAO"
  | "JIN_YUN"
  | "QIU_XIANG"
  | "BI_SHAN"
  | "TIAN_SHUI"
  | "QING_MING"
  | "NING_YE"
  | "LIAN_HONG"
  | "LI_KE"
  | "WAN_YAN"
  | "YUE_PO"
  | "JIN_YU"
  | "CHENWUNAIFU"
  | "TAIJINGSHENLIN"
  | "XUANZHIMOYUN";

const DEPRECATED_PLATFORM_THEMES = new Set<PlatformConsoleTheme>([
  "CHENWUNAIFU",
  "TAIJINGSHENLIN",
  "XUANZHIMOYUN",
]);

type NexusEnvelope<T> = {
  success?: boolean;
  code?: string | number;
  data?: T;
  message?: string;
};

function unwrap<T>(body: NexusEnvelope<T>): T {
  const code = body.code;
  if (body.success === false) {
    throw new Error(body.message ?? "请求失败");
  }
  if (code !== undefined && code !== 0 && code !== "0" && code !== "OK") {
    throw new Error(body.message ?? `业务错误码 ${String(code)}`);
  }
  if (body.data === undefined) {
    throw new Error(body.message ?? "响应缺少 data");
  }
  return body.data;
}

export async function fetchNavigationLayoutMode(): Promise<PlatformNavigationLayoutMode> {
  const response = await fetch("/api/d/platform/settings/navigation-layout", {
    cache: "no-store",
  });
  if (!response.ok) {
    return "RAIL_DUAL";
  }
  const payload = unwrap<{ navigationLayoutMode?: PlatformNavigationLayoutMode }>(
    (await response.json()) as NexusEnvelope<{ navigationLayoutMode?: PlatformNavigationLayoutMode }>,
  );
  return payload.navigationLayoutMode === "SINGLE_GROUP" ? "SINGLE_GROUP" : "RAIL_DUAL";
}

export async function fetchConsoleTheme(): Promise<{ theme: PlatformConsoleTheme; skinKey: string } | null> {
  const response = await fetch("/api/d/platform/settings/console-theme", {
    cache: "no-store",
  });
  if (!response.ok) {
    return null;
  }
  return unwrap<{ theme: PlatformConsoleTheme; skinKey: string }>(
    (await response.json()) as NexusEnvelope<{ theme: PlatformConsoleTheme; skinKey: string }>,
  );
}

export async function saveConsoleTheme(theme: PlatformConsoleTheme): Promise<void> {
  const response = await fetch("/api/d/platform/settings/console-theme", {
    method: "PUT",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ theme }),
  });
  const body = (await response.json()) as NexusEnvelope<unknown>;
  if (!response.ok) {
    throw new Error(body.message ?? "保存主题失败");
  }
  unwrap(body);
  notifyConsoleSettingsChange();
}

/** 将平台返回的主题应用到当前会话（localStorage + CSS link）。 */
export function applyConsoleThemeFromRemote(payload: {
  theme?: PlatformConsoleTheme | string;
  skinKey?: string;
}): void {
  if (payload.skinKey && payload.skinKey.trim().length > 0) {
    selectSkin(resolveSkin(payload.skinKey));
    return;
  }
  if (payload.theme && typeof payload.theme === "string") {
    const theme = payload.theme as PlatformConsoleTheme;
    const skin = THEME_TO_SKIN[theme] as SkinValue;
    if (skin) {
      selectSkin(skin);
    }
  }
}

export async function saveNavigationLayoutMode(
  navigationLayoutMode: PlatformNavigationLayoutMode,
): Promise<void> {
  const response = await fetch("/api/d/platform/settings/navigation-layout", {
    method: "PUT",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ navigationLayoutMode }),
  });
  if (!response.ok) {
    throw new Error("保存导航布局失败");
  }
  unwrap(await response.json());
}

export async function saveConsoleTimezone(timezone: string): Promise<void> {
  const response = await fetch("/api/d/platform/settings/console-timezone", {
    method: "PUT",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ timezone: timezone.trim() }),
  });
  if (!response.ok) {
    throw new Error("保存时区失败");
  }
  unwrap(await response.json());
  notifyConsoleSettingsChange();
}

export const DEFAULT_CONSOLE_SITE_NAME = "Nexus Console";

export const CONSOLE_SETTINGS_CHANGE_EVENT = "pactor-console-settings-change";

export async function fetchConsoleSiteName(): Promise<string> {
  const response = await fetch("/api/d/platform/settings/console-general", {
    cache: "no-store",
  });
  if (!response.ok) {
    return DEFAULT_CONSOLE_SITE_NAME;
  }
  const payload = unwrap<{ siteName?: string }>(
    (await response.json()) as NexusEnvelope<{ siteName?: string }>,
  );
  const name = payload.siteName?.trim();
  return name && name.length > 0 ? name : DEFAULT_CONSOLE_SITE_NAME;
}

/** 保存系统名称等控制台设置后通知 NavigationShell 等壳层刷新。 */
export function notifyConsoleSettingsChange(detail?: { siteName?: string }): void {
  if (typeof window === "undefined") {
    return;
  }
  if (detail?.siteName) {
    window.dispatchEvent(
      new CustomEvent("nexus-console-site-name-sync", {
        detail: detail.siteName.trim(),
      }),
    );
  }
  window.dispatchEvent(new Event(CONSOLE_SETTINGS_CHANGE_EVENT));
}

export const THEME_TO_SKIN: Record<PlatformConsoleTheme, string> = {
  ZHU_CAO: "zhu-cao",
  JIN_YUN: "jin-yun",
  QIU_XIANG: "qiu-xiang",
  BI_SHAN: "bi-shan",
  TIAN_SHUI: "tian-shui",
  QING_MING: "qing-ming",
  NING_YE: "ning-ye",
  LIAN_HONG: "lian-hong",
  LI_KE: "li-ke",
  WAN_YAN: "wan-yan",
  YUE_PO: "yue-po",
  JIN_YU: "jin-yu",
  CHENWUNAIFU: "qing-ming",
  TAIJINGSHENLIN: "bi-shan",
  XUANZHIMOYUN: "jin-yu",
};

export function skinToPlatformTheme(skin: string): PlatformConsoleTheme | null {
  const normalized = skin.trim().toLowerCase();
  const matches = Object.entries(THEME_TO_SKIN).filter(([, value]) => value === normalized);
  if (matches.length === 0) {
    return null;
  }
  const canonical = matches.find(([key]) => !DEPRECATED_PLATFORM_THEMES.has(key as PlatformConsoleTheme));
  const chosen = canonical ?? matches[0];
  return chosen[0] as PlatformConsoleTheme;
}
