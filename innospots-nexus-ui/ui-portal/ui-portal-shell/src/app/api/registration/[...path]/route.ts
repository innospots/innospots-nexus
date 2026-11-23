import { dispatchPortalApi } from "@/backend/module-compat";

/** 兼容层：portal 模块的 `/api/registration/*`（独立注册领域）→ 模块挂载路径 */
type Context = { params: Promise<{ path?: string[] }> };

async function handle(request: Request, context: Context) {
  const { path = [] } = await context.params;
  return dispatchPortalApi(request, `/api/registration/${path.join("/")}`);
}

export const POST = handle;
export const OPTIONS = handle;
