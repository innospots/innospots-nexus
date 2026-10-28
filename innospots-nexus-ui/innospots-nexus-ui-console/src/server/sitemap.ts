import { readFileSync, readdirSync, watch } from "node:fs";
import { join, relative } from "node:path";

import type { PageDsl, PermissionConfig } from "@pactor-app/core";
import { localizePageDsl, type AuthorPageDsl } from "@pactor-app/i18n";
import { parseDocument } from "yaml";

import type { LayoutDsl, SitemapResource } from "pactor-app";

import type { DemoUser } from "./accounts";
import type { RequestI18n } from "./i18n";
import { consoleTranslatableFields } from "./i18n-registry";

/**
 * Mock sitemap 数据源（design/dynamic-page.md 方案 B 示例实现）。
 * 真实项目由后端接口返回同构对象——端点按会话裁剪：
 * 匿名 → public 页（登录/注册）；登录后 → 该角色可见的 menus / pages / permissions。
 *
 * 入口数据（元数据 / layouts / pages / menus 全量）在 yaml/sitemap.yaml，
 * 页面 DSL 在 yaml/<域>/<id>.yaml（route / provider / audit / model /
 * 等，按 PRD 模块树划分）；本文件只负责：
 * 加载 YAML、按会话裁剪可见性（public / permission / 需登录）、注入会话
 * 动态字段（updatedAt / user / permissions）。
 */

/* ---------------- YAML DSL 文件加载（src/server/yaml/，缓存 + dev 下 fs.watch 失效重读） ---------------- */

const YAML_DIR = join(process.cwd(), "src/server/yaml");

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

/** sitemap.yaml 中的页面声明：可见性标记（public / permission）+ DSL 加载方式（lazy） */
interface PageEntry {
  id: string;
  path: string;
  title: string;
  version: number;
  public?: boolean;
  permission?: string;
  /** 不随 sitemap 内联 pageDsl，客户端经 /api/public/pages/{id} 懒加载 */
  lazy?: boolean;
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
  menus: SitemapResource["menus"];
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

  return {
    site,
    pages: new Map(
      site.pages.map((page) => [
        page.id,
        {
          pageDsl: loadPageDsl(page.id),
        },
      ]),
    ),
  };
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

/** 菜单按会话过滤；group 无可见子项时整组隐藏 */
function filterMenus(
  menus: SitemapResource["menus"],
  user: DemoUser | null,
): SitemapResource["menus"] {
  const out: SitemapResource["menus"] = [];
  for (const item of menus) {
    if (!canSee(item, user)) continue;
    if (item.type === "group") {
      const children = filterMenus(item.children ?? [], user);
      if (children.length === 0) continue;
      out.push({ ...item, children });
    } else {
      out.push(item);
    }
  }
  return out;
}

/**
 * 按会话构建 sitemap：同一 URL，不同用户拿到不同内容。
 * 匿名 → 仅 public 页；guest → + 首页；admin → + 用户列表 / 报表组。
 */
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

function localizeMenus(
  menus: SitemapResource["menus"],
  messages: Readonly<Record<string, string>>,
): SitemapResource["menus"] {
  return menus.map((menu) => ({
    ...menu,
    title: messages[`common.menu.${menu.id}.title`] ?? menu.title,
    ...(menu.type === "group"
      ? { children: localizeMenus(menu.children ?? [], messages) }
      : {}),
  }));
}

export function buildSitemap(
  user: DemoUser | null,
  i18n: RequestI18n,
): SitemapResource {
  const { site, pages: loadedPages } = state();
  const pages: SitemapResource["pages"] = site.pages
    .filter((p) => canSee(p, user))
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

  // sitemap.yaml 原样透传，仅覆盖会话相关部分：pages / menus 按可见性过滤，
  // updatedAt / user / permissions 按会话注入
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
    menus: localizeMenus(filterMenus(site.menus, user), i18n.messages),
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
