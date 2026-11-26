import { lstatSync, readFileSync, readdirSync, statSync } from "node:fs";
import { dirname, join, relative, resolve } from "node:path";
import { createHash } from "node:crypto";
import { parseAllDocuments } from "yaml";

export type JsonSchema = Record<string, unknown>;

export interface RuntimeSettings {
  logLevel: "debug" | "info" | "warn" | "error";
  maxConcurrentRuns: number;
  runTimeoutSeconds: number;
  maxTurns: number;
  maxToolCalls: number;
  maxMessageBytes: number;
  maxFileReadBytes: number;
  maxFileWriteBytes: number;
  maxFileListEntries: number;
  maxFileOpsPerRun: number;
  trashRetentionDays: number;
  allowSymlinks: boolean;
  toolHttpAllowlist: string[];
  fileDenyPatterns: string[];
}

export interface ProviderDef {
  name: string;
  type: "openai" | "anthropic" | "google" | "deepseek" | "openrouter" | "openai-compatible";
  baseUrl?: string;
  apiKey?: { fromEnv?: string; secretRef?: string };
  headers: Record<string, string>;
  enabled: boolean;
  source: string;
}

export interface ModelDef {
  name: string;
  provider: string;
  modelId: string;
  displayName?: string;
  contextWindow?: number;
  maxOutputTokens?: number;
  capabilities: string[];
  defaultParams?: Record<string, unknown>;
  enabled: boolean;
  source: string;
}

export interface PromptTemplateDef {
  name: string;
  content: string;
  variables: Record<string, { required?: boolean; default?: string }>;
  source: string;
}

export interface SkillDef {
  name: string;
  mode: "inline";
  content: string;
  description?: string;
  source: string;
}

export interface AgentProfile {
  name: string;
  displayName?: string;
  description?: string;
  model: string;
  systemPrompt?: string;
  promptTemplate?: string;
  promptVariables: Record<string, string>;
  thinkingLevel: string;
  toolExecution: "parallel" | "sequential";
  maxTurns?: number;
  maxToolCalls?: number;
  skills: string[];
  tools: string[];
  mcpServers: string[];
  fileRoot?: string;
  fileAccess: "none" | "read" | "read_write";
  allowDelete: boolean;
  enabled: boolean;
  source: string;
  prompt: string;
  promptHash: string;
  profileHash: string;
}

export interface ToolDef {
  name: string;
  type: "http";
  method: string;
  url: string;
  headers: Record<string, string>;
  parameters: JsonSchema;
  timeoutSeconds: number;
  allowPrivateNetwork: boolean;
  enabled: boolean;
  source: string;
}

export interface McpServerDef {
  name: string;
  transport: "http" | "sse";
  url: string;
  headers: Record<string, string>;
  enabled: boolean;
  source: string;
}

export interface FileRootDef {
  name: string;
  path: string;
  enabled: boolean;
  source: string;
}

export interface ConfigSnapshot {
  version: string;
  runtime: RuntimeSettings;
  providers: Map<string, ProviderDef>;
  models: Map<string, ModelDef>;
  promptTemplates: Map<string, PromptTemplateDef>;
  skills: Map<string, SkillDef>;
  agents: Map<string, AgentProfile>;
  tools: Map<string, ToolDef>;
  mcpServers: Map<string, McpServerDef>;
  fileRoots: Map<string, FileRootDef>;
  sources: string[];
}

export const DEFAULT_RUNTIME: RuntimeSettings = {
  logLevel: "info",
  maxConcurrentRuns: 2,
  runTimeoutSeconds: 300,
  maxTurns: 12,
  maxToolCalls: 100,
  maxMessageBytes: 1024 * 1024,
  maxFileReadBytes: 256 * 1024,
  maxFileWriteBytes: 1024 * 1024,
  maxFileListEntries: 1000,
  maxFileOpsPerRun: 100,
  trashRetentionDays: 7,
  allowSymlinks: false,
  toolHttpAllowlist: [],
  fileDenyPatterns: [
    ".git/**", ".env*", ".ssh/**", "id_rsa*", "*.pem", "*.key", "*.p12", ".aws/**", ".kube/**",
  ],
};

