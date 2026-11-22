import type { PageDsl } from "@pactor-app/core";

import {
  applyActionAccess,
  collectActionRequests,
} from "@innospots/ui-platform-app/server";

import { serverBaseUrl } from "./config";

/**
 * 控制台页面授权：Shell 复用模块导出的 access 语义。
 *
 * 1. 把模块挂载 URL（/api/modules/console/...）还原为逻辑 URL（/api/...）；
 * 2. 收集 DSL 中的操作请求，调用平台 /api/public/page-access/verify；
 * 3. 按返回的 requestPermissions / grantGroups 移除或禁用无权限操作。
 */

type PageMeta = {
  "access-mode"?: string;
  domain?: string;
  module?: string;
  group?: string;
  grantGroup?: string;
  pageKey?: string;
  name?: string;
};

export interface PageAccessResult {
  allowed: boolean;
  /** 后端返回 401（令牌失效 / 过期）：前端应跳转登录页 */
  authFailure?: boolean;
  reason?: string;
  redirectTo?: string;
  requestPermissions?: Record<string, boolean>;
  grantGroups?: string[];
  ignoreGrantGroup?: boolean;
  publicPage?: boolean;
}

interface VerifyEnvelope {
  success?: boolean;
  code?: string | number;
  message?: string;
  data?: {
    allowed?: boolean;
    reason?: string;
    requestPermissions?: Record<string, boolean>;
    grantGroups?: string[];
    ignoreGrantGroup?: boolean;
  };
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

/** `/api/modules/console/d/x` → `/api/d/x`（保持后端授权键与兼容层路径一致） */
export function normalizeConsoleApiPaths(value: unknown): unknown {
  if (typeof value === "string") {
    return value.startsWith("/api/modules/console/")
      ? `/api${value.slice("/api/modules/console".length)}`
      : value;
  }
  if (Array.isArray(value)) return value.map(normalizeConsoleApiPaths);
  if (!isRecord(value)) return value;
  return Object.fromEntries(
    Object.entries(value).map(([key, child]) => [key, normalizeConsoleApiPaths(child)]),
  );
}

function pageMeta(page: PageDsl): PageMeta {
  return isRecord(page.page) ? (page.page as PageMeta) : {};
}

export async function verifyConsolePageAccess(
  page: PageDsl,
  options: { cookie?: string; accessToken?: string },
): Promise<PageAccessResult> {
  const meta = pageMeta(page);
  const accessMode = meta["access-mode"] ?? "";
  if (!["public", "authenticated", "grant"].includes(accessMode)) {
    return { allowed: false, reason: "页面访问模式缺失或无效" };
  }
  if (accessMode === "public") {
    return { allowed: true, reason: "公开页面", publicPage: true };
  }

  const domainKey = (meta.domain ?? "").trim();
  const moduleKey = (meta.module ?? "").trim();
  if (accessMode === "grant" && (!domainKey || !moduleKey)) {
    return { allowed: false, reason: "页面授权资源配置不完整" };
  }

  const requests = collectActionRequests(page, isRecord(page.actions) ? page.actions : {});
  try {
    const response = await fetch(`${serverBaseUrl}/api/public/page-access/verify`, {
      method: "POST",
      headers: {
        "content-type": "application/json",
        ...(options.cookie ? { cookie: options.cookie } : {}),
        ...(options.accessToken ? { authorization: `Bearer ${options.accessToken}` } : {}),
      },
      body: JSON.stringify({
        domainKey,
        moduleKey,
        pageKey: (meta.pageKey ?? meta.name ?? "").trim(),
        grantGroup: (meta.group ?? meta.grantGroup ?? "view").trim() || "view",
        accessMode,
        requests,
      }),
      cache: "no-store",
      signal: AbortSignal.timeout(5000),
    });
    const payload = (await response.json().catch(() => null)) as VerifyEnvelope | null;
    const data = payload?.data;
    const allowed = response.ok && payload?.success !== false && data?.allowed === true;
    const result: PageAccessResult = {
      allowed,
      authFailure: response.status === 401,
      reason: data?.reason ?? payload?.message ?? (allowed ? "OK" : "无页面访问权限"),
      requestPermissions: data?.requestPermissions,
      grantGroups: data?.grantGroups,
      ignoreGrantGroup: data?.ignoreGrantGroup === true,
    };
    if (!allowed && (result.authFailure === true || options.accessToken === undefined)) {
      result.redirectTo = "/console/login";
    }
    return result;
  } catch (error) {
    return {
      allowed: false,
      reason: `页面权限校验服务不可用：${
        error instanceof Error ? error.message : String(error)
      }`,
    };
  }
}

/** 按授权结果裁剪页面操作；无 requestPermissions 时仅移除作者态 access 声明 */
export function applyConsolePageAccess(
  page: PageDsl,
  access: PageAccessResult,
): PageDsl {
  if (
    access.publicPage === true ||
    access.ignoreGrantGroup === true ||
    access.requestPermissions === undefined
  ) {
    return stripAuthorAccess(page) as PageDsl;
  }
  return applyActionAccess(
    page,
    {
      allowed: access.requestPermissions,
      grantGroups: access.grantGroups ?? [],
    },
    false,
  );
}

/** 无授权上下文时移除作者态 access，避免 Pactor schema 校验失败 */
export function stripAuthorAccess(value: unknown): unknown {
  if (Array.isArray(value)) return value.map(stripAuthorAccess);
  if (!isRecord(value)) return value;
  return Object.fromEntries(
    Object.entries(value)
      .filter(([key]) => key !== "access")
      .map(([key, child]) => [key, stripAuthorAccess(child)]),
  );
}
