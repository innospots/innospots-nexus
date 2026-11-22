/* eslint-disable @typescript-eslint/no-explicit-any -- Tests inspect dynamic DSL trees. */
import { test } from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { parse } from "yaml";
import type { PageDsl } from "@pactor-app/core";
import { applyActionAccess, collectActionRequests } from "../src/backend/pages/action-access";

const page = parse(readFileSync("src/backend/pages/yaml/dictionary/dictionary-type-list.yaml", "utf8")) as PageDsl;
const post = "POST:/api/d/platform/dictionary-types";
const put = "PUT:/api/d/platform/dictionary-types/${row.dictionaryTypeId}";
const putDrawer = "PUT:/api/d/platform/dictionary-types/${state.editingTypeId}";

function allNodes(value: any): any[] {
  if (!value || typeof value !== "object") return [];
  return [value, ...Object.values(value).flatMap(allNodes)];
}

test("create-only permissions retain the create drawer and hide edit actions", () => {
  const allowed = Object.fromEntries(collectActionRequests(page).map((item) => [`${item.method}:${item.url}`, false]));
  allowed[post] = true;
  const result = applyActionAccess(page, { allowed, grantGroups: ["view", "create"] }, false);
  const nodes = allNodes(result.body);
  assert.equal(nodes.some((n) => n.type === "Button" && n.props?.text?.includes("new-type:")), true);
  assert.equal(nodes.some((n) => n.type === "Button" && n.props?.text?.includes(".edit:")), false);
  assert.equal(nodes.find((n) => n.type === "Drawer" && n.when === "${state.drawerMode == 'create'}")?.events?.onOk?.length > 0, true);
  assert.equal(nodes.find((n) => n.type === "Drawer" && n.when === "${state.drawerMode == 'edit'}")?.events?.onOk, undefined);
});

test("edit-only permissions retain edit without requiring create", () => {
  const allowed = Object.fromEntries(collectActionRequests(page).map((item) => [`${item.method}:${item.url}`, false]));
  allowed[put] = true;
  allowed[putDrawer] = true;
  const result = applyActionAccess(page, { allowed, grantGroups: ["view", "update"] }, false);
  const nodes = allNodes(result.body);
  assert.equal(nodes.some((n) => n.type === "Button" && n.props?.text?.includes("new-type:")), false);
  assert.equal(nodes.some((n) => n.type === "Button" && n.props?.text?.includes(".edit:")), true);
  assert.equal(nodes.find((n) => n.type === "Drawer" && n.when === "${state.drawerMode == 'edit'}")?.events?.onOk?.length > 0, true);
  assert.equal(nodes.find((n) => n.type === "Drawer" && n.when === "${state.drawerMode == 'create'}")?.events?.onOk, undefined);
});
