import { dispatchPortalApi } from "@/backend/module-compat";

/** 兼容层：portal 模块的 `/api/d/*` 数据请求 → 模块挂载路径 */
type Context = { params: Promise<{ parts?: string[] }> };

async function handle(request: Request, context: Context) {
  const { parts = [] } = await context.params;
  return dispatchPortalApi(request, `/api/d/${parts.join("/")}`);
}

export const GET = handle;
export const POST = handle;
export const PUT = handle;
export const PATCH = handle;
export const DELETE = handle;
export const OPTIONS = handle;
