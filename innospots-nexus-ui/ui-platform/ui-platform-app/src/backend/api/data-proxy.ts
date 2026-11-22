/**
 * 页面数据接口统一转发（纯转发）：DSL 的 http 数据源（如
 * `/api/d/platform/roles`）按相同路径、方法、查询与请求体转发到
 * `$serverBaseUrl/api/d/<parts>`，并携带会话 Cookie 与 Bearer token。
 *
 * 独立开发时由 `src/app/api/d/[...parts]/route.ts` 调用；打包为模块后
 * 由 `module/server.ts` 注册到 `/api/modules/console/api/d/*`。
 *
 * 上游不可达（连接失败）、接口不存在（404）或返回非 JSON（如命中前端
 * 兜底 HTML）时：GET 返回 `{}`，写请求返回 502，页面按空态渲染。
 */
import {
  ACCESS_TOKEN_COOKIE,
  PLATFORM_LOGIN_COOKIE,
  readPlatformAccessToken,
  resolveSessionUser,
} from "@/backend/auth/accounts";
import { serverBaseUrl } from "@/backend/config";

import {
  corsHeaders,
  requestCookieReader,
  serializeCookie,
  withCookies,
} from "./http";

function isReadMethod(method: string): boolean {
  return method === "GET" || method === "HEAD";
}

/** 无服务层接口时的空数据（仅读请求）；写请求返回明确失败，避免误报成功。 */
function emptyData(request: Request): Response {
  if (!isReadMethod(request.method)) {
    return Response.json(
      {
        success: false,
        code: "UPSTREAM_UNAVAILABLE",
        message: "后端接口不可用，请确认 API 已启动且地址配置正确",
      },
      { status: 502, headers: { "cache-control": "no-store", ...corsHeaders(request) } },
    );
  }
  return Response.json(
    {},
    { headers: { "cache-control": "no-store", ...corsHeaders(request) } },
  );
}

/** 平台匿名自助 API（登录、注册、邀请等）无需会话即可转发 */
function isAnonymousPlatformPublicPath(parts: string[]): boolean {
  return parts.length >= 2 && parts[0] === "platform" && parts[1] === "public";
}

/** 令牌失效（AIO040007）：清掉本地会话 cookie，让登录页回到匿名态 */
function isAuthTokenFailure(status: number, body: ArrayBuffer): boolean {
  if (status !== 401) return false;
  try {
    return new TextDecoder().decode(body).includes("AIO040007");
  } catch {
    return false;
  }
}

function clearAuthCookies(response: Response): Response {
  return withCookies(
    response,
    [ACCESS_TOKEN_COOKIE, PLATFORM_LOGIN_COOKIE].map((name) =>
      serializeCookie(name, "", {
        httpOnly: true,
        sameSite: "lax",
        maxAge: 0,
      }),
    ),
  );
}

/** 原样转发：同路径、同方法、同查询、同请求体，携带会话 cookie */
async function forward(
  request: Request,
  parts: string[],
  accessToken?: string,
): Promise<Response> {
  const url = new URL(request.url);
  const target = `${serverBaseUrl}/api/d/${parts.join("/")}${url.search}`;
  const headers = new Headers();
  const cookie = request.headers.get("cookie");
  if (cookie) headers.set("cookie", cookie);
  if (accessToken) {
    headers.set("authorization", `Bearer ${accessToken}`);
  }
  const contentType = request.headers.get("content-type");
  if (contentType) headers.set("content-type", contentType);
  const init: RequestInit = { method: request.method, headers };
  if (request.method !== "GET" && request.method !== "HEAD") {
    init.body = await request.text();
  }
  let upstream: globalThis.Response;
  try {
    upstream = await fetch(target, init);
  } catch {
    return emptyData(request); // 上游不可达 → 空数据
  }
  const upstreamType = upstream.headers.get("content-type") ?? "";
  const body = await upstream.arrayBuffer();
  const authFailure = isAuthTokenFailure(upstream.status, body);
  const resHeaders: Record<string, string> = {
    "cache-control": "no-store",
    ...corsHeaders(request),
  };
  if (upstream.status === 404) {
    if (upstreamType.includes("json") && body.byteLength > 0) {
      resHeaders["content-type"] = upstreamType;
      return new Response(body, { status: upstream.status, headers: resHeaders });
    }
    return emptyData(request);
  }
  if (!upstreamType.includes("json")) {
    if (upstream.status >= 400) {
      const message = new TextDecoder().decode(body).trim() || upstream.statusText;
      return Response.json(
        { code: upstream.status, message },
        {
          status: upstream.status,
          headers: { ...resHeaders, "content-type": "application/json" },
        },
      );
    }
    return emptyData(request); // 命中前端兜底 HTML 等 → 空数据
  }
  resHeaders["content-type"] = upstreamType;
  const response = new Response(body, {
    status: upstream.status,
    headers: resHeaders,
  });
  return authFailure ? clearAuthCookies(response) : response;
}

/**
 * 转发 `/api/d/*` 请求。
 *
 * @param parts `/api/d/` 之后的路径段
 */
export async function forwardDataRequest(
  request: Request,
  parts: string[],
): Promise<Response> {
  const reader = requestCookieReader(request);
  const anonymousPublic = isAnonymousPlatformPublicPath(parts);
  if (!anonymousPublic && !resolveSessionUser(reader)) {
    return Response.json(
      { error: "未登录" },
      { status: 401, headers: corsHeaders(request) },
    );
  }
  // 登录 / 注册等匿名路径不携带旧 token：否则后端会因失效 Bearer 直接 401，
  // 登录页连 RSA 公钥与验证码都无法获取。
  const accessToken = anonymousPublic
    ? undefined
    : readPlatformAccessToken(reader);
  return forward(request, parts, accessToken);
}

/** 从逻辑 URL `/api/d/...` 解析路径段 */
export function dataPartsOf(pathname: string): string[] {
  const rest = pathname.startsWith("/api/d") ? pathname.slice("/api/d".length) : pathname;
  return rest.replace(/^\//, "").split("/").filter(Boolean);
}
