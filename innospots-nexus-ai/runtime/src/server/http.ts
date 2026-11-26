import { createServer as createHttpServer, type IncomingMessage, type Server, type ServerResponse } from "node:http";
import { readFileSync } from "node:fs";
import { join } from "node:path";
import type { ConfigSnapshot } from "../config/loader.js";
import type { RunManager } from "../agent/run-manager.js";
import type { SessionStore } from "../session/store.js";

export interface ServerDeps {
  snapshot: ConfigSnapshot;
  sessions: SessionStore;
  runs: RunManager;
  dataDir: string;
  version: string;
  startedAt: number;
  configVersion: string;
}

export function createRuntimeServer(deps: ServerDeps): Server {
  return createHttpServer(async (req, res) => {
    try {
      await route(deps, req, res);
    } catch (error) {
      sendError(res, req, error);
    }
  });
}

async function route(deps: ServerDeps, req: IncomingMessage, res: ServerResponse): Promise<void> {
  const url = new URL(req.url ?? "/", "http://127.0.0.1");
  const path = url.pathname;
  const method = req.method ?? "GET";
  const owner = String(req.headers["x-nexus-user"] ?? "").trim();

  if (method === "GET" && path === "/v1/health") {
    return sendJson(res, 200, health(deps));
  }
  if (method === "GET" && path === "/v1/openapi.yaml") {
    res.writeHead(200, { "content-type": "application/yaml; charset=utf-8" });
    res.end(openapi(deps));
    return;
  }
  if (method === "GET" && path === "/v1/agents") {
    return sendJson(res, 200, { items: [...deps.snapshot.agents.values()].filter((a) => a.enabled).map(publicAgent) });
  }

  if (path === "/v1/sessions" && method === "GET") {
    requireOwner(owner);
    return sendJson(res, 200, { items: await deps.sessions.list(owner) });
  }
  if (path === "/v1/sessions" && method === "POST") {
    requireOwner(owner);
    const body = await readJson(req, deps.snapshot.runtime.maxMessageBytes);
    const profile = deps.snapshot.agents.get(String(body.agent ?? ""));
    if (!profile || !profile.enabled) throw httpError(404, "agent_not_found");
    const fileRoot = profile.fileRoot ? deps.snapshot.fileRoots.get(profile.fileRoot) : undefined;
    const session = deps.sessions.create({
      owner, agent: profile.name, title: body.title,
      profileHash: profile.profileHash, promptHash: profile.promptHash, prompt: profile.prompt,
      skills: profile.skills, tools: profile.tools, mcpServers: profile.mcpServers,
      fileRoot: profile.fileRoot, fileRootPath: fileRoot?.path,
      fileAccess: profile.fileAccess, allowDelete: profile.allowDelete,
    });
    return sendJson(res, 201, session);
  }

  const sessionMatch = /^\/v1\/sessions\/([^/]+)$/.exec(path);
  if (sessionMatch) {
    requireOwner(owner);
    const opened = await deps.sessions.find(sessionMatch[1], owner);
    if (!opened) throw httpError(404, "session_not_found");
    if (method === "GET") {
      return sendJson(res, 200, { id: opened.snapshot.id, agent: opened.snapshot.agent, owner: opened.snapshot.owner, title: opened.snapshot.title });
    }
    if (method === "DELETE") {
      deps.sessions.delete(opened);
      res.writeHead(204).end();
      return;
    }
  }

  const messagesMatch = /^\/v1\/sessions\/([^/]+)\/messages$/.exec(path);
  if (messagesMatch) {
    requireOwner(owner);
    const opened = await deps.sessions.find(messagesMatch[1], owner);
    if (!opened) throw httpError(404, "session_not_found");
    if (method === "GET") {
      return sendJson(res, 200, { items: deps.sessions.history(opened, Number(url.searchParams.get("limit") ?? 50)) });
    }
    if (method === "POST") {
      const body = await readJson(req, deps.snapshot.runtime.maxMessageBytes);
      if (!body.input || typeof body.input !== "string") throw httpError(400, "invalid_request");
      const stream = /text\/event-stream/i.test(String(req.headers.accept ?? ""));
      if (stream) {
        res.writeHead(200, {
          "content-type": "text/event-stream; charset=utf-8",
          "cache-control": "no-cache, no-transform",
          connection: "keep-alive",
        });
        const heartbeat = setInterval(() => writeEvent(res, "heartbeat", { ts: Date.now() }), 15000);
        try {
          const result = await deps.runs.run(opened.snapshot.id, owner, body.input, async (event) => writeEvent(res, event.event, event.data));
          if (result.status === "failed") writeEvent(res, "error", result.error);
        } finally {
          clearInterval(heartbeat);
          res.end();
        }
        return;
      }
      let text = "";
      const result = await deps.runs.run(opened.snapshot.id, owner, body.input, (event) => {
        if (event.event === "text_delta") text += (event.data as any).delta ?? "";
      });
      return sendJson(res, result.status === "failed" ? 502 : 200, { ...result, text });
    }
  }

  const runMatch = /^\/v1\/runs\/([^/]+)$/.exec(path);
  if (runMatch) {
    const run = deps.runs.get(runMatch[1]);
    if (!run) throw httpError(404, "run_not_found");
    if (method === "GET") return sendJson(res, 200, run);
  }
  const cancelMatch = /^\/v1\/runs\/([^/]+)\/cancel$/.exec(path);
  if (cancelMatch && method === "POST") {
    if (!deps.runs.cancel(cancelMatch[1])) throw httpError(409, "run_not_active");
    return sendJson(res, 200, { runId: cancelMatch[1], status: "aborted" });
  }

  throw httpError(404, "path_not_found");
}

