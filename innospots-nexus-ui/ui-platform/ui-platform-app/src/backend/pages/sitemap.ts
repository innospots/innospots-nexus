import { readFileSync, readdirSync, watch } from "node:fs";
import { join, relative } from "node:path";

import type { PageDsl, PermissionConfig } from "@pactor-app/core";
import { localizePageDsl, type AuthorPageDsl } from "@pactor-app/i18n";
import { parseDocument } from "yaml";

import type { LayoutDsl, SitemapResource } from "pactor-app";

import type { DemoUser } from "@/backend/auth/accounts";
import type { RequestI18n } from "@/backend/pages/i18n";
import { consoleTranslatableFields } from "@/backend/pages/i18n";

/** 页面清单与布局来自本地 YAML；可见导航来自后端，非菜单页面生成隐藏路由。 */

/* ---------------- YAML DSL 文件加载（src/backend/pages/yaml/，缓存 + dev 下 fs.watch 失效重读） ---------------- */

const YAML_DIR = join(process.cwd(), "src/backend/pages/yaml");

/** 解析 YAML 文件；语法错误时 fail-fast（parseDocument 默认静默容错，结构会悄悄残缺） */
function loadYaml<T>(file: string): T {
  const doc = parseDocument(readFileSync(join(YAML_DIR, file), "utf8"));
  if (doc.errors.length > 0) {
    throw new Error(
      `${file} 解析失败：${doc.errors.map((e) => e.message.split("\n")[0]).join("；")}`,
    );
  }
  return doc.toJS() as T;
}

/** sitemap 中的路由及布局信息；授权归属从页面 DSL 读取。 */
interface PageEntry {
  id: string;
  path: string;
  title: string;
  version: number;
  public?: boolean;
  permission?: string;
  /** 不随 sitemap 内联 pageDsl，客户端经 /api/public/pages/{id} 懒加载 */
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
  namespace: string;
  path: string;
}

/** 递归收集 yaml 下所有页面 DSL（含业务域子目录；跳过 sitemap.yaml），按 id 建索引。 */
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
    const relativePath = relative(YAML_DIR, entryPath);
    const namespace = relativePath.includes("/")
      ? relativePath.split("/")[0]
      : "common";
    files.set(id, { namespace, path: relativePath });
  }
  return files;
}

interface LoadedPage {
  pageDsl: PageDsl;
}

interface YamlState {
  site: SitemapFile;
  pages: Map<string, LoadedPage>;
}

function loadState(): YamlState {
  const site = loadYaml<SitemapFile>("sitemap.yaml");
  const pageFiles = collectPageFiles(YAML_DIR);

  function loadPageDsl(id: string): PageDsl {
    const file = pageFiles.get(id);
    if (!file) throw new Error(`页面 DSL 缺失：${id}.yaml`);
    return loadYaml<PageDsl>(file.path);
  }

  const pages = new Map<string, LoadedPage>();
  for (const entry of site.pages) {
    const pageDsl = loadPageDsl(entry.id);
    const page = pageDsl.page as unknown as Record<string, unknown>;
    const mode = page["access-mode"];
    if (page.id !== entry.id || !["public", "authenticated", "grant"].includes(String(mode))) {
      throw new Error(`页面 ${entry.id} 的 id 或 access-mode 无效`);
    }
    if (!entry.lazy) {
      throw new Error(`页面 ${entry.id} 必须通过懒加载端点进行访问检查`);
    }
    if (mode === "grant" && [page.domain, page.module, page.pageKey].some(
      (value) => typeof value !== "string" || value.trim() === "",
    )) {
      throw new Error(`页面 ${entry.id} 的 domain、module 或 pageKey 缺失`);
    }
    entry.public = page["access-mode"] === "public";
    pages.set(entry.id, { pageDsl });
  }

  return { site, pages };
}

let stateCache: YamlState | null = null;

/** YAML 状态：首次请求时读入缓存；dev 下 yaml 目录任一文件变更由 fs.watch 失效缓存，下次请求重读，无需重启 */
function state(): YamlState {
  if (!stateCache) stateCache = loadState();
  return stateCache;
}

// watcher 挂 globalThis：Next dev 重编译会重新执行本模块，只更新失效回调、不重复建 watcher
const globalForYamlWatch = globalThis as unknown as {
  __pactorYamlInvalidate?: () => void;
  __pactorYamlWatcher?: ReturnType<typeof watch>;
};
globalForYamlWatch.__pactorYamlInvalidate = () => {
  stateCache = null;
};
if (process.env.NODE_ENV !== "production" && !globalForYamlWatch.__pactorYamlWatcher) {
  globalForYamlWatch.__pactorYamlWatcher = watch(
    YAML_DIR,
    { recursive: true },
    () => {
      globalForYamlWatch.__pactorYamlInvalidate?.();
    },
  );
}

