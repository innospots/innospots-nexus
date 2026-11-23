import type { NextConfig } from "next";
import path from "node:path";

import { withShell } from "@pactor-app/shell/next";

/**
 * UI 模块以 file: tgz 安装在本工程 node_modules 内；把 Turbopack / tracing
 * 的根提升到 ui-portal，保持与同级 ui-portal-app 一致的解析边界。
 */
const PORTAL_ROOT = path.resolve(process.cwd(), "..");

const nextConfig: NextConfig = withShell({
  turbopack: { root: PORTAL_ROOT },
  outputFileTracingRoot: PORTAL_ROOT,
  /** 底部左下角留给账户菜单，开发指示器移到右下 */
  devIndicators: { position: "bottom-right" },
  /** 模块包为构建产物（dist-module），仅需转译其客户端入口 */
  transpilePackages: ["@innospots/ui-portal-app"],
  /** 模块 server entry 在运行时读取后端地址（src/backend/config.ts） */
  env: {
    NEXT_PUBLIC_API_BASE_URL:
      process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8081",
  },
  /** 根路径让位给 portal 模块的工作台；认证页统一走 portal 模块 */
  async redirects() {
    return [
      { source: "/", destination: "/portal", permanent: false },
      { source: "/workspace", destination: "/portal", permanent: false },
      { source: "/login", destination: "/portal/login", permanent: false },
      { source: "/register", destination: "/portal/register", permanent: false },
      { source: "/settings", destination: "/portal/settings/profile", permanent: false },
      {
        source: "/settings/profile",
        destination: "/portal/settings/profile",
        permanent: false,
      },
      {
        source: "/settings/appearance",
        destination: "/portal/settings/appearance",
        permanent: false,
      },
      { source: "/settings/ai", destination: "/portal/settings/ai", permanent: false },
      {
        source: "/portal/settings",
        destination: "/portal/settings/profile",
        permanent: false,
      },
    ];
  },
});

export default nextConfig;
