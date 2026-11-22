/**
 * 模块服务端入口：Shell 通过 `/api/modules/console/*` 统一分发到此处。
 * 逻辑路径为原始 `/api/...`，由分发器还原后按固定段数匹配。
 *
 * 同时导出页面授权转换函数，供 Shell 的页面端点复用同一套
 * access / grantGroup 语义（避免 Shell 自行实现导致行为漂移）。
 */
import { createConsoleApiHandlers } from "@/backend/api/handlers";

export { applyActionAccess, collectActionRequests } from "@/backend/pages/action-access";

export const handlers = createConsoleApiHandlers();