/* ---------------- 会话裁剪 ---------------- */

type Visibility = { public?: boolean; permission?: PermissionConfig };

/** PermissionConfig 归一化为权限码数组（string / string[] / { code } 三种形态） */
function permissionCodes(permission: PermissionConfig): string[] {
  const code =
    typeof permission === "object" && !Array.isArray(permission)
      ? permission.code
      : permission;
  return Array.isArray(code) ? code : [code];
}

/** 可见性：public 匿名可见；permission 命中任一权限码；无标记需登录 */
function canSee(entry: Visibility, user: DemoUser | null): boolean {
  if (entry.public) return true;
  if (!user) return false;
  if (entry.permission) {
    return permissionCodes(entry.permission).some((c) =>
      user.permissions.includes(c),
    );
  }
  return true;
}

/** 将当前会话与后端导航组合为 Pactor sitemap。 */
function sitemapUser(user: DemoUser): DemoUser {
  const name = user.name?.trim();
  return {
    ...user,
    name: name && name.length > 0 ? name : user.username,
  };
}

function localizeLayout(
  layout: LayoutDsl,
  i18n: RequestI18n,
): LayoutDsl {
  // LayoutDsl 没有作者态 i18n 字段；借临时 Page DSL 复用 Pactor 的节点转换。
  const page: AuthorPageDsl = {
    dsl: layout.dsl,
    page: { id: "layout", title: "" },
    ...(layout.state === undefined ? {} : { state: layout.state }),
    i18n: { namespaces: ["common"] },
    body: layout.body,
  };
  const localized = localizePageDsl({
    page,
    locale: i18n.locale,
    defaultLocale: i18n.defaultLocale,
    languageBundles: i18n.languageBundles,
    registry: consoleTranslatableFields,
  });
  return {
    ...layout,
    ...(localized.state === undefined ? {} : { state: localized.state }),
    body: localized.body ?? layout.body,
  };
}

function resolveMenuTitle(
  menu: SitemapResource["menus"][number],
  messages: Readonly<Record<string, string>>,
): string {
  const byMenuId = messages[`common.menu.${menu.id}.title`];
  if (byMenuId !== undefined && byMenuId !== "") {
    return byMenuId;
  }
  if (menu.pageId) {
    const byPageId = messages[`common.page.${menu.pageId}.title`];
    if (byPageId !== undefined && byPageId !== "") {
      return byPageId;
    }
  }
  return menu.title;
}

function localizeMenus(
  menus: SitemapResource["menus"],
  messages: Readonly<Record<string, string>>,
): SitemapResource["menus"] {
  return menus.map((menu) => ({
    ...menu,
    title: resolveMenuTitle(menu, messages),
    ...(menu.type === "group"
      ? { children: localizeMenus(menu.children ?? [], messages) }
      : {}),
  }));
}

/** 为菜单项注入 page 声明中的 path，供 NavigationShell 左轨 / 侧栏链接使用。 */
function enrichMenusWithPaths(
  menus: SitemapResource["menus"],
  pages: SitemapFile["pages"],
): SitemapResource["menus"] {
  const pathByPageId = new Map(pages.map((page) => [page.id, page.path]));
  function walk(items: SitemapResource["menus"]): SitemapResource["menus"] {
    return items.map((item) => {
      const next = { ...item };
      if (item.pageId && !item.path) {
        const path = pathByPageId.get(item.pageId);
        if (path) {
          next.path = path;
        }
      }
      if (item.type === "group" && item.children) {
        next.children = walk(item.children);
      }
      return next;
    });
  }
  return walk(menus);
}

