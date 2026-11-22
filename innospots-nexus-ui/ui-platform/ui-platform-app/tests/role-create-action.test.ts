/* eslint-disable @typescript-eslint/no-explicit-any -- Tests inspect dynamic DSL trees. */
import { test } from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { parse } from "yaml";
import { ActionEngine, createRuntimeContext, registerBuiltinActions } from "@pactor-app/runtime";
import { pactorActions } from "../src/frontend/actions";

function createRoleActions() {
  const page = parse(readFileSync("src/backend/pages/yaml/role/role-list.yaml", "utf8"));
  let form: any;
  const walk = (value: any) => {
    if (!value || typeof value !== "object") return;
    if (value.type === "Form" && value.props?.name === "roleCreateForm") form = value;
    Object.values(value).forEach(walk);
  };
  walk(page);
  assert.ok(form);
  return form.events.onSubmit;
}

async function runCreate(denied: boolean) {
  const messages: any[] = [];
  let closed = false;
  let reloaded = false;
  const context = createRuntimeContext({
    messages: { "common.ui.not-signed-in-or-no-permission": "未登录或无权限" },
    http: { request: async () => {
      if (denied) throw new Error("[@pactor-app/runtime] http request failed: 403 Forbidden (/api/d/platform/roles)");
      return { roleId: "test" };
    } },
  });
  registerBuiltinActions(context.actions);
  context.actions.register("request.errorMessage", pactorActions["request.errorMessage"]);
  context.actions.register("message", (action) => { messages.push(action.params); return { ok: true }; });
  context.actions.register("overlay.close", () => { closed = true; return { ok: true }; });
  context.actions.unregister("reload");
  context.actions.register("reload", () => { reloaded = true; return { ok: true }; });
  const engine = new ActionEngine(context);
  await engine.executeAll(createRoleActions(), { event: { roleName: "test", roleCode: "TEST" } });
  return { messages, closed, reloaded };
}

test("permission denial displays an error and retains the form", async () => {
  const result = await runCreate(true);
  assert.equal(result.messages.length, 1);
  assert.equal(result.messages[0].messageType, "error");
  assert.equal(result.messages[0].text, "未登录或无权限");
  assert.equal(result.closed, false);
  assert.equal(result.reloaded, false);
});

test("successful creation displays success and refreshes", async () => {
  const result = await runCreate(false);
  assert.equal(result.messages[0].messageType, "success");
  assert.equal(result.closed, true);
  assert.equal(result.reloaded, true);
});
