import { describe, expect, it } from "vitest";
import { SessionStore } from "../src/session/store.js";
import { RunManager } from "../src/agent/run-manager.js";
import { ModelRegistry } from "../src/model/registry.js";
import { EnvSecrets } from "../src/security/secrets.js";
import { agentProfile, fauxModel, snapshot, tempDir } from "./helpers.js";

describe("agent run", () => {
  it("runs with faux model and appends messages to session JSONL", async () => {
    const { dir, cleanup } = tempDir();
    try {
      const { models, model } = fauxModel("hello from faux");
      const snap = snapshot({ agents: new Map([["default", agentProfile()]]) });
      const sessions = new SessionStore(`${dir}/sessions`, `${dir}/workspace`);
      const registry = new ModelRegistry(snap, new EnvSecrets(dir), models, model);
      const runs = new RunManager({ snapshot: snap, sessions, models: registry, secrets: new EnvSecrets(dir), dataDir: dir });
      const session = sessions.create({
        owner: "u1", agent: "default", profileHash: "sha256:profile", promptHash: "sha256:test",
        prompt: "You are a test assistant.", skills: [], tools: [], mcpServers: [],
        fileAccess: "none", allowDelete: false,
      });
      const events: string[] = [];
      const result = await runs.run(session.id, "u1", "hi", (event) => { events.push(event.event); });
      expect(result.status).toBe("succeeded");
      expect(events).toContain("run_started");
      expect(events).toContain("run_completed");
      expect(events).toContain("text_delta");
      const opened = await sessions.find(session.id, "u1");
      expect(sessions.history(opened!).some((m) => m.role === "assistant")).toBe(true);
    } finally {
      cleanup();
    }
  });
});
