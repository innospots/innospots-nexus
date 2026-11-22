/**
 * Shell 会话契约。
 *
 * 控制台模块的登录页（/console/login）通过模块 BFF 写入平台 access token
 * cookie；Shell 从这里还原最小用户信息，供菜单 / 页面可见性裁剪使用。
 * 保留演示会话（/api/auth/login）作为无后端时的兜底。
 */
export const SESSION_COOKIE = "pactor_demo_session";

/** 平台登录后由 console 模块 BFF 写入（与 ui-platform-app 的 constants 对齐） */
export const ACCESS_TOKEN_COOKIE = "nexus_platform_access_token";
export const PLATFORM_LOGIN_COOKIE = "nexus_platform_login";

export interface ShellSessionUser {
  id: string;
  username: string;
  name: string;
  roles: string[];
  permissions: string[];
}

type CookieReader = {
  get: (name: string) => { value: string } | undefined;
};

/** 演示账号：admin / admin123（无后端时的兜底登录） */
const DEMO_USER: ShellSessionUser = {
  id: "u-1",
  username: "admin",
  name: "演示管理员",
  roles: ["admin"],
  permissions: ["report:view", "user:view"],
};

export function findAccount(
  username: string,
  password: string,
): ShellSessionUser | null {
  return username === DEMO_USER.username && password === "admin123"
    ? DEMO_USER
    : null;
}

/** 从平台 access token cookie 还原用户（权限明细由后端在业务接口校验） */
function findPlatformUser(store: CookieReader): ShellSessionUser | null {
  const accessToken = store.get(ACCESS_TOKEN_COOKIE)?.value?.trim();
  if (!accessToken) {
    return null;
  }
  const login = store.get(PLATFORM_LOGIN_COOKIE)?.value?.trim() || "platform";
  return {
    id: `platform-${login}`,
    username: login,
    name: login,
    roles: [],
    permissions: [],
  };
}

/** 优先平台会话，其次演示会话 */
export function resolveSessionUser(
  store: CookieReader,
): ShellSessionUser | null {
  return (
    findPlatformUser(store) ??
    findUserBySession(store.get(SESSION_COOKIE)?.value)
  );
}

/** 演示会话 cookie 值 = username（仅兜底登录使用） */
export function findUserBySession(
  cookieValue: string | undefined,
): ShellSessionUser | null {
  return cookieValue === DEMO_USER.username ? DEMO_USER : null;
}
