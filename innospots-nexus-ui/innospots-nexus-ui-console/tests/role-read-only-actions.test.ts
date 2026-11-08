import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { test } from "node:test";
import { fileURLToPath } from "node:url";

import { parse } from "yaml";

const testDirectory = dirname(fileURLToPath(import.meta.url));
const listPage = resolve(testDirectory, "../src/backend/pages/yaml/role/role-list.yaml");
const detailPage = resolve(testDirectory, "../src/backend/pages/yaml/role/role-detail-basic.yaml");

type DslNode = {
  type?: string;
  when?: string;
  props?: { text?: unknown };
  children?: DslNode[];
  [key: string]: unknown;
};

function buttons(root: DslNode): DslNode[] {
  const found: DslNode[] = [];
  const walk = (value: unknown) => {
    if (!value || typeof value !== "object") return;
    if ((value as DslNode).type === "Button") found.push(value as DslNode);
    for (const child of Object.values(value)) {
      if (Array.isArray(child)) child.forEach(walk);
      else walk(child);
    }
  };
  walk(root);
  return found;
}

function findButton(root: DslNode, textFragment: string): DslNode {
  const match = buttons(root).find((button) =>
    String(button.props?.text ?? "").includes(textFragment),
  );
  assert.ok(match, `button containing ${textFragment} must exist`);
  return match;
}

test("read-only role hides destructive actions in the role list", async () => {
  const document = parse(await readFile(listPage, "utf8")) as { body: DslNode };
  const deleteButton = findButton(document.body, "role.role-list.delete");
  const statusButton = findButton(document.body, "role.role-list.disable");
  assert.match(String(deleteButton.when), /!row\.readOnly/);
  assert.match(String(statusButton.when), /!row\.readOnly/);
});

test("read-only role hides status, edit and permission save actions", async () => {
  const document = parse(await readFile(detailPage, "utf8")) as { body: DslNode };
  const statusButton = findButton(document.body, "role-detail-basic.disable");
  const editButton = findButton(document.body, "role-detail-basic.edit");
  const saveButton = findButton(document.body, "role-detail-basic.save-full-authorization-2");
  for (const button of [statusButton, editButton, saveButton]) {
    assert.match(String(button.when), /!data\.role\.readOnly/);
  }
});
