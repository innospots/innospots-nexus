import { randomUUID } from "node:crypto";
import { Agent, type AgentEvent } from "@earendil-works/pi-agent-core";
import type { AgentProfile, ConfigSnapshot } from "../config/loader.js";
import type { ModelRegistry } from "../model/registry.js";
import type { SessionStore, SessionSnapshot } from "../session/store.js";
import type { Secrets } from "../security/secrets.js";
import { buildTools } from "../tool/registry.js";
import type { AgentTool } from "@earendil-works/pi-agent-core";

export interface RuntimeEvent {
  event: string;
  data: unknown;
}

export interface RunResult {
  runId: string;
  sessionId: string;
  status: "succeeded" | "aborted" | "failed";
  usage?: unknown;
  error?: { code: string; message: string };
}

interface RunState {
  runId: string;
  sessionId: string;
  owner: string;
  status: "running" | "succeeded" | "aborted" | "failed";
  startedAt: number;
  finishedAt?: number;
  usage?: unknown;
  error?: { code: string; message: string };
  abort: () => void;
}

export class RunManager {
  private readonly runs = new Map<string, RunState>();
  private readonly snapshot: ConfigSnapshot;
  private readonly sessions: SessionStore;
  private readonly models: ModelRegistry;
  private readonly secrets: Secrets;
  private readonly dataDir: string;
  private readonly mcpTools: Map<string, AgentTool<any>[]>;

  constructor(input: {
    snapshot: ConfigSnapshot;
    sessions: SessionStore;
    models: ModelRegistry;
    secrets: Secrets;
    dataDir: string;
    mcpTools?: Map<string, AgentTool<any>[]>;
  }) {
    this.snapshot = input.snapshot;
    this.sessions = input.sessions;
    this.models = input.models;
    this.secrets = input.secrets;
    this.dataDir = input.dataDir;
    this.mcpTools = input.mcpTools ?? new Map();
  }

  get(runId: string): RunState | undefined {
    return this.runs.get(runId);
  }

  cancel(runId: string): boolean {
    const run = this.runs.get(runId);
    if (!run || run.status !== "running") return false;
    run.abort();
    run.status = "aborted";
    run.finishedAt = Date.now();
    return true;
  }

  async run(
    sessionId: string,
    owner: string,
    input: string,
    onEvent: (event: RuntimeEvent) => Promise<void> | void,
  ): Promise<RunResult> {
    const opened = await this.sessions.find(sessionId, owner);
    if (!opened) throw new Error("session_not_found");
    const profile = this.resolveProfile(opened.snapshot);
    const runId = randomUUID();
    const active = [...this.runs.values()].filter((run) => run.status === "running");
    if (active.length >= this.snapshot.runtime.maxConcurrentRuns) throw new Error("limit_exceeded");

    let aborted = false;
    const state: RunState = {
      runId, sessionId, owner, status: "running", startedAt: Date.now(),
      abort: () => { aborted = true; agent.abort(); },
    };
    this.runs.set(runId, state);

    await onEvent({ event: "run_started", data: { runId, sessionId, owner } });
    let agent: Agent;
    try {
      const resolved = this.models.resolve(profile.model);
      const context = opened.manager.buildSessionContext();
      const messages = context.messages as any[];
      const tools = buildTools({
        snapshot: this.snapshot,
        profile,
        fileRoot: profile.fileRoot ? this.snapshot.fileRoots.get(profile.fileRoot) : undefined,
        dataDir: this.dataDir,
        secrets: this.secrets,
        sessionId,
        runId,
        mcpTools: this.mcpTools,
      });
      const maxTurns = profile.maxTurns ?? this.snapshot.runtime.maxTurns;
      let turns = 0;
      let toolCalls = 0;
      const firstRole = messages[0]?.role;
      agent = new Agent({
        initialState: {
          ...(firstRole === "system" ? {} : { systemPrompt: profile.prompt }),
          messages,
          model: resolved.model,
          thinkingLevel: profile.thinkingLevel as any,
          tools,
        },
        streamFn: (model, ctx, options) => this.models.getModels().streamSimple(model, ctx, {
          ...(options ?? {}),
          apiKey: resolved.apiKey,
        } as any),
        convertToLlm: (msgs) => msgs as any,
        toolExecution: profile.toolExecution,
        sessionId,
        beforeToolCall: async ({ toolCall }) => {
          if (!isAllowedTool(profile, toolCall.name)) return { block: true, reason: `tool_not_allowed: ${toolCall.name}` };
          toolCalls += 1;
          if (toolCalls > (profile.maxToolCalls ?? this.snapshot.runtime.maxToolCalls)) {
            return { block: true, reason: "max_tool_calls_exceeded" };
          }
          return undefined;
        },
        finishTurn: async ({ message }) => {
          turns += 1;
          if (turns >= maxTurns && message.stopReason !== "aborted") return { action: "end" };
          return undefined;
        },
      });

      agent.subscribe(async (event: AgentEvent) => {
        await this.onAgentEvent(event, opened.manager, state, onEvent);
      });

      const timer = setTimeout(() => {
        aborted = true;
        agent.abort();
      }, this.snapshot.runtime.runTimeoutSeconds * 1000);

      try {
        await agent.prompt(input);
      } finally {
        clearTimeout(timer);
      }

      state.status = aborted ? "aborted" : "succeeded";
      state.finishedAt = Date.now();
      state.usage = usageOf(agent);
      await onEvent({
        event: "run_completed",
        data: { runId, sessionId, status: state.status, usage: state.usage },
      });
      return { runId, sessionId, status: state.status, usage: state.usage };
    } catch (error) {
      state.status = aborted ? "aborted" : "failed";
      state.finishedAt = Date.now();
      state.error = { code: aborted ? "run_aborted" : "run_failed", message: messageOf(error) };
      await onEvent({
        event: "error",
        data: { runId, sessionId, code: state.error.code, message: state.error.message },
      });
      return { runId, sessionId, status: state.status, error: state.error };
    }
  }