export interface LoadedDocument {
  source: string;
  doc: any;
}

/**
 * 只读配置来源。模型/Provider/AgentProfile 都是其中的资源。
 */
export interface ConfigSource {
  readonly name: string;
  readonly priority: number;
  load(): Promise<LoadedDocument[]>;
}

/**
 * 本地 YAML 目录来源。
 */
export class FileConfigSource implements ConfigSource {
  readonly name = "file";
  readonly priority = 0;
  private readonly dir: string;

  constructor(dir: string) {
    this.dir = dir;
  }

  async load(): Promise<LoadedDocument[]> {
    if (!isDirectory(this.dir)) return [];
    const docs: LoadedDocument[] = [];
    for (const file of walkYaml(this.dir)) parseFile(file, docs);
    return docs;
  }
}

/**
 * HTTP ConfigSource：GET 返回 YAML 多文档。
 */
export class HttpConfigSource implements ConfigSource {
  readonly name = "http";
  readonly priority = 100;
  private readonly url: string;

  constructor(url: string) {
    this.url = url;
  }

  async load(): Promise<LoadedDocument[]> {
    const response = await fetch(this.url);
    if (!response.ok) throw new Error(`ConfigSource ${this.url} returned ${response.status}`);
    const docs: LoadedDocument[] = [];
    parseText(this.url, await response.text(), docs);
    return docs;
  }
}

/**
 * 进程内静态来源，便于嵌入宿主或测试。
 */
export class StaticConfigSource implements ConfigSource {
  readonly name: string;
  readonly priority: number;
  private readonly documents: any[];

  constructor(name: string, documents: any[], priority = 200) {
    this.name = name;
    this.documents = documents;
    this.priority = priority;
  }

  async load(): Promise<LoadedDocument[]> {
    return this.documents.map((doc) => ({ source: `static:${this.name}`, doc }));
  }
}

export interface ConfigLoadOptions {
  configDir: string;
  configUrl?: string;
  allowSymlinks: boolean;
  sources?: ConfigSource[];
}

export async function loadConfig(options: ConfigLoadOptions): Promise<ConfigSnapshot> {
  const sources = options.sources ?? [
    new FileConfigSource(options.configDir),
    ...(options.configUrl ? [new HttpConfigSource(options.configUrl)] : []),
  ];
  const layers = [] as Array<{ source: ConfigSource; docs: LoadedDocument[] }>;
  for (const source of sources) {
    const docs = await source.load();
    const keys = new Set<string>();
    for (const entry of docs) {
      const kind = String(entry.doc.kind ?? "");
      const name = String(entry.doc.metadata?.name ?? "");
      if (!kind || !name) throw new Error(`${entry.source}: kind and metadata.name are required`);
      const key = `${kind}/${name}`;
      if (keys.has(key)) throw new Error(`${entry.source}: duplicate ${key} in ${source.name}`);
      keys.add(key);
    }
    layers.push({ source, docs });
  }
  layers.sort((a, b) => a.source.priority - b.source.priority);
  const documents = layers.flatMap((layer) => layer.docs);
  return buildSnapshot(documents, [...new Set(documents.map((d) => d.source))], options);
}

function isDirectory(path: string): boolean {
  try {
    return statSync(path).isDirectory();
  } catch {
    return false;
  }
}

function walkYaml(dir: string): string[] {
  const out: string[] = [];
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    const full = join(dir, entry.name);
    if (entry.isDirectory()) out.push(...walkYaml(full));
    else if (entry.isFile() && /\.ya?ml$/i.test(entry.name)) out.push(full);
  }
  return out.sort((a, b) => relative(dir, a).localeCompare(relative(dir, b)));
}

