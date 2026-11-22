import type { PageDsl } from "@pactor-app/core";

import type { ModuleApiHandlers } from "../src/backend/api/types";

/**
 * Shell 装配期类型契约：`<pkg>/server` 只发布构建产物（dist-module），
 * 由本声明文件提供 handlers 与页面授权转换函数的类型。
 */
export declare const handlers: ModuleApiHandlers;

export declare function collectActionRequests(
  value: unknown,
  namedActions?: Record<string, unknown>,
): { method: string; url: string }[];

export declare function applyActionAccess(
  page: PageDsl,
  result: { allowed: Record<string, boolean>; grantGroups: string[] },
  failed: boolean,
  failureMessage?: string,
): PageDsl;