  private async onAgentEvent(
    event: AgentEvent,
    manager: any,
    state: RunState,
    onEvent: (event: RuntimeEvent) => Promise<void> | void,
  ): Promise<void> {
    switch (event.type) {
      case "message_start":
        await onEvent({ event: "message_started", data: { messageId: (event.message as any).id, role: (event.message as any).role } });
        break;
      case "message_update": {
        const update: any = (event as any).assistantMessageEvent;
        if (update?.type === "text_delta") await onEvent({ event: "text_delta", data: { delta: update.delta } });
        if (update?.type === "thinking_delta") await onEvent({ event: "thinking_delta", data: { delta: update.delta } });
        break;
      }
      case "message_end": {
        const message: any = event.message;
        if (message.role === "user" || message.role === "assistant" || message.role === "toolResult") {
          manager.appendMessage(message);
        }
        if (message.role === "assistant") {
          state.usage = message.usage;
          await onEvent({ event: "message_completed", data: { messageId: message.id, usage: message.usage } });
        }
        break;
      }
      case "tool_execution_start":
        await onEvent({ event: "tool_started", data: { toolCallId: event.toolCallId, name: event.toolName, arguments: event.args } });
        break;
      case "tool_execution_end":
        await onEvent({ event: "tool_finished", data: { toolCallId: event.toolCallId, isError: event.isError } });
        break;
      default:
        break;
    }
  }

  private resolveProfile(snapshot: SessionSnapshot): AgentProfile {
    const profile = this.snapshot.agents.get(snapshot.agent);
    if (!profile || !profile.enabled) throw new Error("agent_not_found");
    if (profile.profileHash !== snapshot.profileHash) {
      // Session keeps its original binding; use the current definitions but honor snapshot restrictions.
      return {
        ...profile,
        skills: snapshot.skills,
        tools: snapshot.tools,
        mcpServers: snapshot.mcpServers,
        fileRoot: snapshot.fileRoot,
        fileAccess: snapshot.fileAccess,
        allowDelete: snapshot.allowDelete,
      };
    }
    return profile;
  }
}

function isAllowedTool(profile: AgentProfile, name: string): boolean {
  if (name.startsWith("file_")) {
    if (name === "file_delete") return profile.allowDelete;
    if (name === "file_write" || name === "file_edit") return profile.fileAccess === "read_write";
    return profile.fileAccess !== "none";
  }
  return profile.tools.includes(name);
}

function usageOf(agent: Agent): unknown {
  const messages = agent.state.messages as any[];
  for (let i = messages.length - 1; i >= 0; i -= 1) {
    if (messages[i]?.role === "assistant" && messages[i].usage) return messages[i].usage;
  }
  return undefined;
}

function messageOf(error: unknown): string {
  return error instanceof Error ? error.message : String(error);
}
