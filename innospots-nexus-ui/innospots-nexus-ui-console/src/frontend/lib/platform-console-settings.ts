/**
 * 平台控制台界面设置 API（/api/d/platform/settings/*）。
 */

export type PlatformNavigationLayoutMode = "RAIL_DUAL" | "SINGLE_GROUP";

export type PlatformConsoleTheme =
  | "CHENWUNAIFU"
  | "TAIJINGSHENLIN"
  | "XUANZHIMOYUN";

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
  if (!response.ok) {
    throw new Error("保存主题失败");
  }
  unwrap(await response.json());
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

export const THEME_TO_SKIN: Record<PlatformConsoleTheme, string> = {
  CHENWUNAIFU: "chenwunaifu",
  TAIJINGSHENLIN: "taijingshenlin",
  XUANZHIMOYUN: "xuanzhimoyun",
};

export function skinToPlatformTheme(skin: string): PlatformConsoleTheme | null {
  const entry = Object.entries(THEME_TO_SKIN).find(([, value]) => value === skin);
  return entry ? (entry[0] as PlatformConsoleTheme) : null;
}
