/**
 * 模块服务端入口：Shell 通过 `/api/modules/portal/*` 统一分发到此处。
 * 逻辑路径为原始 `/api/...`，由分发器还原后按固定段数匹配。
 */
import { createPortalApiHandlers } from "@/backend/api/handlers";

export const handlers = createPortalApiHandlers();
