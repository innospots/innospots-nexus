"use client";

/** 当前门户挂载路径下的登录页（模块内 /portal/login，独立开发 /login）。 */
export function portalLoginPath(): string {
  const path = window.location.pathname;
  return path === "/portal" || path.startsWith("/portal/")
    ? "/portal/login"
    : "/login";
}

/** 登出并整页跳转登录页（确保会话 cookie 变更后重新拉取 sitemap）。 */
export async function logoutAndRedirect(): Promise<void> {
  const loginPath = portalLoginPath();
  try {
    await fetch("/api/auth/logout", {
      method: "POST",
      credentials: "include",
    });
  } finally {
    window.location.assign(loginPath);
  }
}
