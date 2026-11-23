import {
  createModuleDispatcher,
  type ModuleApiRouteContext,
} from "@pactor-app/shell/server";

import { moduleServers } from "@generated/pactor/server-registry";

/**
 * 兼容层：portal 模块客户端按独立运行时的相对路径调用
 * `/api/auth/*`、`/api/d/*`。这里把路径改写成模块挂载路径
 * `/api/modules/portal/*`，再交给 Shell 通用分发器。
 */
export const PORTAL_MODULE_ID = "portal";

const dispatch = createModuleDispatcher(moduleServers);

export function dispatchPortalApi(
  request: Request,
  logicalPath: string,
): Promise<Response> {
  const url = new URL(request.url);
  url.pathname = `/api/modules/${PORTAL_MODULE_ID}${logicalPath.replace(/^\/api/, "")}`;

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
    params: Promise.resolve({ moduleId: PORTAL_MODULE_ID }),
  };
  return dispatch(new Request(url, init), context);
}
