/**
 * 控制台会话用户形状（sitemap / 懒加载页鉴权用）。
 * 登录经 {@code /api/auth/platform-login} 写入平台 access token cookie，不再使用本地 mock 账号。
 */
export interface DemoUser {
  id: string;
  username: string;
  name: string;
  roles: string[];
  permissions: string[];
}

// ---------------------------------------------------------------------------
// 以下为早期 Pactor 演示模板数据，已对接平台认证后停用（保留注释供对照）。
// ---------------------------------------------------------------------------
//
// export const DEMO_ACCOUNTS: (DemoUser & { password: string })[] = [
//   {
//     id: "u-1",
//     username: "admin",
//     password: "pactor123",
//     name: "演示管理员",
//     roles: ["admin"],
//     permissions: ["report:view", "user:view"],
//   },
//   {
//     id: "u-2",
//     username: "guest",
//     password: "guest123",
//     name: "访客",
//     roles: ["guest"],
//     permissions: [],
//   },
// ];
//
// export const SESSION_COOKIE = "pactor_demo_session";
//
// function toDemoUser(account: (typeof DEMO_ACCOUNTS)[number]): DemoUser {
//   return {
//     id: account.id,
//     username: account.username,
//     name: account.name,
//     roles: account.roles,
//     permissions: account.permissions,
//   };
// }
//
// export function findAccount(
//   username: string,
//   password: string,
// ): DemoUser | null {
//   const hit = DEMO_ACCOUNTS.find(
//     (a) => a.username === username && a.password === password,
//   );
//   return hit ? toDemoUser(hit) : null;
// }
//
// export function findUserBySession(
//   cookieValue: string | undefined,
// ): DemoUser | null {
//   if (!cookieValue) {
//     return null;
//   }
//   const hit = DEMO_ACCOUNTS.find((a) => a.username === cookieValue);
//   return hit ? toDemoUser(hit) : null;
// }

/** 平台登录后由 BFF 写入，转发层带给后端 {@code Authorization: Bearer}。 */
export const ACCESS_TOKEN_COOKIE = "nexus_platform_access_token";

/** 与 access token 同生命周期，供 sitemap 展示登录名。 */
export const PLATFORM_LOGIN_COOKIE = "nexus_platform_login";

/** 历史演示会话 cookie 名；登出时清理残留。 */
export const LEGACY_DEMO_SESSION_COOKIE = "pactor_demo_session";

/**
 * 菜单裁剪用占位权限：真实权限以后端令牌 / 接口为准；
 * 当前 sitemap.yaml 仅少量页声明 permission，登录后即视为可访问需登录页。
 */
const PLATFORM_UI_PERMISSIONS = [
  "report:view",
  "user:view",
] as const;

type CookieReader = {
  get: (name: string) => { value: string } | undefined;
};

function platformUserFromCookies(store: CookieReader): DemoUser | null {
  const accessToken = store.get(ACCESS_TOKEN_COOKIE)?.value?.trim();
  if (!accessToken) {
    return null;
  }
  const login = store.get(PLATFORM_LOGIN_COOKIE)?.value?.trim() || "platform";
  return {
    id: `platform-${login}`,
    username: login,
    name: login,
    roles: ["platform-admin"],
    permissions: [...PLATFORM_UI_PERMISSIONS],
  };
}

/** 解析当前请求会话（平台 access token cookie）。 */
export function resolveSessionUser(store: CookieReader): DemoUser | null {
  return platformUserFromCookies(store);
}

export function readPlatformAccessToken(store: CookieReader): string | undefined {
  const value = store.get(ACCESS_TOKEN_COOKIE)?.value?.trim();
  return value && value.length > 0 ? value : undefined;
}