function parseFile(file: string, out: LoadedDocument[]): void {
  parseText(file, readFileSync(file, "utf8"), out);
}

function parseText(source: string, text: string, out: LoadedDocument[]): void {
  for (const doc of parseAllDocuments(text)) {
    if (doc.errors.length) throw new Error(`${source}: ${doc.errors[0]?.message ?? "invalid yaml"}`);
    const value = doc.toJS();
    if (value != null) out.push({ source, doc: value });
  }
}

function buildSnapshot(documents: LoadedDocument[], sources: string[], options: ConfigLoadOptions): ConfigSnapshot {
  const merged = new Map<string, LoadedDocument>();
  for (const entry of documents) {
    const kind = String(entry.doc.kind ?? "");
    const name = String(entry.doc.metadata?.name ?? "");
    if (!kind || !name) throw new Error(`${entry.source}: kind and metadata.name are required`);
    merged.set(`${kind}/${name}`, entry);
  }

  const runtime: RuntimeSettings = { ...DEFAULT_RUNTIME };
  const providers = new Map<string, ProviderDef>();
  const models = new Map<string, ModelDef>();
  const promptTemplates = new Map<string, PromptTemplateDef>();
  const skills = new Map<string, SkillDef>();
  const tools = new Map<string, ToolDef>();
  const mcpServers = new Map<string, McpServerDef>();
  const fileRoots = new Map<string, FileRootDef>();

  // Pass 1: non-Agent resources.
  for (const [key, entry] of merged) {
    const { kind, metadata, spec = {} } = entry.doc;
    if (kind === "AgentProfile") continue;
    const name = metadata.name;
    const enabled = spec.enabled !== false;
    switch (kind) {
      case "Runtime":
        Object.assign(runtime, spec);
        break;
      case "Provider":
        providers.set(name, {
          name, type: spec.type, baseUrl: spec.baseUrl, apiKey: spec.apiKey,
          headers: spec.headers ?? {}, enabled, source: entry.source,
        });
        break;
      case "Model":
        models.set(name, {
          name, provider: spec.provider, modelId: spec.modelId, displayName: spec.displayName,
          contextWindow: spec.contextWindow, maxOutputTokens: spec.maxOutputTokens,
          capabilities: spec.capabilities ?? ["text"], defaultParams: spec.defaultParams,
          enabled, source: entry.source,
        });
        break;
      case "PromptTemplate":
        promptTemplates.set(name, {
          name,
          content: spec.content ?? readRelative(entry.source, spec.file, options.configDir),
          variables: spec.variables ?? {},
          source: entry.source,
        });
        break;
      case "Skill":
        skills.set(name, {
          name,
          mode: "inline",
          content: loadSkill(entry.source, spec.path, spec.include ?? ["SKILL.md"], spec.maxBytes ?? 128 * 1024, options.configDir, options.allowSymlinks),
          description: spec.description,
          source: entry.source,
        });
        break;
      case "Tool":
        tools.set(name, {
          name, type: "http", method: spec.method ?? "POST", url: spec.url,
          headers: spec.headers ?? {}, parameters: spec.parameters ?? { type: "object", properties: {} },
          timeoutSeconds: spec.timeoutSeconds ?? 15, allowPrivateNetwork: spec.allowPrivateNetwork === true,
          enabled, source: entry.source,
        });
        break;
      case "McpServer":
        mcpServers.set(name, {
          name, transport: spec.transport, url: spec.url, headers: spec.headers ?? {}, enabled, source: entry.source,
        });
        break;
      case "FileRoot":
        fileRoots.set(name, { name, path: resolveRelative(entry.source, spec.path, options.configDir), enabled, source: entry.source });
        break;
      default:
        throw new Error(`${entry.source}: unknown kind ${kind}`);
    }
  }

  // Pass 2: AgentProfile, resolving templates/skills.
  const agents = new Map<string, AgentProfile>();
  for (const entry of merged.values()) {
    if (entry.doc.kind !== "AgentProfile") continue;
    const { metadata, spec = {} } = entry.doc;
    const name = metadata.name;
    const agent = buildAgent(entry, metadata, spec, providers, models, promptTemplates, skills, tools, mcpServers, fileRoots);
    agents.set(name, agent);
  }

  const version = hash(JSON.stringify({
    sources,
    runtime,
    providers: [...providers.keys()].sort(),
    models: [...models.keys()].sort(),
    prompts: [...promptTemplates.keys()].sort(),
    skills: [...skills.keys()].sort(),
    agents: [...agents.keys()].sort(),
  }));

  return { version, runtime, providers, models, promptTemplates, skills, agents, tools, mcpServers, fileRoots, sources };
}

