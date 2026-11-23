/**
 * `/api/d/*` 统一转发：按相同路径、方法、查询与请求体转发到
 * `${serverBaseUrl}/api/d/<parts>`，并携带会话 Cookie 与 Bearer token。
 */
import {
  readAccessToken,
  resolveSessionUser,
} from "@/backend/auth/accounts";
import { serverBaseUrl } from "@/backend/config";

import {
  corsHeaders,
  jsonResponse,
  requestCookieReader,
} from "./http";

function isReadMethod(method: string): boolean {
  return method === "GET" || method === "HEAD";
}

async function forward(
  request: Request,
  parts: string[],
  accessToken?: string,
): Promise<Response> {
  const url = new URL(request.url);
  const target = `${serverBaseUrl}/api/d/${parts.join("/")}${url.search}`;
  const headers = new Headers();
  const cookie = request.headers.get("cookie");
  if (cookie) {
    headers.set("cookie", cookie);
  }
  if (accessToken) {
    headers.set("authorization", `Bearer ${accessToken}`);
  }
  const contentType = request.headers.get("content-type");
  if (contentType) {
    headers.set("content-type", contentType);
  }
  const init: RequestInit = { method: request.method, headers, cache: "no-store" };
  if (!isReadMethod(request.method)) {
    init.body = await request.text();
  }

  let upstream: globalThis.Response;
  try {
    upstream = await fetch(target, init);
  } catch {
    if (!isReadMethod(request.method)) {
      return jsonResponse(
        request,
        {
          success: false,
          code: "UPSTREAM_UNAVAILABLE",
          message: "后端接口不可用，请确认 API 已启动且地址配置正确",
        },
        { status: 502 },
      );
    }
    return Response.json({}, {
      headers: { "cache-control": "no-store", ...corsHeaders(request) },
    });
  }

  const body = await upstream.arrayBuffer();
  const response = new Response(body, {
    status: upstream.status,
    headers: {
      "cache-control": "no-store",
      "content-type":
        upstream.headers.get("content-type") ?? "application/json",
      ...corsHeaders(request),
    },
  });
  return response;
}

/** 转发 `/api/d/*` 请求；未登录直接 401。 */
export async function forwardDataRequest(
  request: Request,
  parts: string[],
): Promise<Response> {
  const reader = requestCookieReader(request);
  if (!resolveSessionUser(reader)) {
    return jsonResponse(request, { error: "未登录" }, { status: 401 });
  }
  return forward(request, parts, readAccessToken(reader));
}

/** 从逻辑 URL `/api/d/...` 解析路径段。 */
export function dataPartsOf(pathname: string): string[] {
  const rest = pathname.startsWith("/api/d")
    ? pathname.slice("/api/d".length)
    : pathname;
  return rest.replace(/^\//, "").split("/").filter(Boolean);
}