export function buildSitemap(
  user: DemoUser | null,
  i18n: RequestI18n,
  navigationMenus: SitemapResource["menus"],
): SitemapResource {
  const { site, pages: loadedPages } = state();
  const pages: SitemapResource["pages"] = site.pages
    .map((p) => {
      const loaded = loadedPages.get(p.id);
      return {
        id: p.id,
        path: p.path,
        title: i18n.messages[`common.page.${p.id}.title`] ?? p.title,
        version: p.version,
        ...(p.lazy || !loaded
          ? {}
          : {
              pageDsl: localizePageDsl(
                {
                  page: loaded.pageDsl as AuthorPageDsl,
                  locale: i18n.locale,
                  defaultLocale: i18n.defaultLocale,
                  languageBundles: i18n.languageBundles,
                  registry: consoleTranslatableFields,
                },
              ),
            }),
      };
    });
  const layouts = Object.fromEntries(
    Object.entries(site.layouts).map(([name, layout]) => [
      name,
      localizeLayout(layout, i18n),
    ]),
  ) as SitemapFile["layouts"];

  // 页面路由和布局由本地清单提供，导航由后端按当前用户过滤。
  const visiblePageIds = new Set<string>();
  const collect = (items: SitemapResource["menus"]) => {
    for (const item of items) {
      if (item.pageId) visiblePageIds.add(item.pageId);
      if (item.children) collect(item.children);
    }
  };
  collect(navigationMenus);
  const routeEntries: SitemapResource["menus"] = site.pages
    .filter((page) => !visiblePageIds.has(page.id))
    .map((page) => ({
      id: page.id,
      type: "page" as const,
      pageId: page.id,
      title: page.title,
      layout: page.layout ?? "admin",
      hidden: true,
      ...(page.public ? { public: true } : {}),
      ...(page.activeMenu ? { activeMenu: page.activeMenu } : {}),
    }));
  return {
    ...site,
    layouts,
    updatedAt: new Date().toISOString(),
    ...(user
      ? {
          user: sitemapUser(user) as unknown as Record<string, unknown>,
          permissions: user.permissions,
        }
      : {}),
    pages,
    menus: localizeMenus(
      enrichMenusWithPaths([...navigationMenus, ...routeEntries], site.pages),
      i18n.messages,
    ),
  };
}

/** 懒加载页：校验会话与可见性（前端权限只控制 UI，接口必须鉴权） */
export function getLazyPage(
  pageId: string,
  user: DemoUser | null,
  i18n: RequestI18n,
): { version: number; pageDsl: PageDsl } | null {
  const { site, pages } = state();
  const entry = site.pages.find((p) => p.id === pageId);
  const page = pages.get(pageId);
  if (!entry?.lazy || !page || !canSee(entry, user)) {
    return null;
  }
  return {
    version: entry.version,
    pageDsl: localizePageDsl({
      page: page.pageDsl as AuthorPageDsl,
      locale: i18n.locale,
      defaultLocale: i18n.defaultLocale,
      languageBundles: i18n.languageBundles,
      registry: consoleTranslatableFields,
    }),
  };
}

/* ---------------- 页面访问校验辅助（page-access.ts / 懒加载页 API） ---------------- */

export type { PageEntry };

interface RouteRef {
  pattern: string;
  pageId: string;
}

export function getLoginPath(): string {
  return state().site.auth.loginPath ?? "/login";
}

export function findPageEntryById(pageId: string): PageEntry | undefined {
  return state().site.pages.find((page) => page.id === pageId);
}

export function findPageDslById(pageId: string): PageDsl | undefined {
  return state().pages.get(pageId)?.pageDsl;
}

export function listPageEntries(): PageEntry[] {
  return state().site.pages;
}

export function findPageEntryByPath(pathname: string): PageEntry | null {
  const { site } = state();
  const refs: RouteRef[] = [];
  for (const page of site.pages) {
    refs.push({ pattern: page.path, pageId: page.id });
  }

  const path = normalizeRoutePath(pathname);
  const matched = refs
    .map((ref, index) => ({ ref, index }))
    .filter(({ ref }) => matchRoutePath(ref.pattern, path))
    .sort(
      (a, b) =>
        staticSegmentCount(b.ref.pattern) - staticSegmentCount(a.ref.pattern) ||
        a.index - b.index,
    )[0];
  if (matched === undefined) {
    return null;
  }
  return site.pages.find((page) => page.id === matched.ref.pageId) ?? null;
}

function splitPath(value: string): string[] {
  return normalizeRoutePath(value)
    .split("/")
    .filter(Boolean)
    .map((segment) => {
      try {
        return decodeURIComponent(segment);
      } catch {
        return segment;
      }
    });
}

function matchRoutePath(pattern: string, pathname: string): boolean {
  const patternSegments = splitPath(pattern);
  const pathSegments = splitPath(pathname);
  return (
    patternSegments.length === pathSegments.length &&
    patternSegments.every(
      (segment, index) =>
        segment.startsWith(":") || segment === pathSegments[index],
    )
  );
}

function staticSegmentCount(pattern: string): number {
  return splitPath(pattern).filter((segment) => !segment.startsWith(":")).length;
}

function normalizeRoutePath(pathname: string): string {
  const path = pathname.split("?")[0] ?? pathname;
  return path === "/" ? "/" : path.replace(/\/+$/, "");
}
