import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { test } from "node:test";
import { fileURLToPath } from "node:url";

import { parse } from "yaml";

const testDirectory = dirname(fileURLToPath(import.meta.url));
const yamlDirectory = resolve(testDirectory, "../src/backend/pages/yaml");

const operationListPages = [
  "role/role-list.yaml",
  "organization/tenant-list.yaml",
  "platform-user/platform-user-list.yaml",
  "platform-user/platform-invite-list.yaml",
  "platform-user/platform-access-request-list.yaml",
];

type DslNode = {
  type?: string;
  props?: Record<string, unknown>;
  events?: Record<string, unknown>;
  children?: DslNode[];
};

function findNode(node: DslNode, type: string): DslNode | undefined {
  if (node.type === type) {
    return node;
  }
  for (const child of node.children ?? []) {
    const found = findNode(child, type);
    if (found) {
      return found;
    }
  }
  return undefined;
}

for (const pagePath of operationListPages) {
  test(`${pagePath} uses server pagination without a separate count label`, async () => {
    const document = parse(await readFile(resolve(yamlDirectory, pagePath), "utf8")) as {
      body: DslNode;
    };
    const table = findNode(document.body, "Table");
    const pagination = findNode(document.body, "Pagination");

    assert.ok(table, "Table must exist");
    assert.equal(table.props?.pagination, false);
    assert.ok(pagination, "Pagination must exist");
    assert.match(String(pagination.props?.total), /\.total/);
    assert.match(String(pagination.props?.page), /state\.pageNo/);
    assert.match(String(pagination.props?.pageSize), /state\.pageSize/);
    assert.ok(pagination.events?.onChange);
    assert.ok(pagination.events?.onPageSizeChange);
  });
}
