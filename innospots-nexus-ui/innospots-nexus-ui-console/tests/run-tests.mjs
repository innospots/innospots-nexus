#!/usr/bin/env node
/**
 * 运行 `tests/*.test.ts`（node:test），无需额外测试框架。
 *
 * 测试源码使用 `@/...` 路径别名并依赖 Next 的服务端模块，直接用 node 运行无法解析；
 * 这里用 esbuild 打包到临时目录 `.test-dist/`，同时为 `server-only` / `next/headers`
 * 提供最小桩实现，最后用 `node --test` 执行。
 */
import { spawn } from "node:child_process";
import {
  mkdirSync,
  readdirSync,
  rmSync,
  writeFileSync,
} from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

import { build } from "esbuild";

const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..");
const OUT_DIR = join(ROOT, ".test-dist");
const STUB_DIR = join(OUT_DIR, "_stubs");

rmSync(OUT_DIR, { recursive: true, force: true });
mkdirSync(STUB_DIR, { recursive: true });
writeFileSync(join(STUB_DIR, "server-only.mjs"), "export {};\n");
writeFileSync(
  join(STUB_DIR, "next-headers.mjs"),
  [
    "export const cookies = async () => ({ get: () => undefined, set: () => undefined });",
    "export const headers = async () => new Headers();",
    "export default {};",
    "",
  ].join("\n"),
);

const testFiles = readdirSync(join(ROOT, "tests"))
  .filter((file) => file.endsWith(".test.ts"))
  .sort();
if (testFiles.length === 0) {
  console.error("tests/ 下没有找到 *.test.ts");
  process.exit(1);
}

const bundles = [];
for (const file of testFiles) {
  const outfile = join(OUT_DIR, file.replace(/\.ts$/, ".mjs"));
  await build({
    entryPoints: [join(ROOT, "tests", file)],
    bundle: true,
    platform: "node",
    format: "esm",
    target: "node22",
    packages: "external",
    alias: {
      "@": join(ROOT, "src"),
      "server-only": join(STUB_DIR, "server-only.mjs"),
      "next/headers": join(STUB_DIR, "next-headers.mjs"),
    },
    outfile,
    logLevel: "warning",
  });
  bundles.push(outfile);
}

const child = spawn(process.execPath, ["--test", ...bundles], {
  cwd: ROOT,
  stdio: "inherit",
  // sitemap.ts 在非 production 下会创建 fs.watch，node --test 会等待句柄导致不退出。
  env: { ...process.env, NODE_ENV: "production" },
});
child.on("exit", (code) => {
  rmSync(OUT_DIR, { recursive: true, force: true });
  process.exit(code ?? 1);
});
