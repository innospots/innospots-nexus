import { dispatchConsoleApi } from "@/backend/module-compat";

/** 兼容层：console 模块的平台登录 → 模块挂载路径 */
export function POST(request: Request) {
  return dispatchConsoleApi(request, "/api/auth/platform-login");
}

export function OPTIONS(request: Request) {
  return dispatchConsoleApi(request, "/api/auth/platform-login");
}
