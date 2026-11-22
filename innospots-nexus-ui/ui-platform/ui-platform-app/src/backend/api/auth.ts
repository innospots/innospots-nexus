/**
 * 平台登录 / 登出（BFF）。
 *
 * 登录代理后端 `/api/d/platform/public/auth/login`，把 accessToken 与登录名
 * 写入 httpOnly Cookie，供 sitemap / 数据转发鉴权使用。
 */
import { serverBaseUrl } from "@/backend/config";
import {
  ACCESS_TOKEN_COOKIE,
  LEGACY_DEMO_SESSION_COOKIE,
  PLATFORM_LOGIN_COOKIE,
} from "@/backend/auth/accounts";

import { jsonResponse, serializeCookie, withCookies } from "./http";

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

function isProduction(): boolean {
  return process.env.NODE_ENV === "production";
}

/** 代理平台登录并写入 httpOnly 会话 cookie */
export async function platformLogin(request: Request): Promise<Response> {
  const body = (await request.json().catch(() => ({}))) as {
    login?: string;
    encryptedPassword?: string;
    captchaClientKey?: string;
    captchaCode?: string;
  };
  const login = body.login?.trim() ?? "";
  if (!login || !body.encryptedPassword) {
    return jsonResponse(
      request,
      { code: "BAD_REQUEST", message: "请填写登录名与密码" },
      { status: 400 },
    );
  }

  let upstream: globalThis.Response;
  try {
    upstream = await fetch(`${serverBaseUrl}/api/d/platform/public/auth/login`, {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        login,
        encryptedPassword: body.encryptedPassword,
        captchaClientKey: body.captchaClientKey ?? "",
        captchaCode: body.captchaCode ?? "",
      }),
    });
  } catch {
    return jsonResponse(
      request,
      { code: "UPSTREAM", message: "无法连接认证服务" },
      { status: 502 },
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
    return jsonResponse(
      request,
      { code: envelope?.code ?? upstream.status, message },
      { status: upstream.status >= 400 ? upstream.status : 401 },
    );
  }

  const tokenPayload =
    envelope.data && typeof envelope.data === "object"
      ? (envelope.data as AuthTokenVo)
      : null;
  const accessToken = tokenPayload?.accessToken?.trim();
  if (!accessToken) {
    return jsonResponse(
      request,
      { code: "UPSTREAM", message: "登录响应缺少 accessToken" },
      { status: 502 },
    );
  }

  const response = jsonResponse(request, {
    success: true,
    code: "OK",
    message: "OK",
    data: tokenPayload,
  });
  const secure = isProduction();
  return withCookies(response, [
    serializeCookie(ACCESS_TOKEN_COOKIE, accessToken, {
      httpOnly: true,
      sameSite: "lax",
      secure,
    }),
    serializeCookie(PLATFORM_LOGIN_COOKIE, login, {
      httpOnly: true,
      sameSite: "lax",
      secure,
    }),
  ]);
}

/** 清会话 cookie（含历史演示会话） */
export function platformLogout(request: Request): Response {
  const response = jsonResponse(request, { ok: true });
  return withCookies(
    response,
    [LEGACY_DEMO_SESSION_COOKIE, ACCESS_TOKEN_COOKIE, PLATFORM_LOGIN_COOKIE].map(
      (name) =>
        serializeCookie(name, "", {
          httpOnly: true,
          sameSite: "lax",
          maxAge: 0,
        }),
    ),
  );
}

/** 早期演示登录已停用 */
export function deprecatedDemoLogin(request: Request): Response {
  return jsonResponse(
    request,
    {
      code: "DEPRECATED",
      message: "演示登录已停用，请使用平台登录接口 /api/auth/platform-login",
    },
    { status: 410 },
  );
}
