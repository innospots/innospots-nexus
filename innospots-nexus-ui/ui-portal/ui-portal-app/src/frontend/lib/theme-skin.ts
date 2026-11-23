"use client";

import { useSyncExternalStore } from "react";

import {
  DEFAULT_THEME,
  THEMES,
  THEME_STORAGE_KEY,
  applyTheme,
  isThemeName,
  type ThemeDefinition,
  type ThemeName,
} from "@pactor-app/theme";

/** Pactor 预置皮肤清单（来自 @pactor-app/theme / packages/theme）。 */
export const SKINS = THEMES;
export const DEFAULT_SKIN = DEFAULT_THEME;
export const SKIN_STORAGE_KEY = THEME_STORAGE_KEY;
export const PREFERENCES_CHANGE_EVENT = "pactor-preferences-change";

export type SkinValue = ThemeName;
export type SkinDef = ThemeDefinition;

const listeners = new Set<() => void>();

export function resolveSkin(value: string | null | undefined): SkinValue {
  return isThemeName(value) ? value : DEFAULT_SKIN;
}

/** 选择皮肤：写入 localStorage 并通知订阅者。 */
export function selectSkin(skin: SkinValue): void {
  if (typeof window === "undefined") {
    return;
  }
  window.localStorage.setItem(SKIN_STORAGE_KEY, skin);
  window.dispatchEvent(new Event(PREFERENCES_CHANGE_EVENT));
  for (const listener of listeners) {
    listener();
  }
}

/**
 * 应用皮肤：由主题包懒加载并注入 <style>，随后移除首屏预置的 <link>，
 * 避免同一皮肤的双份 CSS。
 */
export async function applySkinStylesheet(skin: string): Promise<void> {
  const next = resolveSkin(skin);
  document.documentElement.setAttribute("data-requested-skin", next);
  try {
    await applyTheme(next, {
      storageKey: SKIN_STORAGE_KEY,
      persist: false,
    });
    document
      .querySelectorAll("link[data-pactor-skin]")
      .forEach((link) => link.remove());
  } catch {
    // 主题 chunk 加载失败时保留首屏 link，下一次切换会重试。
  }
}

function subscribe(listener: () => void): () => void {
  listeners.add(listener);
  const onChange = () => listener();
  window.addEventListener("storage", onChange);
  window.addEventListener(PREFERENCES_CHANGE_EVENT, onChange);
  return () => {
    listeners.delete(listener);
    window.removeEventListener("storage", onChange);
    window.removeEventListener(PREFERENCES_CHANGE_EVENT, onChange);
  };
}

function snapshot(): SkinValue {
  return resolveSkin(window.localStorage.getItem(SKIN_STORAGE_KEY));
}

/** 读取并订阅当前皮肤（服务端渲染回退默认皮肤）。 */
export function useThemeSkin(): SkinValue {
  return useSyncExternalStore(subscribe, snapshot, () => DEFAULT_SKIN);
}
