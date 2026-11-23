/**
 * Shell 会话契约。
 *
 * 门户模块的登录 BFF（/api/auth/login、/api/auth/register）写入
 * access token cookie；Shell 从这里还原最小用户信息，供菜单 / 页面可见性裁剪。
 */
export const ACCESS_TOKEN_COOKIE = "nexus_portal_access_token";

/** 与 access token 同生命周期，供 sitemap 展示登录名。 */
export const PORTAL_LOGIN_COOKIE = "nexus_portal_login";

/** 注册 / 登录时写入的显示名。 */
export const PORTAL_NAME_COOKIE = "nexus_portal_display_name";

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

/** 从门户 access token cookie 还原用户（权限明细由后端在业务接口校验）。 */
export function resolveSessionUser(
  store: CookieReader,
): ShellSessionUser | null {
  const accessToken = store.get(ACCESS_TOKEN_COOKIE)?.value?.trim();
  if (!accessToken) {
    return null;
  }
  const login = store.get(PORTAL_LOGIN_COOKIE)?.value?.trim() || "portal";
  const displayName = store.get(PORTAL_NAME_COOKIE)?.value?.trim();
  return {
    id: `portal-${login}`,
    username: login,
    name: displayName && displayName.length > 0 ? displayName : login,
    roles: [],
    permissions: [],
  };
}
