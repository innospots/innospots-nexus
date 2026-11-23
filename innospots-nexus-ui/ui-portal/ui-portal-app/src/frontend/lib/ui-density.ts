"use client";

import { useSyncExternalStore } from "react";

export type UiDensity = "default" | "compact" | "comfortable";

const STORAGE_KEY = "pactor.size";
const DENSITIES: UiDensity[] = ["default", "compact", "comfortable"];
const listeners = new Set<() => void>();

function isDensity(value: string | null): value is UiDensity {
  return value !== null && (DENSITIES as string[]).includes(value);
}

export function getUiDensity(): UiDensity {
  if (typeof window === "undefined") {
    return "default";
  }
  const value = window.localStorage.getItem(STORAGE_KEY);
  return isDensity(value) ? value : "default";
}

export function setUiDensity(density: UiDensity): void {
  const root = document.documentElement;
  if (density === "default") {
    root.removeAttribute("data-pactor-size");
    window.localStorage.removeItem(STORAGE_KEY);
  } else {
    root.setAttribute("data-pactor-size", density);
    window.localStorage.setItem(STORAGE_KEY, density);
  }
  for (const listener of listeners) {
    listener();
  }
}

function subscribe(listener: () => void): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

/** 读取并订阅界面密度（服务端渲染回退 default）。 */
export function useUiDensity(): UiDensity {
  return useSyncExternalStore(subscribe, getUiDensity, () => "default");
}