function health(deps: ServerDeps) {
  return {
    status: "UP",
    version: deps.version,
    node: process.version,
    configVersion: deps.configVersion,
    startedAt: new Date(deps.startedAt).toISOString(),
    checks: { config: "UP", sessions: "UP", providers: "SKIPPED" },
  };
}

function publicAgent(agent: any) {
  return {
    name: agent.name,
    displayName: agent.displayName,
    description: agent.description,
    model: agent.model,
    capabilities: ["text", "tool"],
    fileAccess: agent.fileAccess,
    skills: agent.skills,
  };
}

function openapi(deps: ServerDeps): string {
  return [
    "openapi: 3.1.0",
    "info:",
    "  title: Nexus AI Runtime",
    `  version: ${deps.version}`,
    "paths:",
    "  /v1/agents: { get: { responses: { '200': { description: OK } } } }",
    "  /v1/sessions: { get: { responses: { '200': { description: OK } } }, post: { responses: { '201': { description: Created } } } }",
  ].join("\n");
}

function requireOwner(owner: string): void {
  if (!owner) throw httpError(401, "owner_required");
}

async function readJson(req: IncomingMessage, maxBytes: number): Promise<any> {
  let size = 0;
  const chunks: Buffer[] = [];
  for await (const chunk of req) {
    size += chunk.length;
    if (size > maxBytes) throw httpError(413, "payload_too_large");
    chunks.push(chunk);
  }
  if (!chunks.length) return {};
  try {
    return JSON.parse(Buffer.concat(chunks).toString("utf8"));
  } catch {
    throw httpError(400, "invalid_request");
  }
}

function sendJson(res: ServerResponse, status: number, body: unknown): void {
  const text = JSON.stringify(body);
  res.writeHead(status, { "content-type": "application/json; charset=utf-8", "content-length": Buffer.byteLength(text) });
  res.end(text);
}

function writeEvent(res: ServerResponse, event: string, data: unknown): void {
  res.write(`event: ${event}\ndata: ${JSON.stringify(data)}\n\n`);
}

function sendError(res: ServerResponse, req: IncomingMessage, error: unknown): void {
  const status = (error as any)?.status ?? 500;
  const code = (error as any)?.code ?? (status === 500 ? "internal_error" : "invalid_request");
  const message = error instanceof Error ? error.message : String(error);
  if (res.headersSent) {
    try { res.end(); } catch { /* noop */ }
    return;
  }
  sendJson(res, status, { error: { code, message, requestId: String(req.headers["x-request-id"] ?? ""), details: {} } });
}

function httpError(status: number, code: string): Error & { status: number; code: string } {
  const error = new Error(code) as Error & { status: number; code: string };
  error.status = status;
  error.code = code;
  return error;
}
