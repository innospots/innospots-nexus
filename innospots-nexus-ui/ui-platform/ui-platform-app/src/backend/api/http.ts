/**
 * 模块服务端 API 的 HTTP 基础设施：cookie 读写、JSON 响应与 CORS。
 *
 * 独立开发时 Next route handler 与打包后的 module server entry 共用本文件，
 * 因此**不得**依赖 `next/headers` / `next/server`。
 */

export interface CookieReader {
  get: (name: string) => { value: string } | undefined;
}

export interface CookieOptions {
  httpOnly?: boolean;
  sameSite?: "lax" | "strict" | "none";
  path?: string;
  maxAge?: number;
  secure?: boolean;
}

/** 从原始 Cookie 请求头构造读取器（同名取第一个，空值视为不存在） */
export function requestCookieReader(request: Request): CookieReader {
  const values = new Map<string, string>();
  for (const part of (request.headers.get("cookie") ?? "").split(";")) {
    const index = part.indexOf("=");
    if (index <= 0) {
      continue;
    }
    const name = part.slice(0, index).trim();
    const raw = part.slice(index + 1).trim();
    if (!name || values.has(name)) {
      continue;
    }
    try {
      values.set(name, decodeURIComponent(raw));
    } catch {
      values.set(name, raw);
    }
  }
  return {
    get(name) {
      const value = values.get(name);
      return value === undefined ? undefined : { value };
    },
  };
}

/** 序列化单条 Set-Cookie（值按 RFC 6265 做最小转义） */
export function serializeCookie(
  name: string,
  value: string,
  options: CookieOptions = {},
): string {
  const parts = [`${name}=${encodeURIComponent(value)}`];
  if (options.maxAge !== undefined) {
    parts.push(`Max-Age=${Math.trunc(options.maxAge)}`);
  }
  parts.push(`Path=${options.path ?? "/"}`);
  if (options.httpOnly) {
    parts.push("HttpOnly");
  }
  if (options.secure) {
    parts.push("Secure");
  }
  if (options.sameSite) {
    parts.push(`SameSite=${options.sameSite[0]!.toUpperCase()}${options.sameSite.slice(1)}`);
  }
  return parts.join("; ");
}

/** 给响应追加多条 Set-Cookie（Headers.append 保留多条） */
export function withCookies(response: Response, cookies: string[]): Response {
  for (const cookie of cookies) {
    response.headers.append("set-cookie", cookie);
  }
  return response;
}

/** 演示环境放开跨源：带凭据时回显 Origin，不能用 * */
export function corsHeaders(request: Request): Record<string, string> {
  const origin = request.headers.get("origin");
  return {
    "access-control-allow-origin": origin ?? "*",
    "access-control-allow-credentials": "true",
    "access-control-allow-methods": "GET, POST, PUT, PATCH, DELETE, OPTIONS",
    "access-control-allow-headers": "content-type, authorization",
    vary: "origin",
  };
}

export function jsonResponse(
  request: Request,
  body: unknown,
  init?: ResponseInit,
): Response {
  return Response.json(body, {
    ...init,
    headers: { ...corsHeaders(request), ...(init?.headers as Record<string, string> | undefined) },
  });
}
