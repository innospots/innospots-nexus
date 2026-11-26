import { appendFileSync, mkdirSync, readFileSync, readdirSync, renameSync, rmSync, statSync, writeFileSync, existsSync } from "node:fs";
import { basename, dirname, join } from "node:path";
import type { AgentTool } from "@earendil-works/pi-agent-core";
import type { AgentProfile, ConfigSnapshot, FileRootDef, ToolDef } from "../config/loader.js";
import { PathGuard } from "../security/path-guard.js";
import type { Secrets } from "../security/secrets.js";
import { interpolate } from "../security/secrets.js";

export interface ToolContext {
  snapshot: ConfigSnapshot;
  profile: AgentProfile;
  fileRoot?: FileRootDef;
  dataDir: string;
  secrets: Secrets;
  sessionId: string;
  runId: string;
  mcpTools?: Map<string, AgentTool<any>[]>;
}

export function buildTools(ctx: ToolContext): AgentTool<any>[] {
  const tools: AgentTool<any>[] = [];
  if (ctx.profile.fileRoot && ctx.profile.fileAccess !== "none" && ctx.fileRoot) {
    tools.push(...buildFileTools(ctx, ctx.fileRoot));
  }
  for (const name of ctx.profile.tools) {
    if (name.startsWith("mcp__")) {
      for (const tool of ctx.mcpTools?.get(name.split("__")[1]) ?? []) {
        if (tool.name === name) tools.push(tool);
      }
      continue;
    }
    const def = ctx.snapshot.tools.get(name);
    if (def?.enabled) tools.push(buildHttpTool(ctx, def));
  }
  return tools;
}

function buildFileTools(ctx: ToolContext, root: FileRootDef): AgentTool<any>[] {
  const guard = new PathGuard(root.path);
  const access = ctx.profile.fileAccess;
  const tools: AgentTool<any>[] = [
    {
      name: "file_read",
      label: "Read file",
      description: "Read a UTF-8 text file inside the workspace.",
      parameters: obj({ path: str(), offset: num(), limit: num() }),
      execute: async (_id, params: any) => {
        audit(ctx, "file_read", params.path, 0, 0, "ok");
        const target = guard.resolve(params.path);
        guard.assertRegularFile(target);
        const text = readFileSync(target, "utf8");
        const lines = text.split(/\r?\n/);
        const offset = Math.max(0, Number(params.offset ?? 0));
        const limit = Math.max(1, Math.min(Number(params.limit ?? 400), 2000));
        return textResult(lines.slice(offset, offset + limit).join("\n"), { bytes: Buffer.byteLength(text) });
      },
    },
    {
      name: "file_list",
      label: "List files",
      description: "List files and directories inside the workspace.",
      parameters: obj({ path: str() }),
      execute: async (_id, params: any) => {
        audit(ctx, "file_list", params.path ?? ".", 0, 0, "ok");
        const target = guard.resolve(params.path ?? ".");
        guard.assertDirectory(target);
        const entries = readdirSync(target, { withFileTypes: true }).slice(0, ctx.snapshot.runtime.maxFileListEntries);
        const text = entries.map((e) => `${e.isDirectory() ? "dir " : "file"} ${e.name}`).join("\n");
        return textResult(text);
      },
    },
  ];
  if (access === "read_write") {
    tools.push(
      {
        name: "file_write",
        label: "Write file",
        description: "Create or overwrite a UTF-8 text file inside the workspace.",
        parameters: obj({ path: str(), content: str(), overwrite: bool() }),
        execute: async (_id, params: any) => {
          const target = guard.resolve(params.path);
          const bytes = Buffer.byteLength(params.content);
          if (bytes > ctx.snapshot.runtime.maxFileWriteBytes) {
            audit(ctx, "file_write", params.path, bytes, 0, "error", "file_too_large");
            throw new Error("file_too_large");
          }
          mkdirSync(dirname(target), { recursive: true });
          if (existsSync(target) && params.overwrite === false) throw new Error("file_exists");
          const tmp = `${target}.nexus-tmp-${ctx.runId}`;
          writeFileSync(tmp, params.content, { mode: 0o640 });
          renameSync(tmp, target);
          audit(ctx, "file_write", params.path, bytes, 0, "ok");
          return textResult(`written ${bytes} bytes`);
        },
      },
      {
        name: "file_edit",
        label: "Edit file",
        description: "Replace exact text in a file inside the workspace.",
        parameters: obj({ path: str(), oldText: str(), newText: str(), replaceAll: bool() }),
        execute: async (_id, params: any) => {
          const target = guard.resolve(params.path);
          guard.assertRegularFile(target);
          const text = readFileSync(target, "utf8");
          const count = text.split(params.oldText).length - 1;
          if (count === 0) throw new Error("old_text_not_found");
          if (count > 1 && !params.replaceAll) throw new Error("old_text_not_unique");
          const next = params.replaceAll ? text.split(params.oldText).join(params.newText) : text.replace(params.oldText, params.newText);
          if (Buffer.byteLength(next) > ctx.snapshot.runtime.maxFileWriteBytes) throw new Error("file_too_large");
          const tmp = `${target}.nexus-tmp-${ctx.runId}`;
          writeFileSync(tmp, next, { mode: 0o640 });
          renameSync(tmp, target);
          audit(ctx, "file_edit", params.path, 0, 0, "ok");
          return textResult("edited");
        },
      },
    );
    if (ctx.profile.allowDelete) {
      tools.push({
        name: "file_delete",
        label: "Delete file",
        description: "Soft-delete a file or empty directory inside the workspace.",
        parameters: obj({ path: str() }),
        execute: async (_id, params: any) => {
          const target = guard.resolve(params.path);
          const stat = statSync(target);
          if (stat.isDirectory() && readdirSync(target).length > 0) throw new Error("directory_not_empty");
          const trash = join(ctx.dataDir, "trash", safeName(ctx.fileRoot!.name), new Date().toISOString().slice(0, 10), ctx.runId, params.path);
          mkdirSync(dirname(trash), { recursive: true });
          renameSync(target, trash);
          audit(ctx, "file_delete", params.path, 0, 0, "ok");
          return textResult(`moved to trash: ${trash}`);
        },
      });
    }
  }
  return tools;
}

