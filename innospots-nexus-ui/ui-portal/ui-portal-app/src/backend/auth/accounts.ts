/**
 * 租户门户会话形状（sitemap / 懒加载页鉴权用）。
 * 登录经 `/api/auth/login` 写入租户域 access token cookie。
 */
export interface PortalSessionUser {
  id: string;
  username: string;
  name: string;
  roles: string[];
  permissions: string[];
}

/** 租户域登录令牌，由 BFF 写入；转发层带给后端 `Authorization: Bearer`。 */
export const ACCESS_TOKEN_COOKIE = "nexus_portal_access_token";

/** 刷新令牌，后续接入静默续期时使用。 */
export const REFRESH_TOKEN_COOKIE = "nexus_portal_refresh_token";

/** 与 access token 同生命周期，供 sitemap 展示登录名。 */
export const PORTAL_LOGIN_COOKIE = "nexus_portal_login";

/** 注册时提交的显示名；缺失时回退登录名。 */
export const PORTAL_NAME_COOKIE = "nexus_portal_display_name";

type CookieReader = {
  get: (name: string) => { value: string } | undefined;
};

/** 解析当前请求会话（租户域 access token cookie）。 */
export function resolveSessionUser(
  store: CookieReader,
): PortalSessionUser | null {
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

export function readAccessToken(store: CookieReader): string | undefined {
  const value = store.get(ACCESS_TOKEN_COOKIE)?.value?.trim();
  return value && value.length > 0 ? value : undefined;
}
