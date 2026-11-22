import { dispatchConsoleApi } from "@/backend/module-compat";

/**
 * 兼容层：console 模块的 `/api/d/*` 数据请求 → 模块挂载路径。
 * 业务实现见 ui-platform-app 的 module/server.ts。
 */
type Context = { params: Promise<{ parts?: string[] }> };

async function handle(request: Request, context: Context) {
  const { parts = [] } = await context.params;
  return dispatchConsoleApi(request, `/api/d/${parts.join("/")}`);
}

export const GET = handle;
export const POST = handle;
export const PUT = handle;
export const PATCH = handle;
export const DELETE = handle;
export const OPTIONS = handle;
