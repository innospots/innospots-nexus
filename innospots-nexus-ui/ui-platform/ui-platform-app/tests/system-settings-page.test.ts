import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { test } from "node:test";
import { fileURLToPath } from "node:url";

import { parse } from "yaml";

const testDirectory = dirname(fileURLToPath(import.meta.url));
const pagePath = resolve(
  testDirectory,
  "../src/backend/pages/yaml/settings/system-settings.yaml",
);
const actionAccessUrl = new URL(
  "../src/backend/pages/action-access.ts",
  import.meta.url,
).href;

test("system settings uses registered platform endpoints for every update", async () => {
  const source = await readFile(pagePath, "utf8");
  const document = parse(source) as {
    dataSources: {
      settings: { request: { method: string; url: string } };
    };
  };

  assert.equal(document.dataSources.settings.request.method, "GET");
  assert.equal(
    document.dataSources.settings.request.url,
    "/api/d/platform/settings/console-general",
  );
  const { collectActionRequests } = await import(actionAccessUrl) as {
    collectActionRequests: (value: unknown) => { method: string; url: string }[];
  };
  const expectedUpdates = [
    { method: "PUT", url: "/api/d/platform/settings/console-display-language" },
    { method: "PUT", url: "/api/d/platform/settings/console-site-description" },
    { method: "PUT", url: "/api/d/platform/settings/console-site-name" },
    { method: "PUT", url: "/api/d/platform/settings/console-timezone" },
    { method: "PUT", url: "/api/d/platform/settings/navigation-layout" },
  ].sort((left, right) => left.url.localeCompare(right.url));
  assert.deepEqual(
    collectActionRequests(document)
      .filter(({ method }) => method === "PUT")
      .sort((left, right) => left.url.localeCompare(right.url)),
    expectedUpdates,
  );
  assert.doesNotMatch(source, /\/api\/d\/system-settings/);
});

test("system settings no longer exposes session or default page-size controls", async () => {
  const source = await readFile(pagePath, "utf8");

  assert.doesNotMatch(source, /systemSettings\.sessionTimeout/);
  assert.doesNotMatch(source, /systemSettings\.pageSize/);
  assert.doesNotMatch(source, /common\.systemSettings\.group\.session/);
});
