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

/** 从页面 DSL 解析访问配置并调用平台统一鉴权接口。 */

type NexusPageMeta = {
  "access-mode"?: string;
  domain?: string;
  module?: string;
  group?: string;
  grantGroup?: string;
  name?: string;
  pageKey?: string;
};

export interface PageAccessBinding {
  accessMode: string;
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
  requestPermissions?: Record<string, boolean>;
  grantGroups?: string[];
  publicPage?: boolean;
}

interface PlatformVerifyResponse {
  success?: boolean;
  code?: string | number;
  message?: string;
  data?: { allowed?: boolean; reason?: string; requestPermissions?: Record<string, boolean>; grantGroups?: string[] };
}

function platformOk(code: string | number | undefined): boolean {
  return code === 0 || code === "0" || code === "OK";
}

/** 平台英文 reason → 控制台可读文案 */
function normalizeDenyReason(reason: string | undefined): string {
  if (reason === undefined || reason.trim() === "") {
    return "无页面访问权限";
  }
  const trimmed = reason.trim();
  if (trimmed === "Page permission is required") {
    return "无页面访问权限";
  }
  if (trimmed === "Authentication is required") {
    return "未登录";
  }
  if (trimmed === "Workspace is required") {
    return "缺少工作区上下文";
  }
  return trimmed;
}

function readPageMeta(dsl: PageDsl | undefined): NexusPageMeta | undefined {
  if (dsl?.page === undefined || typeof dsl.page !== "object") {
    return undefined;
  }
  return dsl.page as NexusPageMeta;
}

export function resolvePageAccessBinding(entry: PageEntry): PageAccessBinding {
  const meta = readPageMeta(findPageDslById(entry.id));
  const domainKey = (meta?.domain ?? "").trim();
  const moduleKey = (meta?.module ?? "").trim();
  const grantGroup =
    (meta?.group ?? meta?.grantGroup ?? "view").trim() || "view";
  const pageKey =
    meta?.pageKey?.trim() ?? meta?.name?.trim() ?? entry.id;
  return {
    accessMode: meta?.["access-mode"] ?? "",
    path: entry.path,
    domainKey,
    moduleKey,
    pageKey,
    grantGroup,
    publicPage: meta?.["access-mode"] === "public",
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
  requests?: { method: string; url: string }[];
}

/**
 * 未匹配到页面时由应用路由处理；public 页不请求平台，其余模式统一由平台验证。
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

  if (!["public", "authenticated", "grant"].includes(binding.accessMode)) {
    return { allowed: false, reason: "页面访问模式缺失或无效" };
  }

  if (binding.publicPage) {
    return { allowed: true, reason: "公开页面", publicPage: true };
  }

  if (binding.accessMode === "grant" && !binding.requiresCatalogGrant) {
    return { allowed: false, reason: "页面授权资源配置不完整" };
  }

  const result = await verifyWithPlatform(binding, options.accessToken, options.cookie, options.requests ?? []);
  if (!result.allowed && options.user === null) result.redirectTo = getLoginPath();
  return result;
}

async function verifyWithPlatform(
  binding: PageAccessBinding,
  accessToken?: string | null,
  cookie?: string | null,
  requests: { method: string; url: string }[] = [],
): Promise<PageAccessResult> {
  const body = {
    domainKey: binding.domainKey,
    moduleKey: binding.moduleKey,
    pageKey: binding.pageKey,
    grantGroup: binding.grantGroup,
    accessMode: binding.accessMode,
    requests,
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
        signal: AbortSignal.timeout(5000),
      },
    );
    const payload = (await response
      .json()
      .catch(() => ({}))) as PlatformVerifyResponse;
    const allowed =
      response.ok
      && platformOk(payload.code)
      && payload.data?.allowed === true;
    if (allowed && (!payload.data?.requestPermissions
      || typeof payload.data.requestPermissions !== "object"
      || Array.isArray(payload.data.requestPermissions)
      || !Array.isArray(payload.data.grantGroups)
      || payload.data.grantGroups.some((group) => typeof group !== "string"))) {
      throw new Error("权限检查返回格式无效");
    }
    return {
      allowed,
      requestPermissions: payload.data?.requestPermissions,
      grantGroups: payload.data?.grantGroups,
      reason: normalizeDenyReason(
        payload.data?.reason
        ?? payload.message
        ?? (allowed ? "OK" : `页面权限校验失败：HTTP ${response.status}`),
      ),
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
