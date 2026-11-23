/**
 * Next.js 开发配置（模块工程）。
 *
 * 本工程以 UI 模块交付（`pactor module build` → `dist-module/`），
 * `src/app/` 仅作为模块本地开发与调试入口（`pnpm dev`）。
 */
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const PACTOR_TRANSPILE_PACKAGES = [
  "@pactor-app/core",
  "@pactor-app/expression",
  "@pactor-app/schema",
  "@pactor-app/permission",
  "@pactor-app/resource",
  "@pactor-app/router",
  "@pactor-app/runtime",
  "@pactor-app/ui",
  "@pactor-app/app",
  "pactor-app",
];

function isLocalPactorLinked() {
  if (process.env.PACTOR_LOCAL_LINK === "1") {
    return true;
  }
  try {
    const corePath = path.join(__dirname, "node_modules/@pactor-app/core");
    return fs.lstatSync(corePath).isSymbolicLink();
  } catch {
    return false;
  }
}

const localPactorLink = isLocalPactorLinked();
/** ui-portal-app → ui-portal → innospots-nexus-ui → innospots-nexus → innospots-gen */
const linkedMonorepoRoot = path.join(__dirname, "../../../..");

/** @type {import('next').NextConfig} */
const nextConfig = {
  env: {
    NEXT_PUBLIC_API_BASE_URL:
      process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8081",
  },
  /** 底部左下角留给账户菜单，开发指示器移到右下 */
  devIndicators: { position: "bottom-right" },
  async redirects() {
    return [
      { source: "/settings", destination: "/settings/profile", permanent: false },
    ];
  },
};

if (localPactorLink) {
  nextConfig.transpilePackages = PACTOR_TRANSPILE_PACKAGES;
  nextConfig.turbopack = { root: linkedMonorepoRoot };
  nextConfig.outputFileTracingRoot = linkedMonorepoRoot;
}

export default nextConfig;
