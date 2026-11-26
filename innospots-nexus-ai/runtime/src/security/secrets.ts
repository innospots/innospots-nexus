import { readFileSync, existsSync } from "node:fs";
import { join } from "node:path";

export interface Secrets {
  get(name: string): string | undefined;
}

export class EnvSecrets implements Secrets {
  private readonly values: Record<string, string>;

  constructor(dataDir: string, env: NodeJS.ProcessEnv = process.env) {
    const values: Record<string, string> = {};
    const file = join(dataDir, "secrets.env");
    if (existsSync(file)) {
      for (const line of readFileSync(file, "utf8").split(/\r?\n/)) {
        const match = /^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)\s*$/.exec(line);
        if (!match) continue;
        values[match[1]] = match[2].replace(/^['"]|['"]$/g, "");
      }
    }
    for (const [key, value] of Object.entries(env)) {
      if (key.startsWith("NEXUS_AI_") && value != null) values[key] = value;
    }
    this.values = values;
  }

  get(name: string): string | undefined {
    return this.values[name];
  }
}

export function interpolate(value: string, secrets: Secrets): string {
  return value.replace(/\$\{([A-Za-z_][A-Za-z0-9_]*)\}/g, (_, name: string) => {
    const secret = secrets.get(name);
    if (secret == null) throw new Error(`missing secret ${name}`);
    return secret;
  });
}

export function sanitizeEnv(base: NodeJS.ProcessEnv, dataDir: string): NodeJS.ProcessEnv {
  const allowed = ["PATH", "LANG", "LC_ALL", "TZ", "NEXUS_AI_LOG_LEVEL", "NEXUS_AI_MASTER_KEY", "NEXUS_AI_CONFIG_URL"];
  const env: NodeJS.ProcessEnv = { NODE_ENV: "production", NEXUS_AI_HOME: dataDir };
  for (const key of allowed) {
    if (base[key] != null) env[key] = base[key];
  }
  return env;
}
