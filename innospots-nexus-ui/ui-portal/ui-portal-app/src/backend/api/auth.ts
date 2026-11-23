/**
 * 租户门户认证 BFF。
 *
 * 浏览器只与同源 `/api/auth/*` 交互；本层转发到 spring-portal 的
 * `/tenant/auth/*`，并把 access / refresh token 写入 httpOnly Cookie。
 */
import { serverBaseUrl } from "@/backend/config";
import {
  ACCESS_TOKEN_COOKIE,
  PORTAL_LOGIN_COOKIE,
  PORTAL_NAME_COOKIE,
  REFRESH_TOKEN_COOKIE,
  readAccessToken,
  resolveSessionUser,
} from "@/backend/auth/accounts";

import {
  jsonResponse,
  requestCookieReader,
  serializeCookie,
  withCookies,
} from "./http";

type NexusEnvelope<T> = {
  success?: boolean;
  code?: string | number;
  message?: string;
  data?: T;
};

type AuthTokenVo = {
  accessToken?: string;
  refreshToken?: string;
  realm?: string;
  tokenType?: string;
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

function sessionCookies(
  token: AuthTokenVo,
  login: string,
  displayName?: string,
): string[] {
  const secure = isProduction();
  const base = { httpOnly: true, sameSite: "lax" as const, secure };
  const cookies = [serializeCookie(ACCESS_TOKEN_COOKIE, token.accessToken ?? "", base)];
  if (token.refreshToken) {
    cookies.push(
      serializeCookie(REFRESH_TOKEN_COOKIE, token.refreshToken, base),
    );
  }
  cookies.push(serializeCookie(PORTAL_LOGIN_COOKIE, login, base));
  if (displayName && displayName.trim().length > 0) {
    cookies.push(serializeCookie(PORTAL_NAME_COOKIE, displayName.trim(), base));
  }
  return cookies;
}

function clearedSessionCookies(): string[] {
  return [
    ACCESS_TOKEN_COOKIE,
    REFRESH_TOKEN_COOKIE,
    PORTAL_LOGIN_COOKIE,
    PORTAL_NAME_COOKIE,
  ].map((name) =>
    serializeCookie(name, "", { httpOnly: true, sameSite: "lax", maxAge: 0 }),
  );
}

async function upstreamJson<T>(
  path: string,
  init: RequestInit,
): Promise<{ status: number; envelope: NexusEnvelope<T> | null; raw: unknown }> {
  const response = await fetch(`${serverBaseUrl}${path}`, {
    ...init,
    cache: "no-store",
  });
  const raw = await response.json().catch(() => null);
  return { status: response.status, envelope: raw as NexusEnvelope<T> | null, raw };
}

type ProfileVo = {
  userName?: string;
  displayName?: string;
};

/** 登录后读取档案，用于导航 / 设置页展示显示名；失败时回退登录名。 */
async function fetchProfile(accessToken: string): Promise<ProfileVo | null> {
  try {
    const { status, envelope } = await upstreamJson<ProfileVo>(
      "/tenant/auth/profile",
      {
        method: "GET",
        headers: { authorization: `Bearer ${accessToken}` },
      },
    );
    return status < 400 && envelope && isSuccess(envelope)
      ? (envelope.data ?? null)
      : null;
  } catch {
    return null;
  }
}

/** 调用认证域的密码登录端点（注册成功后自动登录复用）。 */
function upstreamLogin(
  login: string,
  encryptedPassword: string,
  captchaClientKey = "",
  captchaCode = "",
): Promise<{ status: number; envelope: NexusEnvelope<AuthTokenVo> | null; raw: unknown }> {
  return upstreamJson<AuthTokenVo>("/tenant/auth/login", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({
      login,
      encryptedPassword,
      captchaClientKey,
      captchaCode,
    }),
  });
}

function failureResponse(
  request: Request,
  status: number,
  envelope: NexusEnvelope<unknown> | null,
  raw: unknown,
  fallback: string,
): Response {
  const message =
    envelope?.message ??
    (typeof raw === "object" && raw && "message" in raw
      ? String((raw as { message?: string }).message)
      : `${fallback}（HTTP ${status}）`);
  return jsonResponse(
    request,
    { success: false, code: envelope?.code ?? status, message },
    { status: status >= 400 ? status : 401 },
  );
}

/** 租户域密码登录。 */
export async function portalLogin(request: Request): Promise<Response> {
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
      { success: false, code: "BAD_REQUEST", message: "请填写登录名与密码" },
      { status: 400 },
    );
  }

  let upstream: Awaited<ReturnType<typeof upstreamJson<AuthTokenVo>>>;
  try {
    upstream = await upstreamLogin(
      login,
      body.encryptedPassword,
      body.captchaClientKey,
      body.captchaCode,
    );
  } catch {
    return jsonResponse(
      request,
      { success: false, code: "UPSTREAM", message: "无法连接认证服务" },
      { status: 502 },
    );
  }

  const { status, envelope, raw } = upstream;
  if (status >= 400 || !envelope || !isSuccess(envelope)) {
    return failureResponse(request, status, envelope, raw, "登录失败");
  }
  const token = envelope.data;
  const accessToken = token?.accessToken?.trim();
  if (!accessToken) {
    return jsonResponse(
      request,
      { success: false, code: "UPSTREAM", message: "登录响应缺少 accessToken" },
      { status: 502 },
    );
  }

  const response = jsonResponse(request, {
    success: true,
    code: "OK",
    message: "OK",
    data: { userName: login },
  });
  const profile = await fetchProfile(accessToken);
  const displayName = profile?.displayName?.trim() || undefined;
  return withCookies(response, sessionCookies(token!, login, displayName));
}

