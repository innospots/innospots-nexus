import { corsHeaders, forwardDataRequest } from "@/backend/api";

/**
 * 页面数据接口统一入口（独立开发）。
 *
 * 打包为模块后由 Shell 的 `/api/modules/console/api/d/*` 承接，二者共用
 * `src/backend/api/data-proxy.ts` 的实现。
 */
async function handle(
  request: Request,
  context: RouteContext<"/api/d/[...parts]">,
) {
  const { parts } = await context.params;
  return forwardDataRequest(request, parts ?? []);
}

export const GET = handle;
export const POST = handle;
export const PUT = handle;
export const PATCH = handle;
export const DELETE = handle;

/** 预检（跨源带凭据 / 自定义头时） */
export function OPTIONS(request: Request): Response {
  return new Response(null, { status: 204, headers: corsHeaders(request) });
}