function buildHttpTool(ctx: ToolContext, def: ToolDef): AgentTool<any> {
  return {
    name: def.name,
    label: def.name,
    description: `HTTP tool ${def.method} ${def.url}`,
    parameters: def.parameters,
    execute: async (_id, params: any) => {
      const controller = new AbortController();
      const timer = setTimeout(() => controller.abort(), def.timeoutSeconds * 1000);
      try {
        const response = await fetch(def.url, {
          method: def.method,
          headers: { "content-type": "application/json", ...resolveHeaders(def.headers, ctx.secrets) },
          body: def.method === "GET" ? undefined : JSON.stringify(params),
          signal: controller.signal,
        });
        const text = await response.text();
        if (!response.ok) throw new Error(`http_${response.status}: ${text.slice(0, 500)}`);
        return textResult(text);
      } finally {
        clearTimeout(timer);
      }
    },
  };
}

function resolveHeaders(headers: Record<string, string>, secrets: Secrets): Record<string, string> {
  return Object.fromEntries(Object.entries(headers).map(([k, v]) => [k, interpolate(v, secrets)]));
}

function audit(ctx: ToolContext, op: string, path: string, bytesIn: number, bytesOut: number, result: string, errorCode?: string): void {
  const dir = join(ctx.dataDir, "audit");
  mkdirSync(dir, { recursive: true });
  const file = join(dir, `file-ops-${new Date().toISOString().slice(0, 10)}.jsonl`);
  appendFileSync(file, JSON.stringify({
    ts: new Date().toISOString(), sessionId: ctx.sessionId, runId: ctx.runId,
    agent: ctx.profile.name, root: ctx.fileRoot?.name, op, path, bytesIn, bytesOut, result, errorCode: errorCode ?? null,
  }) + "\n", { mode: 0o600 });
}

function textResult(text: string, details: unknown = {}) {
  return { content: [{ type: "text" as const, text }], details };
}

function obj(properties: Record<string, unknown>) {
  return { type: "object", properties, additionalProperties: false };
}
function str() { return { type: "string" }; }
function num() { return { type: "number" }; }
function bool() { return { type: "boolean" }; }
function safeName(value: string) { return value.replace(/[^A-Za-z0-9._-]/g, "_"); }
