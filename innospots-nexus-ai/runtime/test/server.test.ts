import { afterEach, describe, expect, it } from "vitest";
import type { Server } from "node:http";
import { createRuntimeServer } from "../src/server/http.js";
import { SessionStore } from "../src/session/store.js";
import { RunManager } from "../src/agent/run-manager.js";
import { ModelRegistry } from "../src/model/registry.js";
import { EnvSecrets } from "../src/security/secrets.js";
import { agentProfile, fauxModel, snapshot, tempDir } from "./helpers.js";

let server: Server | undefined;
let cleanup: (() => void) | undefined;

afterEach(() => {
  server?.close();
  cleanup?.();
});

describe("http server", () => {
  it("serves health, agents and session flow", async () => {
    const tmp = tempDir();
    cleanup = tmp.cleanup;
    const { models, model } = fauxModel("server answer");
    const snap = snapshot({ agents: new Map([["default", agentProfile()]]) });
    const sessions = new SessionStore(`${tmp.dir}/sessions`, `${tmp.dir}/workspace`);
    const registry = new ModelRegistry(snap, new EnvSecrets(tmp.dir), models, model);
    const runs = new RunManager({ snapshot: snap, sessions, models: registry, secrets: new EnvSecrets(tmp.dir), dataDir: tmp.dir });
    server = createRuntimeServer({
      snapshot: snap, sessions, runs, dataDir: tmp.dir,
      version: "0.1.0-test", startedAt: Date.now(), configVersion: "sha256:test",
    });
    await new Promise<void>((resolve) => server!.listen(0, "127.0.0.1", resolve));
    const address = server.address();
    const port = typeof address === "object" && address ? address.port : 0;
    const base = `http://127.0.0.1:${port}`;

    expect((await fetch(`${base}/v1/health`)).status).toBe(200);
    const agents = await (await fetch(`${base}/v1/agents`)).json() as any;
    expect(agents.items[0].name).toBe("default");

    const created = await fetch(`${base}/v1/sessions`, {
      method: "POST",
      headers: { "content-type": "application/json", "x-nexus-user": "u1" },
      body: JSON.stringify({ agent: "default", title: "t" }),
    });
    expect(created.status).toBe(201);
    const session = await created.json() as any;

    const streamed = await fetch(`${base}/v1/sessions/${session.id}/messages`, {
      method: "POST",
      headers: { "content-type": "application/json", accept: "text/event-stream", "x-nexus-user": "u1" },
      body: JSON.stringify({ input: "hello" }),
    });
    expect(streamed.status).toBe(200);
    const text = await streamed.text();
    expect(text).toContain("event: text_delta");
    expect(text).toContain("event: run_completed");
    const deltas = [...text.matchAll(/"delta":"([^"]*)"/g)].map((m) => m[1]).join("");
    expect(deltas).toBe("server answer");
  });
});
