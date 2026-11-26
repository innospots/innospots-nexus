import { describe, expect, it } from "vitest";
import { FileConfigSource, loadConfig, StaticConfigSource } from "../src/config/loader.js";
import { tempDir, writeConfig } from "./helpers.js";

describe("config loader", () => {
  it("loads multi-file config and composes skills", async () => {
    const { dir, cleanup } = tempDir();
    try {
      writeConfig(dir, {
        "runtime.yaml": "apiVersion: nexus.ai/v1\nkind: Runtime\nmetadata: {name: default}\nspec: {maxTurns: 3}\n",
        "conf.d/10-provider.yaml": "apiVersion: nexus.ai/v1\nkind: Provider\nmetadata: {name: p}\nspec: {type: openai-compatible, baseUrl: http://localhost:1/v1}\n",
        "conf.d/20-model.yaml": "apiVersion: nexus.ai/v1\nkind: Model\nmetadata: {name: m}\nspec: {provider: p, modelId: x}\n",
        "conf.d/25-prompt.yaml": "apiVersion: nexus.ai/v1\nkind: PromptTemplate\nmetadata: {name: base}\nspec:\n  content: \"You are ${role}.\"\n  variables: {role: {required: true}}\n",
        "conf.d/30-skill.yaml": "apiVersion: nexus.ai/v1\nkind: Skill\nmetadata: {name: s}\nspec: {path: ./skills/s, include: [SKILL.md]}\n",
        "skills/s/SKILL.md": "---\nname: s\ndescription: sample\n---\nUse the sample skill.\n",
        "conf.d/40-agent.yaml": "apiVersion: nexus.ai/v1\nkind: AgentProfile\nmetadata: {name: a}\nspec:\n  model: m\n  promptTemplate: base\n  promptVariables: {role: tester}\n  skills: [s]\n",
      });
      const snapshot = await loadConfig({ configDir: dir, allowSymlinks: false });
      expect(snapshot.runtime.maxTurns).toBe(3);
      expect(snapshot.agents.get("a")?.prompt).toContain("You are tester.");
      expect(snapshot.agents.get("a")?.prompt).toContain("Use the sample skill.");
      expect(snapshot.version).toMatch(/^sha256:/);
    } finally {
      cleanup();
    }
  });

  it("lets higher-priority ConfigSource override local files", async () => {
    const { dir, cleanup } = tempDir();
    try {
      writeConfig(dir, {
        "runtime.yaml": "kind: Runtime\nmetadata: {name: default}\nspec: {}\n",
        "conf.d/provider.yaml": "kind: Provider\nmetadata: {name: p}\nspec: {type: openai, baseUrl: http://local}\n",
        "conf.d/model.yaml": "kind: Model\nmetadata: {name: m}\nspec: {provider: p, modelId: x}\n",
      });
      const high = new StaticConfigSource("remote", [{
        kind: "Provider", metadata: { name: "p" },
        spec: { type: "openai", baseUrl: "http://remote" },
      }], 200);
      const snapshot = await loadConfig({
        configDir: dir, allowSymlinks: false,
        sources: [new FileConfigSource(dir), high],
      });
      expect(snapshot.providers.get("p")?.baseUrl).toBe("http://remote");
    } finally {
      cleanup();
    }
  });

  it("rejects duplicate resources in one layer", async () => {    const { dir, cleanup } = tempDir();
    try {
      writeConfig(dir, {
        "runtime.yaml": "kind: Runtime\nmetadata: {name: default}\nspec: {}\n",
        "conf.d/a.yaml": "kind: Provider\nmetadata: {name: p}\nspec: {type: openai}\n",
        "conf.d/b.yaml": "kind: Provider\nmetadata: {name: p}\nspec: {type: openai}\n",
      });
      await expect(loadConfig({ configDir: dir, allowSymlinks: false })).rejects.toThrow(/duplicate/);
    } finally {
      cleanup();
    }
  });
});
