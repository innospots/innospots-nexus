import { mkdtempSync, mkdirSync, writeFileSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { createModels } from "@earendil-works/pi-ai";
import { fauxAssistantMessage, fauxProvider } from "@earendil-works/pi-ai/providers/faux";
import { DEFAULT_RUNTIME, type AgentProfile, type ConfigSnapshot } from "../src/config/loader.js";
import { EnvSecrets } from "../src/security/secrets.js";

export function tempDir(): { dir: string; cleanup: () => void } {
  const dir = mkdtempSync(join(tmpdir(), "nexus-ai-test-"));
  return { dir, cleanup: () => rmSync(dir, { recursive: true, force: true }) };
}

export function writeConfig(dir: string, files: Record<string, string>): void {
  for (const [name, content] of Object.entries(files)) {
    const file = join(dir, name);
    mkdirSync(join(file, ".."), { recursive: true });
    writeFileSync(file, content);
  }
}

export function agentProfile(overrides: Partial<AgentProfile> = {}): AgentProfile {
  const prompt = overrides.prompt ?? "You are a test assistant.";
  return {
    name: "default",
    displayName: "Default",
    model: "test-model",
    thinkingLevel: "off",
    toolExecution: "parallel",
    skills: [],
    tools: [],
    mcpServers: [],
    fileAccess: "none",
    allowDelete: false,
    enabled: true,
    source: "test",
    prompt,
    promptHash: "sha256:test",
    profileHash: "sha256:profile",
    ...overrides,
    promptVariables: overrides.promptVariables ?? {},
  };
}

export function snapshot(overrides: Partial<ConfigSnapshot> = {}): ConfigSnapshot {
  const agent = agentProfile();
  return {
    version: "sha256:config",
    runtime: { ...DEFAULT_RUNTIME, maxTurns: 2, maxToolCalls: 2 },
    providers: new Map([["test-provider", {
      name: "test-provider", type: "openai", headers: {}, enabled: true, source: "test",
    }]]),
    models: new Map([["test-model", {
      name: "test-model", provider: "test-provider", modelId: "test-model",
      capabilities: ["text"], enabled: true, source: "test",
    }]]),
    promptTemplates: new Map(),
    skills: new Map(),
    agents: new Map([[agent.name, agent]]),
    tools: new Map(),
    mcpServers: new Map(),
    fileRoots: new Map(),
    sources: ["test"],
    ...overrides,
  };
}

export function fauxModel(response = "hello") {
  const faux = fauxProvider({ models: [{ id: "test-model" }] });
  const models = createModels();
  models.setProvider(faux.provider);
  const model = faux.getModel("test-model")!;
  faux.setResponses([fauxAssistantMessage(response)]);
  return { faux, models, model };
}

export function secrets(dir: string): EnvSecrets {
  return new EnvSecrets(dir);
}
