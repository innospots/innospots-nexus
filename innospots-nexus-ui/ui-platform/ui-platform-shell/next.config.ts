import type { NextConfig } from "next";
import path from "node:path";

import { withShell } from "@pactor-app/shell/next";

/**
 * UI 模块以 file: tgz 安装在本工程 node_modules 内；把 Turbopack / tracing
 * 的根提升到 ui-platform，保持与同级 ui-platform-app 一致的解析边界。
 */
const PLATFORM_ROOT = path.resolve(process.cwd(), "..");

const nextConfig: NextConfig = withShell({
  turbopack: { root: PLATFORM_ROOT },
  outputFileTracingRoot: PLATFORM_ROOT,
  /** 模块包为构建产物（dist-module），仅需转译其客户端入口 */
  transpilePackages: ["@innospots/ui-platform-app"],
  /** 模块 server entry 在运行时读取后端地址（src/backend/config.ts） */
  env: {
    NEXT_PUBLIC_API_BASE_URL:
      process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8082",
  },
  /** 根路径让位给 console 模块的工作台 */
  async redirects() {
    return [
      { source: "/", destination: "/console", permanent: false },
      { source: "/workspace", destination: "/console", permanent: false },
    ];
  },
});

export default nextConfig;
