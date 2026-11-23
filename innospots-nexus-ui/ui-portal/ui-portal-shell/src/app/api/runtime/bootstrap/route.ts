import { cookies } from "next/headers";

import { createRuntimeHandlers } from "@pactor-app/shell/server/routes";

import { resolveSessionUser } from "@/backend/session";

/**
 * Runtime bootstrap：注入门户会话解析，菜单 / 页面可见性据此裁剪。
 */
const handlers = createRuntimeHandlers({
  cookies,
  resolveUser: (store) => resolveSessionUser(store),
});

export const GET = handlers.bootstrap;
