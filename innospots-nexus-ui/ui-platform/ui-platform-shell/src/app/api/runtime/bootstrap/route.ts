import { cookies } from "next/headers";

import { createRuntimeHandlers } from "@pactor-app/shell/server/routes";

import { resolveSessionUser } from "@/backend/session";

/**
 * Runtime bootstrap：注入会话解析（平台 access token cookie，演示会话兜底），
 * 菜单 / 页面的登录态与 permission 可见性据此裁剪。cookies 由宿主侧
 * 注入（包与宿主分属不同 next 实例时 AsyncLocalStorage 不共享）。
 */
const handlers = createRuntimeHandlers({
  cookies,
  resolveUser: (store) => resolveSessionUser(store),
});

export const GET = handlers.bootstrap;
