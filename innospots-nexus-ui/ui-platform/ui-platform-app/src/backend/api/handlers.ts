/**
 * 控制台模块 API 逻辑路径注册表。
 *
 * 独立开发：`src/app/api/**` 直接调用同一批实现；
 * 打包为模块：`module/server.ts` 导出本表，Shell 通过
 * `/api/modules/console/*` 统一分发（manifest.apiPrefixes 已声明）。
 */
import { deprecatedDemoLogin, platformLogin, platformLogout } from "./auth";
import { dataPartsOf, forwardDataRequest } from "./data-proxy";
import { writeLocaleCookie } from "./i18n";
import type { ModuleApiHandlers } from "./types";

const DATA_METHODS = ["GET", "POST", "PUT", "PATCH", "DELETE", "HEAD"] as const;

/** `/api/d` 的最大路径深度（与通用分发器的固定段数匹配方式一致） */
const DATA_MAX_DEPTH = 8;

function handleData(
  request: Request,
  context: { url: URL },
): Promise<Response> {
  return forwardDataRequest(request, dataPartsOf(context.url.pathname));
}

export function createConsoleApiHandlers(): ModuleApiHandlers {
  const handlers: ModuleApiHandlers = {
    "/api/auth/login": { POST: deprecatedDemoLogin },
    "/api/auth/platform-login": { POST: platformLogin },
    "/api/auth/logout": { POST: platformLogout },
    "/api/i18n/locale": { POST: writeLocaleCookie },
  };

  // 分发器只支持精确路径与固定段数的 :param 模式，按 1..maxDepth 段各注册一条
  for (let depth = 1; depth <= DATA_MAX_DEPTH; depth += 1) {
    const params = Array.from({ length: depth }, (_, index) => `:p${index}`).join(
      "/",
    );
    const pattern = `/api/d/${params}`;
    const methodMap: ModuleApiHandlers[string] = {};
    for (const method of DATA_METHODS) {
      methodMap[method] = handleData;
    }
    handlers[pattern] = methodMap;
  }

  return handlers;
}
