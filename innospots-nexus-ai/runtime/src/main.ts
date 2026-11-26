import { existsSync, mkdirSync, readFileSync, writeFileSync, renameSync } from "node:fs";
import { homedir } from "node:os";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { loadConfig, type ConfigSnapshot } from "./config/loader.js";
import { ModelRegistry } from "./model/registry.js";
import { SessionStore } from "./session/store.js";
import { RunManager } from "./agent/run-manager.js";
import { createRuntimeServer, type ServerDeps } from "./server/http.js";
import { EnvSecrets } from "./security/secrets.js";
import { connectMcp } from "./tool/mcp.js";

export interface CliOptions {
  host: "127.0.0.1" | "::1";
  port: number;
  dataDir: string;
  configDir: string;
  configUrl?: string;
  readyFile: string;
  pidFile?: string;
  logLevel: string;
  logFormat: string;
  watch: boolean;
  help: boolean;
  version: boolean;
}

export interface RunningRuntime {
  server: ReturnType<typeof createRuntimeServer>;
  snapshot: ConfigSnapshot;
  close(): Promise<void>;
  address(): { host: string; port: number };
}

export function parseArgs(argv: string[]): CliOptions {
  const dataDir = resolve(process.env.NEXUS_AI_DATA_DIR ?? resolve(homedir(), ".nexus-ai"));
  const options: CliOptions = {
    host: (process.env.NEXUS_AI_HOST as any) ?? "127.0.0.1",
    port: Number(process.env.NEXUS_AI_PORT ?? 18080),
    dataDir,
    configDir: resolve(process.env.NEXUS_AI_CONFIG_DIR ?? resolve(dataDir, "config")),
    configUrl: process.env.NEXUS_AI_CONFIG_URL,
    readyFile: resolve(process.env.NEXUS_AI_READY_FILE ?? resolve(dataDir, "runtime.json")),
    pidFile: undefined,
    logLevel: process.env.NEXUS_AI_LOG_LEVEL ?? "info",
    logFormat: process.stdout.isTTY ? "pretty" : "json",
    watch: true,
    help: false,
    version: false,
  };
  for (let i = 0; i < argv.length; i += 1) {
    const arg = argv[i];
    const next = () => argv[++i];
    switch (arg) {
      case "--host": options.host = next() as any; break;
      case "--port": options.port = Number(next()); break;
      case "--data-dir": options.dataDir = resolve(next()); break;
      case "--config-dir": options.configDir = resolve(next()); break;
      case "--config-url": options.configUrl = next(); break;
      case "--ready-file": options.readyFile = resolve(next()); break;
      case "--pid-file": options.pidFile = resolve(next()); break;
      case "--log-level": options.logLevel = next(); break;
      case "--log-format": options.logFormat = next(); break;
      case "--no-watch": options.watch = false; break;
      case "--help": options.help = true; break;
      case "--version": options.version = true; break;
      default: throw new Error(`unknown argument: ${arg}`);
    }
  }
  return options;
}

export async function startRuntime(options: CliOptions): Promise<RunningRuntime> {
  if (options.host !== "127.0.0.1" && options.host !== "::1") throw new Error("host must be loopback");
  if (!Number.isInteger(options.port) || options.port < 0 || options.port > 65535) throw new Error("invalid port");
  mkdirSync(options.dataDir, { recursive: true });
  for (const dir of ["sessions", "workspace", "logs", "tmp", "audit", "trash"]) {
    mkdirSync(resolve(options.dataDir, dir), { recursive: true });
  }
  const snapshot = await loadConfig({
    configDir: options.configDir,
    configUrl: options.configUrl,
    allowSymlinks: false,
  });
  const secrets = new EnvSecrets(options.dataDir);
  const sessions = new SessionStore(resolve(options.dataDir, "sessions"), resolve(options.dataDir, "workspace"));
  const models = new ModelRegistry(snapshot, secrets);
  const version = readVersion();
  const mcp = await connectMcp(snapshot, secrets, version);
  const runs = new RunManager({ snapshot, sessions, models, secrets, dataDir: options.dataDir, mcpTools: mcp.byServer });
  const deps: ServerDeps = {
    snapshot, sessions, runs, dataDir: options.dataDir,
    version, startedAt: Date.now(), configVersion: snapshot.version,
  };
  const server = createRuntimeServer(deps);
  await new Promise<void>((resolvePromise, reject) => {
    server.once("error", reject);
    server.listen(options.port, options.host, () => resolvePromise());
  });
  const address = server.address();
  const actualPort = typeof address === "object" && address ? address.port : options.port;
  const ready = { event: "listening", host: options.host, port: actualPort, pid: process.pid, version, configVersion: snapshot.version };
  mkdirSync(dirname(options.readyFile), { recursive: true });
  const temp = `${options.readyFile}.tmp`;
  writeFileSync(temp, JSON.stringify(ready), { mode: 0o600 });
  renameSync(temp, options.readyFile);
  if (options.pidFile) writeFileSync(options.pidFile, String(process.pid), { mode: 0o600 });
  process.stdout.write(JSON.stringify(ready) + "\n");

  let closed = false;
  return {
    server, snapshot,
    address: () => ({ host: options.host, port: actualPort }),
    async close() {
      if (closed) return;
      closed = true;
      await mcp.close();
      await new Promise<void>((resolvePromise) => server.close(() => resolvePromise()));
    },
  };
}

function readVersion(): string {
  try {
    const url = new URL("../package.json", import.meta.url);
    return JSON.parse(readFileSync(url, "utf8")).version;
  } catch {
    return "0.0.0";
  }
}

export async function main(argv = process.argv.slice(2)): Promise<number> {
  let options: CliOptions;
  try {
    options = parseArgs(argv);
  } catch (error) {
    process.stderr.write(`${messageOf(error)}\n`);
    return 2;
  }
  if (options.help) {
    process.stdout.write(usage());
    return 0;
  }
  if (options.version) {
    process.stdout.write(readVersion() + "\n");
    return 0;
  }
  try {
    const runtime = await startRuntime(options);
    const shutdown = async () => {
      await runtime.close();
      process.exit(0);
    };
    process.once("SIGTERM", shutdown);
    process.once("SIGINT", shutdown);
    return 0;
  } catch (error) {
    process.stderr.write(`${messageOf(error)}\n`);
    return 1;
  }
}

function usage(): string {
  return [
    "nexus-ai [options]",
    "",
    "  --host 127.0.0.1       loopback only",
    "  --port 18080           listen port (0 = random)",
    "  --data-dir ~/.nexus-ai data directory",
    "  --config-dir <data>/config",
    "  --config-url <url>     ConfigSource HTTP URL",
    "  --ready-file <data>/runtime.json",
    "  --pid-file <path>",
    "  --log-level info",
    "  --log-format json|pretty",
    "  --no-watch",
    "  --help / --version",
  ].join("\n") + "\n";
}

function messageOf(error: unknown): string {
  return error instanceof Error ? error.message : String(error);
}

if (process.argv[1] && import.meta.url === `file://${resolve(process.argv[1])}`) {
  main().then((code) => { if (code !== 0) process.exit(code); });
}
