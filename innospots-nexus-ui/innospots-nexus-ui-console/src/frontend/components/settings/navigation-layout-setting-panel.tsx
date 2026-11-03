"use client";

import { useEffect, useState } from "react";

import {
  fetchNavigationLayoutMode,
  saveNavigationLayoutMode,
  type PlatformNavigationLayoutMode,
} from "../../lib/platform-console-settings";

const OPTIONS: { value: PlatformNavigationLayoutMode; label: string }[] = [
  { value: "RAIL_DUAL", label: "左轨双栏" },
  { value: "SINGLE_GROUP", label: "单栏分组" },
];

function notifyLayoutChange() {
  window.dispatchEvent(new Event("pactor-console-settings-change"));
}

/**
 * 系统设置页 — 导航布局（平台持久化 + 保存后刷新 NavigationShell）。
 */
export function NavigationLayoutSettingPanel() {
  const [mode, setMode] = useState<PlatformNavigationLayoutMode>("RAIL_DUAL");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchNavigationLayoutMode()
      .then((value) => {
        if (!cancelled) {
          setMode(value);
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function onChange(next: PlatformNavigationLayoutMode) {
    setSaving(true);
    setError(null);
    setMode(next);
    try {
      await saveNavigationLayoutMode(next);
      notifyLayoutChange();
    } catch {
      setError("保存失败，请重试。");
      const previous = await fetchNavigationLayoutMode();
      setMode(previous);
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="system-setting navigation-layout-setting">
      <div>
        <strong>导航布局</strong>
        <p className="system-setting-hint">左轨双栏为模块左轨 + 侧栏；单栏分组为单列分组菜单。</p>
      </div>
      <div className="navigation-layout-options" role="radiogroup" aria-label="导航布局">
        {OPTIONS.map((option) => (
          <label key={option.value} className="navigation-layout-option">
            <input
              type="radio"
              name="navigationLayoutMode"
              value={option.value}
              checked={mode === option.value}
              disabled={loading || saving}
              onChange={() => onChange(option.value)}
            />
            <span>{option.label}</span>
          </label>
        ))}
      </div>
      {error ? (
        <p className="navigation-layout-error" role="alert">
          {error}
        </p>
      ) : null}
    </div>
  );
}
