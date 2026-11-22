type AnyRecord = Record<string, unknown>;

type MenuNode = AnyRecord & { children?: MenuNode[] };

function isRecord(value: unknown): value is AnyRecord {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

/** 后端导航返回允许的 pageKey 集合；接口不可用时返回 null（不过滤） */
async function allowedPageKeys(init?: RequestInit): Promise<Set<string> | null> {
  try {
    const response = await fetch("/api/d/platform/navigation/menus", {
      cache: "no-store",
      credentials: "include",
      headers: init?.headers,
    });
    if (!response.ok) return null;
    const payload = (await response.json().catch(() => null)) as
      | { data?: unknown }
      | null;
    if (!Array.isArray(payload?.data)) return null;
    const keys = new Set<string>();
    const walk = (items: unknown[]) => {
      for (const item of items) {
        if (!isRecord(item)) continue;
        if (typeof item.pageKey === "string" && item.pageKey.length > 0) {
          keys.add(item.pageKey);
        }
        if (Array.isArray(item.children)) walk(item.children);
      }
    };
    walk(payload.data);
    return keys;
  } catch {
    return null;
  }
}

/**
 * 按后端导航裁剪菜单：
 * - hidden 菜单（登录 / 详情 / 创建页）原样保留，保证路由仍可达；
 * - console:<pageId> 菜单仅保留后端导航包含的 pageKey；
 * - 分组在过滤后无可见子项时移除。
 */
function filterMenus(
  menus: unknown,
  allowed: Set<string>,
  parentHidden = false,
): MenuNode[] {
  if (!Array.isArray(menus)) return [];
  const result: MenuNode[] = [];
  for (const raw of menus) {
    if (!isRecord(raw)) continue;
    const menu = raw as MenuNode;
    const hidden = parentHidden || menu.hidden === true;
    if (Array.isArray(menu.children)) {
      const children = filterMenus(menu.children, allowed, hidden);
      if (!hidden && children.length === 0) continue;
      result.push({ ...menu, children });
      continue;
    }
    if (hidden) {
      result.push(menu);
      continue;
    }
    const pageId = typeof menu.pageId === "string" ? menu.pageId : "";
    if (pageId === "shell:themes") {
      if (allowed.has("themes")) result.push(menu);
      continue;
    }
    if (pageId.startsWith("console:")) {
      if (allowed.has(pageId.slice("console:".length))) result.push(menu);
      continue;
    }
    result.push(menu);
  }
  return result;
}

/**
 * Sitemap 源 fetcher：拉取 bootstrap 后，用后端 `/navigation/menus`
 * （已按当前主体的权限裁剪）过滤 Shell 静态菜单，保持菜单可见性与后端一致。
 */
export function createShellFetcher(): typeof fetch {
  return async (input, init) => {
    const response = await fetch(input, init);
    const url = typeof input === "string" ? input : input instanceof URL ? input.href : input.url;
    if (url.includes("/api/runtime/pages/") && response.status === 401) {
      // 令牌过期 / 失效：页面端点已判定需要重新登录，直接回登录页
      if (typeof window !== "undefined") {
        window.location.assign("/console/login");
      }
      return response;
    }
    if (!url.includes("/api/runtime/bootstrap") || !response.ok) {
      return response;
    }
    const sitemap = (await response.json()) as AnyRecord;
    const allowed = await allowedPageKeys(init);
    if (allowed === null) return Response.json(sitemap);
    return Response.json({ ...sitemap, menus: filterMenus(sitemap.menus, allowed) });
  };
}
