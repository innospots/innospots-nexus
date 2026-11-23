import { readFileSync, readdirSync, watch } from "node:fs";
import { join, relative } from "node:path";

import type { PageDsl } from "@pactor-app/core";
import { parseDocument } from "yaml";

import type { LayoutDsl, SitemapResource } from "pactor-app";

import type { PortalSessionUser } from "@/backend/auth/accounts";

/** 页面清单与布局来自本地 YAML；导航由本文件按清单生成。 */

const YAML_DIR = join(process.cwd(), "src/backend/pages/yaml");

interface PageEntry {
  id: string;
  path: string;
  title: string;
  version: number;
  public?: boolean;
  lazy?: boolean;
  layout?: string;
  activeMenu?: string;
}

interface SitemapFile {
  id: string;
  resourceType: "sitemap";
  version: number;
  dslVersion: string;
  app: { name: string };
  layout: string;
  auth: { loginPath: string };
  layouts: Record<string, LayoutDsl>;
  pages: PageEntry[];
}

interface PageFile {
  path: string;
}

/** 解析 YAML；语法错误时 fail-fast，避免结构静默残缺。 */
function loadYaml<T>(file: string): T {
  const doc = parseDocument(readFileSync(join(YAML_DIR, file), "utf8"));
  if (doc.errors.length > 0) {
    throw new Error(
      `${file} 解析失败：${doc.errors.map((e) => e.message.split("\n")[0]).join("；")}`,
    );
  }
  return doc.toJS() as T;
}

/** 递归收集 yaml 下所有页面 DSL（跳过 sitemap.yaml），按 id 建索引。 */
function collectPageFiles(dir: string): Map<string, PageFile> {
  const files = new Map<string, PageFile>();
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    const entryPath = join(dir, entry.name);
    if (entry.isDirectory()) {
      for (const [id, file] of collectPageFiles(entryPath)) {
        if (files.has(id)) {
          throw new Error(`页面 DSL id 重复：${id}`);
        }
        files.set(id, file);
      }
      continue;
    }
    if (!entry.name.endsWith(".yaml") || entry.name === "sitemap.yaml") {
      continue;
    }
    const id = entry.name.slice(0, -".yaml".length);
    files.set(id, { path: relative(YAML_DIR, entryPath) });
  }
  return files;
}

interface YamlState {
  site: SitemapFile;
  pages: Map<string, PageDsl>;
}

function loadState(): YamlState {
  const site = loadYaml<SitemapFile>("sitemap.yaml");
  const pageFiles = collectPageFiles(YAML_DIR);
  const pages = new Map<string, PageDsl>();
  for (const entry of site.pages) {
    const file = pageFiles.get(entry.id);
    if (!file) {
      throw new Error(`页面 DSL 缺失：${entry.id}.yaml`);
    }
    const pageDsl = loadYaml<PageDsl>(file.path);
    const page = pageDsl.page as unknown as Record<string, unknown>;
    const mode = page["access-mode"];
    if (page.id !== entry.id || !["public", "authenticated"].includes(String(mode))) {
      throw new Error(`页面 ${entry.id} 的 id 或 access-mode 无效`);
    }
    if (!entry.lazy) {
      throw new Error(`页面 ${entry.id} 必须通过懒加载端点访问`);
    }
    entry.public = mode === "public";
    pages.set(entry.id, pageDsl);
  }
  return { site, pages };
}

let stateCache: YamlState | null = null;

function state(): YamlState {
  if (!stateCache) {
    stateCache = loadState();
  }
  return stateCache;
}

// dev 下 yaml 变更后失效缓存，无需重启
const globalForYamlWatch = globalThis as unknown as {
  __portalYamlInvalidate?: () => void;
  __portalYamlWatcher?: ReturnType<typeof watch>;
};
globalForYamlWatch.__portalYamlInvalidate = () => {
  stateCache = null;
};
if (
  process.env.NODE_ENV !== "production" &&
  !globalForYamlWatch.__portalYamlWatcher
) {
  globalForYamlWatch.__portalYamlWatcher = watch(
    YAML_DIR,
    { recursive: true },
    () => globalForYamlWatch.__portalYamlInvalidate?.(),
  );
}

type Menu = SitemapResource["menus"][number];

/** 可见导航 + 隐藏路由项：登录/注册等页面不在导航出现，但仍需进入路由表。 */
function buildMenus(site: SitemapFile, user: PortalSessionUser | null): Menu[] {
  const settingsChildren: Menu[] = [
    {
      id: "nav-settings-profile",
      type: "page",
      pageId: "settings-profile",
      path: "/settings/profile",
      title: "个人设置",
      icon: "user-round",
    },
    {
      id: "nav-settings-appearance",
      type: "page",
      pageId: "settings-appearance",
      path: "/settings/appearance",
      title: "界面设置",
      icon: "palette",
    },
    {
      id: "nav-settings-ai",
      type: "page",
      pageId: "settings-ai",
      path: "/settings/ai",
      title: "AI 设置",
      icon: "sparkles",
    },
  ];
  const visible: Menu[] = [
    {
      id: "nav-dashboard",
      type: "page",
      pageId: "dashboard",
      path: "/",
      title: "工作台",
      icon: "workspace",
    },
    {
      id: "nav-settings",
      type: "group",
      title: "设置",
      icon: "settings",
      children: settingsChildren,
    },
  ];
  const covered = new Set([
    "dashboard",
    "settings-profile",
    "settings-appearance",
    "settings-ai",
  ]);
  const hiddenRoutes: Menu[] = site.pages
    .filter((page) => !covered.has(page.id))
    .map((page) => ({
      id: `route-${page.id}`,
      type: "page" as const,
      pageId: page.id,
      path: page.path,
      title: page.title,
      layout: page.layout ?? site.layout,
      hidden: true,
      ...(page.public ? { public: true } : {}),
      ...(page.activeMenu ? { activeMenu: page.activeMenu } : {}),
    }));
  const menus = [...visible, ...hiddenRoutes];
  if (user) {
    return menus;
  }
  return menus.filter((item) => item.public === true || item.hidden === true);
}

/** 将当前会话与页面清单组合为 Pactor sitemap。 */
export function buildSitemap(user: PortalSessionUser | null): SitemapResource {
  const { site } = state();
  return {
    id: site.id,
    resourceType: "sitemap",
    version: site.version,
    dslVersion: site.dslVersion,
    app: site.app,
    layout: site.layout,
    auth: site.auth,
    layouts: site.layouts,
    updatedAt: new Date().toISOString(),
    ...(user
      ? {
          user: {
            id: user.id,
            username: user.username,
            name: user.name,
          },
          permissions: user.permissions,
        }
      : {}),
    pages: site.pages.map((page) => ({
      id: page.id,
      path: page.path,
      title: page.title,
      version: page.version,
    })),
    menus: buildMenus(site, user),
  } as SitemapResource;
}

/** 懒加载页：校验会话与可见性。 */
export function getLazyPage(
  pageId: string,
  user: PortalSessionUser | null,
): { version: number; pageDsl: PageDsl } | null {
  const { site, pages } = state();
  const entry = site.pages.find((page) => page.id === pageId);
  const pageDsl = pages.get(pageId);
  if (!entry || !pageDsl) {
    return null;
  }
  if (!entry.public && !user) {
    return null;
  }
  return { version: entry.version, pageDsl };
}

export function findPageEntry(pageId: string): PageEntry | undefined {
  return state().site.pages.find((page) => page.id === pageId);
}
