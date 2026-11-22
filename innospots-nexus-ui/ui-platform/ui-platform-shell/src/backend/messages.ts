import { existsSync, readFileSync } from "node:fs";
import { join } from "node:path";

import { parseDocument } from "yaml";

/**
 * 读取已装配模块的语言包（装配产物 public/modules/<moduleId>/<version>/i18n）。
 *
 * Shell 通过 `SitemapApp` 的 `messages` 把词条交给页面运行时与模块自定义组件；
 * 缺失时返回空表，组件按 key 回退显示。
 */
const MODULES_DIR = join(process.cwd(), "public/modules");
const DEFAULT_LOCALE = "zh-CN";

function moduleDir(moduleId: string, version?: string): string | null {
  if (version !== undefined) {
    const dir = join(MODULES_DIR, moduleId, version);
    return existsSync(dir) ? dir : null;
  }
  return null;
}

function versionFromAssembly(moduleId: string): string | undefined {
  const file = join(process.cwd(), ".generated/pactor/assembly.json");
  if (!existsSync(file)) return undefined;
  try {
    const data = JSON.parse(readFileSync(file, "utf8")) as {
      modules?: { id?: string; version?: string }[];
    };
    return data.modules?.find((item) => item.id === moduleId)?.version;
  } catch {
    return undefined;
  }
}

function readMessages(dir: string, locale: string): Record<string, string> {
  const file = join(dir, "i18n", locale, "app.yaml");
  if (!existsSync(file)) return {};
  const doc = parseDocument(readFileSync(file, "utf8"));
  if (doc.errors.length > 0) {
    console.warn(`[shell] 语言包解析失败: ${file}`);
    return {};
  }
  const data = doc.toJS() as Record<string, unknown> | null;
  if (data === null || typeof data !== "object") return {};
  const messages: Record<string, string> = {};
  for (const [key, value] of Object.entries(data)) {
    if (typeof value === "string") messages[key] = value;
  }
  return messages;
}

/** 当前语言优先，缺省语言兜底 */
export function loadModuleMessages(
  moduleId: string,
  locale: string,
): Record<string, string> {
  const dir = moduleDir(moduleId, versionFromAssembly(moduleId));
  if (dir === null) return {};
  return {
    ...readMessages(dir, DEFAULT_LOCALE),
    ...readMessages(dir, locale),
  };
}
