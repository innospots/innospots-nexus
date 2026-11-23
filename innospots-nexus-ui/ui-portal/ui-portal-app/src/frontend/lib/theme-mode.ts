"use client";

import { useSyncExternalStore } from "react";

export type ThemeMode = "light" | "dark" | "system";

const STORAGE_KEY = "pactor.mode";
const MODES: ThemeMode[] = ["light", "dark", "system"];
const listeners = new Set<() => void>();

function isThemeMode(value: string | null): value is ThemeMode {
  return value !== null && (MODES as string[]).includes(value);
}

export function getThemeMode(): ThemeMode {
  if (typeof window === "undefined") {
    return "system";
  }
  const value = window.localStorage.getItem(STORAGE_KEY);
  return isThemeMode(value) ? value : "system";
}

/** 写入偏好并立即应用到 document。 */
export function setThemeMode(mode: ThemeMode): void {
  const root = document.documentElement;
  if (mode === "system") {
    root.removeAttribute("data-pactor-theme");
    window.localStorage.removeItem(STORAGE_KEY);
  } else {
    root.setAttribute("data-pactor-theme", mode);
    window.localStorage.setItem(STORAGE_KEY, mode);
  }
  for (const listener of listeners) {
    listener();
  }
}

function subscribe(listener: () => void): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

/** 读取并订阅当前外观模式（服务端渲染回退 system）。 */
export function useThemeMode(): ThemeMode {
  return useSyncExternalStore(subscribe, getThemeMode, () => "system");
}
