/* eslint-disable @typescript-eslint/no-explicit-any -- Tests inspect dynamic DSL trees. */
import { test } from "node:test";
import assert from "node:assert/strict";
import type { PageDsl } from "@pactor-app/core";
import { applyActionAccess, collectActionRequests, stripAccessDeclarations } from "../src/backend/pages/action-access";

const post = { action: "request", params: { method: "POST", url: "/api/d/platform/roles" } };
const page = {
  dsl: "1.0", page: { id: "test", domain: "operation", module: "role" },
  body: { type: "Page", children: [
    { type: "Button", props: { text: "create" }, events: { onClick: [{ action: "overlay.open", params: { overlay: { type: "modal", content: { type: "Form", events: { onSubmit: [post] } } } } }] } },
    { type: "Form", events: { onSubmit: [post] }, children: [{ type: "Input", props: { name: "name" } }, { type: "Button", props: { text: "save", submit: true } }] },
    { type: "Button", props: { text: "navigate" }, events: { onClick: [{ action: "navigate", params: { to: "/" } }] } },
    { type: "RoleMemberAddPanel", access: { requests: [{ method: "POST", url: "/api/d/platform/roles" }] } },
  ] },
} as unknown as PageDsl;

test("public page keeps its components while removing server-only access declarations", () => {
  const input = { type: "Button", access: { requests: [{ method: "POST", url: "/api/public/test" }] },
    events: { onClick: [{ action: "request", params: { url: "/api/public/test" } }] } };
  const output = stripAccessDeclarations(input) as any;
  assert.equal(output.access, undefined);
  assert.equal(output.events.onClick[0].action, "request");
  assert.ok(input.access);
});

test("deduplicates nested requests and removes unauthorized actions without mutating original", () => {
  assert.deepEqual(collectActionRequests(page), [{ method: "POST", url: "/api/d/platform/roles" }]);
  const result = applyActionAccess(page, { allowed: { "POST:/api/d/platform/roles": false }, grantGroups: ["view"] }, false);
  const children = (result.body as any).children;
  assert.equal(children.length, 2);
  assert.equal(children[0].type, "Form");
  assert.equal(children[0].events.onSubmit, undefined);
  assert.equal(children[0].children[0].props.disabled, true);
  assert.equal(children[0].children[1].props.disabled, true);
  assert.equal(children[1].props.text, "navigate");
  assert.equal((page.body as any).children.length, 4);
  assert.deepEqual(result.state?.grantGroupList, ["view"]);
  assert.equal((result.state?.grantGroups as Record<string, boolean>).view, true);
});

test("allows authorized actions and fails closed when a result is missing", () => {
  const allowed = applyActionAccess(page, { allowed: { "POST:/api/d/platform/roles": true }, grantGroups: ["create"] }, false);
  assert.equal((allowed.body as any).children.length, 4);
  const failed = applyActionAccess(page, { allowed: {}, grantGroups: [] }, true);
  assert.equal((failed.body as any).children[0].type, "Alert");
  assert.equal((failed.body as any).children.some((child: any) => child.props?.text === "create"), false);
});


test("retains null data and existing visibility expressions while following named actions", () => {
  const source = {
    dsl: "1.0", page: { id: "named" }, state: { options: [null, "x"] },
    actions: { create: [post] },
    body: { type: "Page", children: [{ type: "Button", when: "${state.grantGroups.create}", events: { onClick: [{ action: "call", params: { name: "create" } }] } }] },
  } as unknown as PageDsl;
  const noAccess = applyActionAccess(source, { allowed: { "POST:/api/d/platform/roles": false }, grantGroups: ["view"] }, false);
  assert.equal((noAccess.body as any).children.length, 0);
  assert.deepEqual(noAccess.state?.options, [null, "x"]);
  const withAccess = applyActionAccess(source, { allowed: { "POST:/api/d/platform/roles": true }, grantGroups: ["create"] }, false);
  assert.equal((withAccess.body as any).children[0].when, "${state.grantGroups.create}");
  assert.equal((withAccess.state?.grantGroups as Record<string, boolean>).create, true);
});

test("keeps explicitly read-only components visible while disabling their events", () => {
  const source = {
    dsl: "1.0", page: { id: "panel" }, body: { type: "Page", children: [
      { type: "PermissionGrantPanel", access: { mode: "disable", requests: [{ method: "PUT", url: "/api/d/platform/roles/${params.roleId}/permissions" }] }, props: { className: "test" }, events: { onChange: [{ action: "setState" }] } },
      { type: "Text", access: { grantGroups: ["create"] }, props: { text: "restricted" } },
    ] },
  } as unknown as PageDsl;
  const result = applyActionAccess(source, { allowed: {}, grantGroups: ["view"] }, false);
  const children = (result.body as any).children;
  assert.equal(children.length, 1);
  assert.equal(children[0].type, "PermissionGrantPanel");
  assert.equal(children[0].props.disabled, true);
  assert.equal(children[0].props.className, "test");
  assert.deepEqual(children[0].events, {});
  assert.equal(children[0].access, undefined);
});


test("fails closed for malformed explicit requirements", () => {
  const source = { dsl: "1.0", page: { id: "invalid" }, body: { type: "Page", children: [
    { type: "Button", access: { requests: "not-a-list" }, props: { text: "bad request" } },
    { type: "Button", access: { grantGroups: "create" }, props: { text: "bad group" } },
  ] } } as unknown as PageDsl;
  assert.equal((applyActionAccess(source, { allowed: {}, grantGroups: ["create"] }, false).body as any).children.length, 0);
});


test("a trigger cannot open an embedded component that requires an absent grantGroup", () => {
  const source = { dsl: "1.0", page: { id: "nested" }, body: { type: "Page", children: [
    { type: "Button", events: { onClick: [{ action: "overlay.open", params: { overlay: { type: "modal", content: { type: "RestrictedPanel", access: { grantGroups: ["assign"] } } } } }] } },
  ] } } as unknown as PageDsl;
  assert.equal((applyActionAccess(source, { allowed: {}, grantGroups: ["view"] }, false).body as any).children.length, 0);
  assert.equal((applyActionAccess(source, { allowed: {}, grantGroups: ["assign"] }, false).body as any).children.length, 1);
});
