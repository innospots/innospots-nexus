import type { PageDsl } from "@pactor-app/core";

import type { DemoUser } from "@/backend/auth/accounts";
import { serverBaseUrl } from "@/backend/config";
import {
  findPageDslById,
  findPageEntryById,
  findPageEntryByPath,
  getLoginPath,
  type PageEntry,
} from "@/backend/pages/sitemap";

/**
 * 页面访问校验：解析 sitemap.yaml 与 Page DSL `page` 上的 domain、module、group
 *（group 缺省 view；与 Pactor `grantGroup` 同义），转发平台 POST /api/public/page-access/verify。
 */

/** plugin-declaration entryPageKey（与 PageAccessVerifyRequest.pageKey 对齐） */
const ENTRY_PAGE_KEY_BY_ID: Record<string, string> = {
  dashboard: "platform-user-main",
  "role-list": "platform-role-main",
  "role-detail-basic": "platform-role-main",
  "role-detail-permissions": "platform-role-main",
  "role-detail-members": "platform-role-main",
  "tenant-list": "platform-organization-main",
  "tenant-detail": "platform-organization-main",
  "platform-user-list": "platform-user-main",
  "platform-user-create": "platform-user-main",
  "platform-user-detail": "platform-user-main",
  "platform-invite-list": "platform-user-main",
  "platform-access-request-list": "platform-user-main",
  "system-settings": "system-settings",
  themes: "themes",
  "audit-log-list": "platform-logger-main",
  "audit-log-detail": "platform-logger-main",
  "dictionary-type-list": "platform-dictionary-main",
  "dictionary-type-create": "platform-dictionary-main",
  "dictionary-type-detail": "platform-dictionary-main",
  "plugin-list": "nexus-plugin-main",
  "plugin-detail": "nexus-plugin-main",
};

type NexusPageMeta = {
  domain?: string;
  module?: string;
  group?: string;
  grantGroup?: string;
  name?: string;
};

export interface PageAccessBinding {
  path: string;
  domainKey: string;
  moduleKey: string;
  pageKey: string;
  grantGroup: string;
  publicPage: boolean;
  requiresCatalogGrant: boolean;
}

export interface PageAccessResult {
  allowed: boolean;
  reason: string;
  redirectTo?: string;
}

interface PlatformVerifyResponse {
  success?: boolean;
  code?: string | number;
  message?: string;
  data?: { allowed?: boolean; reason?: string };
}

function platformOk(code: string | number | undefined): boolean {
  return code === 0 || code === "0" || code === "OK";
}

function readPageMeta(dsl: PageDsl | undefined): NexusPageMeta | undefined {
  if (dsl?.page === undefined || typeof dsl.page !== "object") {
    return undefined;
  }
  return dsl.page as NexusPageMeta;
}

export function resolvePageAccessBinding(entry: PageEntry): PageAccessBinding {
  const meta = readPageMeta(findPageDslById(entry.id));
  const domainKey = (entry.domain ?? meta?.domain ?? "").trim();
  const moduleKey = (entry.module ?? meta?.module ?? "").trim();
  const grantGroup =
    (entry.group ?? meta?.group ?? meta?.grantGroup ?? "view").trim() || "view";
  const pageKey =
    ENTRY_PAGE_KEY_BY_ID[entry.id] ?? meta?.name?.trim() ?? entry.id;
  return {
    path: entry.path,
    domainKey,
    moduleKey,
    pageKey,
    grantGroup,
    publicPage: entry.public === true,
    requiresCatalogGrant: domainKey.length > 0 && moduleKey.length > 0,
  };
}

export function resolveBindingByPath(pathname: string): PageAccessBinding | null {
  const entry = findPageEntryByPath(pathname);
  return entry === null ? null : resolvePageAccessBinding(entry);
}

export function resolveBindingByPageId(pageId: string): PageAccessBinding | null {
  const entry = findPageEntryById(pageId);
  return entry === undefined ? null : resolvePageAccessBinding(entry);
}

export interface VerifyPageAccessOptions {
  pathname?: string;
  pageId?: string;
  user: DemoUser | null;
  accessToken?: string | null;
  cookie?: string | null;
}

/**
 * 校验页面访问权限。未匹配到 sitemap 页面时放行（交由路由 404）；
 * public 页放行；无 catalog 归属的受保护页仅要求登录；其余走平台 grant 校验。
 */
export async function verifyPageAccess(
  options: VerifyPageAccessOptions,
): Promise<PageAccessResult> {
  const binding =
    options.pathname !== undefined
      ? resolveBindingByPath(options.pathname)
      : options.pageId !== undefined
        ? resolveBindingByPageId(options.pageId)
        : null;

  if (binding === null) {
    return { allowed: true, reason: "未匹配到页面，交由应用路由处理" };
  }

  if (binding.publicPage) {
    return { allowed: true, reason: "公开页面" };
  }

  if (options.user === null) {
    return {
      allowed: false,
      reason: "未登录",
      redirectTo: getLoginPath(),
    };
  }

  if (!binding.requiresCatalogGrant) {
    return { allowed: true, reason: "受保护页面（无 catalog 分组）" };
  }

  if (process.env.NEXUS_PAGE_ACCESS_DISABLED === "true") {
    return { allowed: true, reason: "页面访问校验已禁用（NEXUS_PAGE_ACCESS_DISABLED）" };
  }

  return verifyWithPlatform(binding, options.accessToken, options.cookie);
}

async function verifyWithPlatform(
  binding: PageAccessBinding,
  accessToken?: string | null,
  cookie?: string | null,
): Promise<PageAccessResult> {
  const body = {
    domainKey: binding.domainKey,
    moduleKey: binding.moduleKey,
    pageKey: binding.pageKey,
    grantGroup: binding.grantGroup,
  };
  try {
    const response = await fetch(
      `${serverBaseUrl}/api/public/page-access/verify`,
      {
        method: "POST",
        headers: {
          "content-type": "application/json",
          ...(cookie ? { cookie } : {}),
          ...(accessToken ? { authorization: `Bearer ${accessToken}` } : {}),
        },
        body: JSON.stringify(body),
        cache: "no-store",
      },
    );
    const payload = (await response
      .json()
      .catch(() => ({}))) as PlatformVerifyResponse;
    const allowed =
      response.ok
      && platformOk(payload.code)
      && payload.data?.allowed === true;
    return {
      allowed,
      reason:
        payload.data?.reason
        ?? payload.message
        ?? (allowed ? "OK" : `页面权限校验失败：HTTP ${response.status}`),
    };
  } catch (error) {
    return {
      allowed: false,
      reason: `页面权限校验服务不可用：${
        error instanceof Error ? error.message : String(error)
      }`,
    };
  }
}
