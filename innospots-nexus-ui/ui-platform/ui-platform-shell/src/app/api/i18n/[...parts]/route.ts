import { dispatchConsoleApi } from "@/backend/module-compat";

/** 兼容层：console 模块的 `/api/i18n/*`（语言偏好 / 语言包）→ 模块挂载路径 */
type Context = { params: Promise<{ parts?: string[] }> };

async function handle(request: Request, context: Context) {
  const { parts = [] } = await context.params;
  return dispatchConsoleApi(request, `/api/i18n/${parts.join("/")}`);
}

export const GET = handle;
export const POST = handle;
export const OPTIONS = handle;