function buildAgent(
  entry: LoadedDocument,
  metadata: any,
  spec: any,
  providers: Map<string, ProviderDef>,
  models: Map<string, ModelDef>,
  templates: Map<string, PromptTemplateDef>,
  skills: Map<string, SkillDef>,
  tools: Map<string, ToolDef>,
  mcpServers: Map<string, McpServerDef>,
  fileRoots: Map<string, FileRootDef>,
): AgentProfile {
  const name = metadata.name;
  const enabled = spec.enabled !== false;
  const template = spec.promptTemplate ? templates.get(spec.promptTemplate) : undefined;
  if (spec.promptTemplate && !template) throw new Error(`${entry.source}: unknown PromptTemplate ${spec.promptTemplate}`);
  if (!spec.systemPrompt && !template) throw new Error(`${entry.source}: AgentProfile ${name} requires systemPrompt or promptTemplate`);

  const basePrompt = spec.systemPrompt ?? renderTemplate(template!, spec.promptVariables ?? {});
  const skillList: string[] = spec.skills ?? [];
  const skillContent = skillList.map((skill) => {
    const def = skills.get(skill);
    if (!def) throw new Error(`${entry.source}: AgentProfile ${name} references unknown Skill ${skill}`);
    return `\n\n## Skill: ${skill}\n${def.content}`;
  }).join("");
  const prompt = `${basePrompt}${skillContent}\n\n## Runtime policy\nOnly use tools explicitly provided. File tools are restricted to the configured root.`;

  const modelDef = models.get(spec.model);
  if (!modelDef) throw new Error(`${entry.source}: AgentProfile ${name} references unknown Model ${spec.model}`);
  const provider = providers.get(modelDef.provider);
  if (!provider) throw new Error(`${entry.source}: Model ${modelDef.name} references unknown Provider ${modelDef.provider}`);

  const agentTools: string[] = spec.tools ?? [];
  const agentMcp: string[] = spec.mcpServers ?? [];
  for (const tool of agentTools) {
    if (tool.startsWith("mcp__")) {
      const server = tool.split("__")[1];
      if (!server) throw new Error(`${entry.source}: invalid MCP tool ${tool}`);
      if (!agentMcp.includes(server)) throw new Error(`${entry.source}: AgentProfile ${name} tool ${tool} is outside mcpServers`);
      if (!mcpServers.has(server)) throw new Error(`${entry.source}: unknown MCP server ${server}`);
    } else if (!tools.has(tool)) {
      throw new Error(`${entry.source}: AgentProfile ${name} references unknown Tool ${tool}`);
    }
  }
  for (const server of agentMcp) {
    if (!mcpServers.has(server)) throw new Error(`${entry.source}: unknown MCP server ${server}`);
  }
  if (spec.fileRoot && !fileRoots.has(spec.fileRoot)) throw new Error(`${entry.source}: unknown FileRoot ${spec.fileRoot}`);

  const profile = {
    name,
    displayName: spec.displayName,
    description: spec.description,
    model: spec.model,
    systemPrompt: spec.systemPrompt,
    promptTemplate: spec.promptTemplate,
    promptVariables: spec.promptVariables ?? {},
    thinkingLevel: spec.thinkingLevel ?? "medium",
    toolExecution: spec.toolExecution === "sequential" ? "sequential" : "parallel",
    maxTurns: spec.maxTurns,
    maxToolCalls: spec.maxToolCalls,
    skills: skillList,
    tools: agentTools,
    mcpServers: agentMcp,
    fileRoot: spec.fileRoot,
    fileAccess: spec.fileAccess ?? "none",
    allowDelete: spec.allowDelete === true,
    enabled,
    source: entry.source,
    prompt,
    promptHash: hash(prompt),
    profileHash: "",
  } satisfies AgentProfile;

  profile.profileHash = hash(JSON.stringify({
    model: profile.model, promptHash: profile.promptHash, skills: profile.skills, tools: profile.tools,
    mcpServers: profile.mcpServers, fileRoot: profile.fileRoot, fileAccess: profile.fileAccess,
    allowDelete: profile.allowDelete, thinkingLevel: profile.thinkingLevel,
  }));
  return profile;
}

