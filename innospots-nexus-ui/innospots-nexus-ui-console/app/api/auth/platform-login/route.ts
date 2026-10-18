import { NextResponse } from "next/server";

import { serverBaseUrl } from "@/pactor/config";
import {
  ACCESS_TOKEN_COOKIE,
  PLATFORM_LOGIN_COOKIE,
} from "@/server/accounts";
import { corsHeaders, corsPreflight } from "@/server/cors";

type NexusEnvelope<T> = {
  success?: boolean;
  code?: string | number;
  message?: string;
  data?: T;
};

type AuthTokenVo = {
  accessToken?: string;
  refreshToken?: string;
};

function isSuccess(envelope: NexusEnvelope<unknown>): boolean {
  if (envelope.success === false) {
    return false;
  }
  const code = envelope.code;
  if (code === undefined) {
    return true;
  }
  return code === 0 || code === "0" || code === "OK";
}

/** 代理平台登录并写入 httpOnly 会话 cookie，供 sitemap /api/d 转发鉴权。 */
export async function POST(request: Request) {
  const body = (await request.json().catch(() => ({}))) as {
    login?: string;
    encryptedPassword?: string;
    captchaClientKey?: string;
    captchaCode?: string;
  };
  const login = body.login?.trim() ?? "";
  if (!login || !body.encryptedPassword) {
    return NextResponse.json(
      { code: "BAD_REQUEST", message: "请填写登录名与密码" },
      { status: 400, headers: corsHeaders(request) },
    );
  }

  let upstream: globalThis.Response;
  try {
    upstream = await fetch(
      `${serverBaseUrl}/api/d/platform/public/auth/login`,
      {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({
          login,
          encryptedPassword: body.encryptedPassword,
          captchaClientKey: body.captchaClientKey ?? "",
          captchaCode: body.captchaCode ?? "",
        }),
      },
    );
  } catch {
    return NextResponse.json(
      { code: "UPSTREAM", message: "无法连接认证服务" },
      { status: 502, headers: corsHeaders(request) },
    );
  }

  const raw = await upstream.json().catch(() => null);
  const envelope = raw as NexusEnvelope<AuthTokenVo> | null;
  if (!upstream.ok || !envelope || !isSuccess(envelope)) {
    const message =
      envelope?.message ??
      (typeof raw === "object" && raw && "message" in raw
        ? String((raw as { message?: string }).message)
        : `登录失败（HTTP ${upstream.status}）`);
    return NextResponse.json(
      { code: envelope?.code ?? upstream.status, message },
      { status: upstream.status >= 400 ? upstream.status : 401, headers: corsHeaders(request) },
    );
  }

  const tokenPayload =
    envelope.data && typeof envelope.data === "object"
      ? (envelope.data as AuthTokenVo)
      : null;
  const accessToken = tokenPayload?.accessToken?.trim();
  if (!accessToken) {
    return NextResponse.json(
      { code: "UPSTREAM", message: "登录响应缺少 accessToken" },
      { status: 502, headers: corsHeaders(request) },
    );
  }

  const response = NextResponse.json(
    { success: true, code: "OK", message: "OK", data: tokenPayload },
    { headers: corsHeaders(request) },
  );
  const secure = process.env.NODE_ENV === "production";
  response.cookies.set(ACCESS_TOKEN_COOKIE, accessToken, {
    httpOnly: true,
    sameSite: "lax",
    path: "/",
    secure,
  });
  response.cookies.set(PLATFORM_LOGIN_COOKIE, login, {
    httpOnly: true,
    sameSite: "lax",
    path: "/",
    secure,
  });
  return response;
}

export const OPTIONS = corsPreflight;
