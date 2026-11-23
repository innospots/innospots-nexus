"use client";

import { useSyncExternalStore } from "react";

export type AiProvider = "volcengine" | "openai" | "custom";

export interface AiConfig {
  provider: AiProvider;
  baseUrl: string;
  model: string;
  apiKey: string;
}

const STORAGE_KEY = "portal.ai.config";
const DEFAULT_CONFIG: AiConfig = {
  provider: "volcengine",
  baseUrl: "https://ark.cn-beijing.volces.com/api/v3",
  model: "",
  apiKey: "",
};
const listeners = new Set<() => void>();
let cached: AiConfig | null = null;

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function readConfig(): AiConfig {
  if (typeof window === "undefined") {
    return DEFAULT_CONFIG;
  }
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return DEFAULT_CONFIG;
    }
    const parsed: unknown = JSON.parse(raw);
    if (!isRecord(parsed)) {
      return DEFAULT_CONFIG;
    }
    const provider =
      parsed.provider === "openai" || parsed.provider === "custom"
        ? parsed.provider
        : "volcengine";
    return {
      provider,
      baseUrl:
        typeof parsed.baseUrl === "string" ? parsed.baseUrl : DEFAULT_CONFIG.baseUrl,
      model: typeof parsed.model === "string" ? parsed.model : "",
      apiKey: typeof parsed.apiKey === "string" ? parsed.apiKey : "",
    };
  } catch {
    return DEFAULT_CONFIG;
  }
}

export function getAiConfig(): AiConfig {
  if (cached === null) {
    cached = readConfig();
  }
  return cached;
}

/** 保存 AI 偏好（当前仅存浏览器本地，后续接服务端配置）。 */
export function saveAiConfig(config: AiConfig): void {
  cached = config;
  if (typeof window !== "undefined") {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(config));
  }
  for (const listener of listeners) {
    listener();
  }
}

function subscribe(listener: () => void): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

/** 读取并订阅 AI 配置（服务端渲染回退默认值）。 */
export function useAiConfig(): AiConfig {
  return useSyncExternalStore(subscribe, getAiConfig, () => DEFAULT_CONFIG);
}
