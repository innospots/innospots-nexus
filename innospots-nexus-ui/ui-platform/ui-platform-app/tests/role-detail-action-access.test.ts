import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { test } from "node:test";
import { fileURLToPath } from "node:url";

import { parse } from "yaml";

const testDirectory = dirname(fileURLToPath(import.meta.url));
const pagePath = resolve(
  testDirectory,
  "../src/backend/pages/yaml/role/role-detail-basic.yaml",
);

type AccessRequest = { method?: string; url?: string };
type DslNode = {
  type?: string;
  access?: { requests?: AccessRequest[] };
  props?: { text?: unknown };
  children?: DslNode[];
  [key: string]: unknown;
};

function walk(node: DslNode, visit: (value: DslNode) => void): void {
  visit(node);
  for (const value of Object.values(node)) {
    if (Array.isArray(value)) {
      value.forEach((child) => {
        if (child && typeof child === "object") walk(child as DslNode, visit);
      });
    } else if (value && typeof value === "object") {
      walk(value as DslNode, visit);
    }
  }
}

function findButton(document: { body: DslNode }, textKey: string): DslNode {
  let result: DslNode | undefined;
  walk(document.body, (node) => {
    if (node.type === "Button" && String(node.props?.text).includes(textKey)) {
      result = node;
    }
  });
  assert.ok(result, `Button containing ${textKey} must exist`);
  return result;
}

test("role permission catalog uses the supported endpoint", async () => {
  const document = parse(await readFile(pagePath, "utf8")) as {
    dataSources: { catalog: { request: { url: string } } };
  };

  assert.equal(
    document.dataSources.catalog.request.url,
    "/api/d/nexus/catalog/permission-catalog",
  );
});

test("role member create button declares every request required by its dialog", async () => {
  const document = parse(await readFile(pagePath, "utf8")) as { body: DslNode };
  const button = findButton(document, "role-detail-basic.add-member");
  const requests = button.access?.requests ?? [];

  assert.deepEqual(requests, [
    { method: "GET", url: "/api/d/platform/users" },
    { method: "GET", url: "/api/d/platform/roles/${params.roleId}/bindings" },
    { method: "POST", url: "/api/d/platform/roles/${params.roleId}/bindings" },
  ]);
});

test("role member remove button declares its dynamic binding request", async () => {
  const document = parse(await readFile(pagePath, "utf8")) as { body: DslNode };
  const button = findButton(document, "role-detail-basic.remove");

  assert.deepEqual(button.access?.requests, [
    {
      method: "DELETE",
      url: "/api/d/platform/roles/${params.roleId}/bindings/${row.bindingId}",
    },
  ]);
});
