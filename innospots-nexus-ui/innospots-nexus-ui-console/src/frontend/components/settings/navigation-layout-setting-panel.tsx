"use client";

import { useEffect, useState, type ReactElement } from "react";
import { Check } from "lucide-react";
import { useI18n } from "@pactor-app/runtime";

import {
  fetchNavigationLayoutMode,
  saveNavigationLayoutMode,
  type PlatformNavigationLayoutMode,
} from "../../lib/platform-console-settings";
import { persistNavigationLayoutMode } from "../../navigation-layout-context";
import { notifyConsoleSettingsChange } from "../../lib/platform-console-settings";

function notifyLayoutChange() {
  notifyConsoleSettingsChange();
}

function RailDualLayoutIcon() {
  return (
    <svg viewBox="0 0 120 72" className="navigation-layout-icon" aria-hidden="true">
      <rect x="6" y="10" width="16" height="52" rx="3" fill="currentColor" opacity="0.22" />
      <rect x="28" y="10" width="28" height="52" rx="3" fill="currentColor" opacity="0.12" />
      <rect x="62" y="10" width="52" height="52" rx="4" fill="currentColor" opacity="0.06" />
      <rect x="8" y="14" width="8" height="8" rx="2" fill="currentColor" opacity="0.45" />
      <rect x="8" y="26" width="8" height="8" rx="2" fill="currentColor" opacity="0.35" />
      <rect x="8" y="38" width="8" height="8" rx="2" fill="currentColor" opacity="0.35" />
      <rect x="32" y="16" width="20" height="4" rx="1" fill="currentColor" opacity="0.35" />
      <rect x="32" y="24" width="16" height="4" rx="1" fill="currentColor" opacity="0.25" />
      <rect x="32" y="32" width="18" height="4" rx="1" fill="currentColor" opacity="0.25" />
      <rect x="68" y="18" width="36" height="5" rx="1" fill="currentColor" opacity="0.18" />
      <rect x="68" y="28" width="28" height="4" rx="1" fill="currentColor" opacity="0.12" />
    </svg>
  );
}

function SingleGroupLayoutIcon() {
  return (
    <svg viewBox="0 0 120 72" className="navigation-layout-icon" aria-hidden="true">
      <rect x="10" y="10" width="100" height="52" rx="4" fill="currentColor" opacity="0.06" />
      <rect x="14" y="16" width="52" height="5" rx="1" fill="currentColor" opacity="0.35" />
      <rect x="22" y="26" width="40" height="4" rx="1" fill="currentColor" opacity="0.22" />
      <rect x="22" y="33" width="36" height="4" rx="1" fill="currentColor" opacity="0.18" />
      <rect x="22" y="40" width="38" height="4" rx="1" fill="currentColor" opacity="0.18" />
      <rect x="14" y="48" width="48" height="5" rx="1" fill="currentColor" opacity="0.35" />
      <rect x="22" y="58" width="34" height="4" rx="1" fill="currentColor" opacity="0.18" />
      <path
        d="M18 18.5 L16 18.5 L16 20.5 L18 20.5 Z M18 50.5 L16 50.5 L16 52.5 L18 52.5 Z"
        fill="currentColor"
        opacity="0.45"
      />
    </svg>
  );
}

const OPTIONS: {
  value: PlatformNavigationLayoutMode;
  titleKey: string;
  descriptionKey: string;
  Icon: () => ReactElement;
}[] = [
  {
    value: "RAIL_DUAL",
    titleKey: "common.systemSettings.navigationLayout.railDual.title",
    descriptionKey: "common.systemSettings.navigationLayout.railDual.description",
    Icon: RailDualLayoutIcon,
  },
  {
    value: "SINGLE_GROUP",
    titleKey: "common.systemSettings.navigationLayout.singleGroup.title",
    descriptionKey: "common.systemSettings.navigationLayout.singleGroup.description",
    Icon: SingleGroupLayoutIcon,
  },
];

/**
 * 系统设置页 — 导航布局（平台持久化 + 保存后刷新 NavigationShell）。
 */
export function NavigationLayoutSettingPanel() {
  const { t } = useI18n();
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

  async function onSelect(next: PlatformNavigationLayoutMode) {
    if (next === mode || loading || saving) {
      return;
    }
    setSaving(true);
    setError(null);
    setMode(next);
    try {
      await saveNavigationLayoutMode(next);
      persistNavigationLayoutMode(next);
      notifyLayoutChange();
    } catch {
      setError(t("common.systemSettings.navigationLayout.saveError"));
      const previous = await fetchNavigationLayoutMode();
      setMode(previous);
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="navigation-layout-setting">
      <div className="navigation-layout-setting-header">
        <strong>{t("common.systemSettings.navigationLayout.label")}</strong>
        <p className="system-setting-hint">
          {t("common.systemSettings.navigationLayout.description")}
        </p>
      </div>
      <div
        className="navigation-layout-card-grid"
        role="radiogroup"
        aria-label={t("common.systemSettings.navigationLayout.label")}
      >
        {OPTIONS.map((option) => {
          const selected = mode === option.value;
          const { Icon } = option;
          return (
            <button
              type="button"
              key={option.value}
              className={`navigation-layout-card${selected ? " is-selected" : ""}`}
              aria-pressed={selected}
              disabled={loading || saving}
              onClick={() => onSelect(option.value)}
            >
              <div className="navigation-layout-card-preview">
                <Icon />
                {selected ? (
                  <span className="navigation-layout-card-selected">
                    <Check size={12} aria-hidden="true" />
                  </span>
                ) : null}
              </div>
              <div className="navigation-layout-card-body">
                <strong>{t(option.titleKey)}</strong>
                <p>{t(option.descriptionKey)}</p>
              </div>
            </button>
          );
        })}
      </div>
      {error ? (
        <p className="navigation-layout-error" role="alert">
          {error}
        </p>
      ) : null}
    </div>
  );
}
