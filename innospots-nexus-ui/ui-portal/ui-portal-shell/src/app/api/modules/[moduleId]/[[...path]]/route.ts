import {
  createModuleDispatcher,
  type ModuleApiRouteContext,
} from "@pactor-app/shell/server";

import { moduleServers } from "@generated/pactor/server-registry";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

/**
 * 模块 API 统一分发入口。
 *
 * 模块 tgz 自带 server/index.mjs，装配器生成 server-registry 后由此处按
 * moduleId + 逻辑路径分发。
 */
const dispatch = createModuleDispatcher(moduleServers);

export function GET(request: Request, context: ModuleApiRouteContext) {
  return dispatch(request, context);
}

export function POST(request: Request, context: ModuleApiRouteContext) {
  return dispatch(request, context);
}

export function PUT(request: Request, context: ModuleApiRouteContext) {
  return dispatch(request, context);
}

export function PATCH(request: Request, context: ModuleApiRouteContext) {
  return dispatch(request, context);
}

export function DELETE(request: Request, context: ModuleApiRouteContext) {
  return dispatch(request, context);
}

export function OPTIONS(request: Request, context: ModuleApiRouteContext) {
  return dispatch(request, context);
}