function resolveRelative(source: string, path: string, configDir: string): string {
  if (!path) throw new Error(`${source}: missing path`);
  if (path.startsWith("/")) return path;
  return resolve(configDir, path);
}

function readRelative(source: string, path: string, configDir: string): string {
  if (!path) return "";
  return readFileSync(resolve(configDir, path), "utf8");
}

function renderTemplate(template: PromptTemplateDef, vars: Record<string, string>): string {
  return template.content.replace(/\$\{([a-zA-Z0-9_.-]+)\}/g, (_, name: string) => {
    const value = vars[name] ?? template.variables[name]?.default;
    if (value == null) throw new Error(`PromptTemplate ${template.name}: missing variable ${name}`);
    return value;
  });
}

function loadSkill(source: string, path: string, includes: string[], maxBytes: number, configDir: string, allowSymlinks: boolean): string {
  const root = resolveRelative(source, path, configDir);
  const files = walkAll(root);
  const selected = new Set<string>();
  for (const pattern of includes) {
    for (const file of files) {
      if (globMatch(pattern, relative(root, file))) selected.add(file);
    }
  }
  let total = 0;
  let out = "";
  for (const file of [...selected].sort()) {
    const stat = lstatSync(file);
    if (stat.isSymbolicLink() && !allowSymlinks) throw new Error(`skill symlink denied: ${file}`);
    if (!stat.isFile() || !isTextFile(file)) continue;
    const text = readFileSync(file, "utf8");
    total += Buffer.byteLength(text);
    if (total > maxBytes) throw new Error(`skill exceeds maxBytes: ${root}`);
    out += `\n\n### ${relative(root, file)}\n${text}`;
  }
  if (!out) throw new Error(`skill has no readable include files: ${root}`);
  return out.trim();
}

function walkAll(dir: string): string[] {
  try {
    const out: string[] = [];
    for (const entry of readdirSync(dir, { withFileTypes: true })) {
      const full = join(dir, entry.name);
      if (entry.isDirectory()) out.push(...walkAll(full));
      else out.push(full);
    }
    return out;
  } catch {
    return [];
  }
}

function isTextFile(file: string): boolean {
  return /\.(md|txt|json|ya?ml)$/i.test(file);
}

function globMatch(pattern: string, value: string): boolean {
  const escaped = pattern.replace(/[.+^${}()|[\]\\]/g, "\\$&")
    .replace(/\*\*/g, "\u0000")
    .replace(/\*/g, "[^/]*")
    .replace(/\u0000/g, ".*");
  return new RegExp(`^${escaped}$`).test(value);
}

function isRemote(source: string): boolean {
  return /^https?:\/\//i.test(source);
}

export function hash(value: string): string {
  return `sha256:${createHash("sha256").update(value).digest("hex")}`;
}
