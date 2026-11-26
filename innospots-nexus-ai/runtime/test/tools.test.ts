import { describe, expect, it } from "vitest";
import { mkdirSync, readFileSync, readdirSync } from "node:fs";
import { join } from "node:path";
import { buildTools } from "../src/tool/registry.js";
import { EnvSecrets } from "../src/security/secrets.js";
import { agentProfile, snapshot, tempDir } from "./helpers.js";

describe("file tools", () => {
  it("writes, reads, lists and soft-deletes within root", async () => {
    const { dir, cleanup } = tempDir();
    try {
      const root = join(dir, "workspace");
      mkdirSync(root, { recursive: true });
      const agent = agentProfile({
        fileRoot: "workspace", fileAccess: "read_write", allowDelete: true,
        tools: [], skills: [],
      });
      const snap = snapshot({
        agents: new Map([["default", agent]]),
        fileRoots: new Map([["workspace", { name: "workspace", path: root, enabled: true, source: "test" }]]),
      });
      const tools = buildTools({
        snapshot: snap, profile: agent, fileRoot: snap.fileRoots.get("workspace"),
        dataDir: dir, secrets: new EnvSecrets(dir), sessionId: "s1", runId: "r1",
      });
      const byName = Object.fromEntries(tools.map((tool) => [tool.name, tool]));
      await byName.file_write.execute("1", { path: "a.txt", content: "hello", overwrite: true });
      expect(readFileSync(join(root, "a.txt"), "utf8")).toBe("hello");
      const read = await byName.file_read.execute("2", { path: "a.txt" });
      expect((read.content[0] as any).text).toBe("hello");
      const list = await byName.file_list.execute("3", { path: "." });
      expect((list.content[0] as any).text).toContain("a.txt");
      await byName.file_delete.execute("4", { path: "a.txt" });
      expect(() => readFileSync(join(root, "a.txt"))).toThrow();
      const audit = readFileSync(join(dir, "audit", readDirName(dir)), "utf8");
      expect(audit).toContain("file_delete");
    } finally {
      cleanup();
    }
  });
});

function readDirName(dir: string): string {
  return readdirSync(join(dir, "audit"))[0];
}
