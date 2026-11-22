/**
 * 模块服务端 API 契约。
 *
 * 与 `@pactor-app/shell/server` 的 ModuleApiHandler / ModuleApiHandlers 结构一致；
 * 此处本地声明，避免业务模块在编译期依赖 Shell 包。
 */
export type ModuleApiMethod =
  | "GET"
  | "POST"
  | "PUT"
  | "PATCH"
  | "DELETE"
  | "HEAD"
  | "OPTIONS";

export interface ModuleApiContext {
  moduleId: string;
  /** 逻辑 URL（原始 /api/... 路径 + query，不是 /api/modules/<moduleId>/ 挂载地址） */
  url: URL;
  params: Record<string, string>;
}

export type ModuleApiHandler = (
  request: Request,
  context: ModuleApiContext,
) => Response | Promise<Response>;

export type ModuleApiHandlers = Record<
  string,
  Partial<Record<ModuleApiMethod, ModuleApiHandler>>
>;
