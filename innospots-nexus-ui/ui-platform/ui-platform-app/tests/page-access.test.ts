import assert from "node:assert/strict";
import { after, test } from "node:test";

import { verifyPageAccess } from "../src/backend/pages/page-access";

const originalFetch = globalThis.fetch;
after(() => { globalThis.fetch = originalFetch; });

test("public page skips platform verification", async () => {
  let calls = 0;
  globalThis.fetch = async () => { calls++; throw new Error("unexpected fetch"); };
  const result = await verifyPageAccess({ pageId: "login", user: null });
  assert.equal(result.allowed, true);
  assert.equal(result.publicPage, true);
  assert.equal(calls, 0);
});

test("grant page submits page metadata and requests in one call", async () => {
  let calls = 0;
  globalThis.fetch = async (_input, init) => {
    calls++;
    const request = JSON.parse(String(init?.body));
    assert.equal(request.accessMode, "grant");
    assert.equal(request.domainKey, "operation");
    assert.equal(request.moduleKey, "role");
    assert.equal(request.pageKey, "platform-role-main");
    assert.deepEqual(request.requests, [{ method: "POST", url: "/api/d/platform/roles" }]);
    return Response.json({ code: "OK", data: { allowed: true,
      requestPermissions: { "POST:/api/d/platform/roles": false }, grantGroups: ["view"] } });
  };
  const result = await verifyPageAccess({ pageId: "role-list", user: null,
    requests: [{ method: "POST", url: "/api/d/platform/roles" }] });
  assert.equal(result.allowed, true);
  assert.equal(result.requestPermissions?.["POST:/api/d/platform/roles"], false);
  assert.deepEqual(result.grantGroups, ["view"]);
  assert.equal(calls, 1);
});

test("authenticated page calls backend and incomplete response is rejected", async () => {
  let calls = 0;
  globalThis.fetch = async (_input, init) => {
    calls++;
    assert.equal(JSON.parse(String(init?.body)).accessMode, "authenticated");
    return Response.json({ code: "OK", data: { allowed: true } });
  };
  const result = await verifyPageAccess({ pageId: "password-change", user: null });
  assert.equal(result.allowed, false);
  assert.equal(calls, 1);
});

test("disabled security returns the debug grant-group bypass flag", async () => {
  globalThis.fetch = async () =>
    Response.json({
      code: "OK",
      data: {
        allowed: true,
        ignoreGrantGroup: true,
        requestPermissions: {},
        grantGroups: [],
      },
    });
  const result = await verifyPageAccess({
    pageId: "role-list",
    user: null,
    requests: [{ method: "POST", url: "/api/d/platform/roles" }],
  });
  assert.equal(result.allowed, true);
  assert.equal(result.ignoreGrantGroup, true);
});

test("ignoreGrantGroup only accepts a boolean true", async () => {
  globalThis.fetch = async () =>
    Response.json({
      code: "OK",
      data: {
        allowed: true,
        ignoreGrantGroup: "true",
        requestPermissions: {},
        grantGroups: [],
      },
    });
  const result = await verifyPageAccess({ pageId: "role-list", user: null });
  assert.equal(result.ignoreGrantGroup, false);
});
