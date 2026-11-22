#!/usr/bin/env node
/**
 * 模块 API 运行时冒烟：验证 /api/modules/<moduleId>/* 分发契约与兼容层。
 * 用法：SHELL_BASE_URL=http://127.0.0.1:9020 node tools/smoke-module-api.mjs
 */
const BASE = process.env.SHELL_BASE_URL ?? "http://127.0.0.1:9020";
const MODULE = "console";
const PUBLIC_DATA = "/d/platform/public/registration/policy";
const API = `${BASE}/api/modules/${MODULE}${PUBLIC_DATA}`;

const failures = [];

function check(name, condition, detail = "") {
  if (condition) {
    console.log(`  ✓ ${name}`);
  } else {
    failures.push(`${name}${detail ? `（${detail}）` : ""}`);
    console.log(`  ✗ ${name}${detail ? `（${detail}）` : ""}`);
  }
}

async function main() {
  console.log(`[smoke] ${BASE}`);

  const data = await fetch(API);
  check("GET 匿名数据接口 → 200", data.status === 200, `status=${data.status}`);
  check(
    "data cache-control=no-store",
    data.headers.get("cache-control") === "no-store",
    data.headers.get("cache-control") ?? "missing",
  );

  const head = await fetch(API, { method: "HEAD" });
  const headText = await head.text();
  check(
    "HEAD → 200 且无响应体",
    head.status === 200 && headText === "",
    `status=${head.status}, len=${headText.length}`,
  );

  const options = await fetch(API, { method: "OPTIONS" });
  const allow = options.headers.get("allow") ?? "";
  check("OPTIONS → 204", options.status === 204, `status=${options.status}`);
  check("OPTIONS Allow 含 GET/HEAD/OPTIONS", allow.includes("GET") && allow.includes("HEAD"), allow);

  const loginGet = await fetch(`${BASE}/api/modules/${MODULE}/auth/platform-login`);
  check("平台登录仅支持 POST → 405", loginGet.status === 405, `status=${loginGet.status}`);

  const compat = await fetch(`${BASE}${PUBLIC_DATA.replace("/d", "/api/d")}`);
  check("兼容层 /api/d/* → 200", compat.status === 200, `status=${compat.status}`);

  const cases = [
    ["未知模块", `${BASE}/api/modules/nope${PUBLIC_DATA}`, 404],
    ["未知数据路径（未登录先 401）", `${BASE}/api/modules/${MODULE}/d/platform/nope`, 401],
    ["模块根路径", `${BASE}/api/modules/${MODULE}`, 404],
    ["前缀外路径", `${BASE}/api/modules/${MODULE}/api/runtime/bootstrap`, 404],
  ];
  for (const [name, url, expected] of cases) {
    const response = await fetch(url);
    check(`${name} → ${expected}`, response.status === expected, `status=${response.status}`);
  }

  const bootstrap = await fetch(`${BASE}/api/runtime/bootstrap`);
  check("/api/runtime/bootstrap 仍可用", bootstrap.status === 200);

  const loginPage = await fetch(`${BASE}/console/login`);
  check("控制台登录页可访问", loginPage.status === 200, `status=${loginPage.status}`);

  if (failures.length > 0) {
    console.error(`\n[smoke] 失败 ${failures.length} 项：\n- ${failures.join("\n- ")}`);
    process.exit(1);
  }
  console.log("\n[smoke] 全部通过");
}

main().catch((error) => {
  console.error("[smoke] 异常", error);
  process.exit(1);
});
