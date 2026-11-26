import type { AgentTool } from "@earendil-works/pi-agent-core";
import { McpClient, StreamableHttpTransport } from "@earendil-works/pi-mcp";
import type { ConfigSnapshot, McpServerDef } from "../config/loader.js";
import type { Secrets } from "../security/secrets.js";
import { interpolate } from "../security/secrets.js";

export interface McpTools {
  byServer: Map<string, AgentTool<any>[]>;
  close(): Promise<void>;
}

export async function connectMcp(
  snapshot: ConfigSnapshot,
  secrets: Secrets,
  version: string,
): Promise<McpTools> {
  const byServer = new Map<string, AgentTool<any>[]>();
  const clients: McpClient[] = [];
  for (const server of snapshot.mcpServers.values()) {
    if (!server.enabled) continue;
    try {
      const client = new McpClient({ name: "nexus-ai-runtime", version, requestTimeoutMs: 3000 });
      const transport = new StreamableHttpTransport({
        url: server.url,
        headers: resolveHeaders(server, secrets),
      });
      await Promise.race([
        client.connect(transport),
        new Promise((_, reject) => setTimeout(() => reject(new Error("mcp connect timeout")), 3000)),
      ]);
      const tools = await client.listTools();
      byServer.set(server.name, tools.map((tool) => toAgentTool(server, tool, client)));
      clients.push(client);
    } catch (error) {
      // MCP outage must not prevent the runtime from serving non-MCP agents.
      process.stderr.write(`mcp server ${server.name} unavailable: ${messageOf(error)}\n`);
    }
  }
  return {
    byServer,
    async close() {
      await Promise.all(clients.map((client) => client.close().catch(() => undefined)));
    },
  };
}

function toAgentTool(server: McpServerDef, tool: any, client: McpClient): AgentTool<any> {
  return {
    name: `mcp__${server.name}__${tool.name}`,
    label: `MCP ${server.name}/${tool.name}`,
    description: tool.description ?? `MCP tool ${server.name}/${tool.name}`,
    parameters: (tool.inputSchema as object) ?? { type: "object", properties: {} },
    execute: async (_id, params: any) => {
      const result = await client.callTool(tool.name, params ?? {});
      const content = (result.content ?? []).map((block: any) => {
        if (block.type === "text") return block.text;
        return JSON.stringify(block);
      }).join("\n");
      return {
        content: [{ type: "text", text: content }],
        details: {},
        isError: result.isError === true,
      };
    },
  };
}

function resolveHeaders(server: McpServerDef, secrets: Secrets): Record<string, string> {
  return Object.fromEntries(Object.entries(server.headers).map(([key, value]) => [key, interpolate(value, secrets)]));
}

function messageOf(error: unknown): string {
  return error instanceof Error ? error.message : String(error);
}
