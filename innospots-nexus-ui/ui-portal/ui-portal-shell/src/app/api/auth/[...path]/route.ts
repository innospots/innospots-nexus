import { dispatchPortalApi } from "@/backend/module-compat";

/** 兼容层：portal 模块的 `/api/auth/*` → 模块挂载路径 */
type Context = { params: Promise<{ path?: string[] }> };

async function handle(request: Request, context: Context) {
  const { path = [] } = await context.params;
  return dispatchPortalApi(request, `/api/auth/${path.join("/")}`);
}

export const GET = handle;
export const POST = handle;
export const OPTIONS = handle;
