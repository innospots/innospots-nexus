import {
  createModuleDispatcher,
  type ModuleApiRouteContext,
} from "@pactor-app/shell/server";

import { moduleServers } from "@generated/pactor/server-registry";

/**
 * 兼容层：console 模块的客户端代码仍按独立运行时的相对路径调用
 * `/api/d/*`、`/api/i18n/*`、`/api/auth/platform-login`。
 *
 * 这里把旧路径改写成模块挂载路径 `/api/modules/console/*`，再交给 Shell 的
 * 通用分发器，保证业务实现只存在于模块 server entry 一处。
 * 仅用于过渡；模块客户端后续可直接改调挂载路径后删除本文件。
 */
export const CONSOLE_MODULE_ID = "console";

const dispatch = createModuleDispatcher(moduleServers);

/**
 * @param logicalPath 原始逻辑路径，如 `/api/d/platform/roles`
 */
export function dispatchConsoleApi(
  request: Request,
  logicalPath: string,
): Promise<Response> {
  const url = new URL(request.url);
  url.pathname = `/api/modules/${CONSOLE_MODULE_ID}${logicalPath.replace(/^\/api/, "")}`;

  const method = request.method.toUpperCase();
  const init: RequestInit & { duplex?: "half" } = {
    method,
    headers: request.headers,
  };
  if (method !== "GET" && method !== "HEAD") {
    init.body = request.body;
    init.duplex = "half";
  }

  const context: ModuleApiRouteContext = {
    params: Promise.resolve({ moduleId: CONSOLE_MODULE_ID }),
  };
  return dispatch(new Request(url, init), context);
}