/** 租户域身份注册（成功后直接登录）。 */
export async function portalRegister(request: Request): Promise<Response> {
  const body = (await request.json().catch(() => ({}))) as {
    userName?: string;
    displayName?: string;
    email?: string;
    mobile?: string;
    encryptedPassword?: string;
    region?: string;
    timeZone?: string;
    language?: string;
  };
  const userName = body.userName?.trim() ?? "";
  if (!userName || !body.encryptedPassword) {
    return jsonResponse(
      request,
      { success: false, code: "BAD_REQUEST", message: "请填写登录名与密码" },
      { status: 400 },
    );
  }

  let registration: Awaited<ReturnType<typeof upstreamJson<ProfileVo>>>;
  try {
    registration = await upstreamJson<ProfileVo>("/tenant/registration/register", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        userName,
        displayName: body.displayName?.trim() || userName,
        email: body.email?.trim() || null,
        mobile: body.mobile?.trim() || null,
        region: body.region?.trim() || "CN",
        timeZone: body.timeZone?.trim() || "Asia/Shanghai",
        language: body.language?.trim() || "zh-CN",
        encryptedPassword: body.encryptedPassword,
      }),
    });
  } catch {
    return jsonResponse(
      request,
      { success: false, code: "UPSTREAM", message: "无法连接注册服务" },
      { status: 502 },
    );
  }

  if (
    registration.status >= 400 ||
    !registration.envelope ||
    !isSuccess(registration.envelope)
  ) {
    return failureResponse(
      request,
      registration.status,
      registration.envelope,
      registration.raw,
      "注册失败",
    );
  }

  const displayName =
    registration.envelope.data?.displayName?.trim() ||
    body.displayName?.trim() ||
    userName;

  // 注册领域只创建身份；自动登录由认证域完成。
  let upstream: Awaited<ReturnType<typeof upstreamJson<AuthTokenVo>>>;
  try {
    upstream = await upstreamLogin(userName, body.encryptedPassword);
  } catch {
    return jsonResponse(
      request,
      { success: false, code: "UPSTREAM", message: "注册成功，但无法连接认证服务" },
      { status: 502 },
    );
  }

  const { status, envelope, raw } = upstream;
  if (status >= 400 || !envelope || !isSuccess(envelope)) {
    return failureResponse(
      request,
      status,
      envelope,
      raw,
      "注册成功，但自动登录失败，请手动登录",
    );
  }
  const token = envelope.data;
  if (!token?.accessToken) {
    return jsonResponse(
      request,
      { success: false, code: "UPSTREAM", message: "登录响应缺少 accessToken" },
      { status: 502 },
    );
  }

  const response = jsonResponse(request, {
    success: true,
    code: "OK",
    message: "OK",
    data: { userName, displayName },
  });
  return withCookies(response, sessionCookies(token, userName, displayName));
}

/** 登出：通知后端清理会话，并清除本地 Cookie。 */
export async function portalLogout(request: Request): Promise<Response> {
  const token = readAccessToken(requestCookieReader(request));
  if (token) {
    try {
      await fetch(`${serverBaseUrl}/tenant/auth/logout`, {
        method: "POST",
        headers: { authorization: `Bearer ${token}` },
        cache: "no-store",
      });
    } catch {
      // 后端不可达也要完成本地登出
    }
  }
  const response = jsonResponse(request, { ok: true });
  return withCookies(response, clearedSessionCookies());
}

/** 当前会话（供设置页与导航展示）。 */
export async function portalSession(request: Request): Promise<Response> {
  const user = resolveSessionUser(requestCookieReader(request));
  if (!user) {
    return jsonResponse(request, { authenticated: false });
  }
  return jsonResponse(request, {
    authenticated: true,
    user: {
      userName: user.username,
      displayName: user.name,
    },
  });
}

/** 代理 RSA 公钥（前端加密密码用）。 */
export async function portalRsaPublicKey(request: Request): Promise<Response> {
  try {
    const response = await fetch(`${serverBaseUrl}/tenant/auth/rsa-public-key`, {
      cache: "no-store",
    });
    const raw = await response.json().catch(() => null);
    return jsonResponse(request, raw, { status: response.status });
  } catch {
    return jsonResponse(
      request,
      { success: false, code: "UPSTREAM", message: "无法连接认证服务" },
      { status: 502 },
    );
  }
}
