import { randomUUID } from "node:crypto";
import { mkdirSync, rmSync } from "node:fs";
import { join } from "node:path";
import { SessionManager, type SessionInfo } from "@earendil-works/pi-coding-agent";

export interface SessionSnapshot {
  id: string;
  owner: string;
  agent: string;
  title?: string;
  profileHash: string;
  promptHash: string;
  skills: string[];
  tools: string[];
  mcpServers: string[];
  fileRoot?: string;
  fileRootPath?: string;
  fileAccess: "none" | "read" | "read_write";
  allowDelete: boolean;
  createdAt: string;
}

export interface SessionSummary {
  id: string;
  owner: string;
  agent: string;
  title?: string;
  messageCount: number;
  createdAt: string;
  updatedAt: string;
  path?: string;
}

export interface MessageView {
  id: string;
  role: string;
  content: unknown;
  timestamp?: number;
}

interface OpenSession {
  manager: SessionManager;
  snapshot: SessionSnapshot;
  path?: string;
}

export class SessionStore {
  private readonly sessionDir: string;
  private readonly virtualCwd: string;
  private readonly open = new Map<string, OpenSession>();

  constructor(sessionDir: string, virtualCwd: string) {
    this.sessionDir = sessionDir;
    this.virtualCwd = virtualCwd;
    mkdirSync(sessionDir, { recursive: true });
    mkdirSync(virtualCwd, { recursive: true });
  }

  create(input: {
    owner: string;
    agent: string;
    title?: string;
    profileHash: string;
    promptHash: string;
    prompt: string;
    skills: string[];
    tools: string[];
    mcpServers: string[];
    fileRoot?: string;
    fileRootPath?: string;
    fileAccess: "none" | "read" | "read_write";
    allowDelete: boolean;
  }): SessionSnapshot {
    const manager = SessionManager.create(this.virtualCwd, this.sessionDir);
    const id = manager.getSessionId();
    const createdAt = new Date().toISOString();
    const snapshot: SessionSnapshot = {
      id, owner: input.owner, agent: input.agent, title: input.title,
      profileHash: input.profileHash, promptHash: input.promptHash,
      skills: input.skills, tools: input.tools, mcpServers: input.mcpServers,
      fileRoot: input.fileRoot, fileRootPath: input.fileRootPath,
      fileAccess: input.fileAccess, allowDelete: input.allowDelete, createdAt,
    };
    manager.appendCustomEntry("nexus.session", snapshot);
    manager.appendMessage({ role: "system", content: input.prompt, timestamp: Date.now() } as any);
    if (input.title) manager.appendSessionInfo(input.title);
    this.open.set(id, { manager, snapshot, path: manager.getSessionFile() });
    return snapshot;
  }

  async find(id: string, owner?: string): Promise<OpenSession | undefined> {
    const cached = this.open.get(id);
    if (cached) {
      if (owner && cached.snapshot.owner !== owner) return undefined;
      return cached;
    }
    const info = (await SessionManager.list(this.virtualCwd, this.sessionDir)).find((item) => item.id === id);
    if (!info) return undefined;
    const manager = SessionManager.open(info.path, this.sessionDir, this.virtualCwd);
    const snapshot = readSnapshot(manager);
    if (!snapshot) return undefined;
    if (owner && snapshot.owner !== owner) return undefined;
    const opened = { manager, snapshot, path: info.path };
    this.open.set(id, opened);
    return opened;
  }

  async list(owner: string): Promise<SessionSummary[]> {
    const infos = await SessionManager.list(this.virtualCwd, this.sessionDir);
    const out: SessionSummary[] = [];
    for (const info of infos) {
      const snapshot = await this.snapshotOf(info);
      if (!snapshot || snapshot.owner !== owner) continue;
      out.push(summary(info, snapshot));
    }
    return out.sort((a, b) => b.updatedAt.localeCompare(a.updatedAt));
  }

  history(opened: OpenSession, limit = 50): MessageView[] {
    const entries = opened.manager.buildContextEntries();
    const messages: MessageView[] = [];
    for (const entry of entries as any[]) {
      if (entry.type !== "message") continue;
      const role = entry.message?.role;
      if (role === "system") continue;
      messages.push({
        id: entry.id,
        role,
        content: entry.message?.content,
        timestamp: entry.message?.timestamp,
      });
    }
    return messages.slice(-limit);
  }

  delete(opened: OpenSession): void {
    const path = opened.path ?? opened.manager.getSessionFile();
    this.open.delete(opened.snapshot.id);
    if (path) rmSync(path, { force: true });
  }

  dir(): string {
    return this.sessionDir;
  }

  private async snapshotOf(info: SessionInfo): Promise<SessionSnapshot | undefined> {
    const cached = this.open.get(info.id);
    if (cached) return cached.snapshot;
    try {
      const manager = SessionManager.open(info.path, this.sessionDir, this.virtualCwd);
      return readSnapshot(manager);
    } catch {
      return undefined;
    }
  }
}

function readSnapshot(manager: SessionManager): SessionSnapshot | undefined {
  for (const entry of manager.getEntries() as any[]) {
    if (entry.type === "custom" && entry.customType === "nexus.session") {
      return entry.data as SessionSnapshot;
    }
  }
  return undefined;
}

function summary(info: SessionInfo, snapshot: SessionSnapshot): SessionSummary {
  return {
    id: snapshot.id,
    owner: snapshot.owner,
    agent: snapshot.agent,
    title: snapshot.title ?? info.name,
    messageCount: info.messageCount,
    createdAt: snapshot.createdAt,
    updatedAt: info.modified.toISOString(),
    path: info.path,
  };
}
