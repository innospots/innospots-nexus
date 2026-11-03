"use client";

import { useEffect, useState, useSyncExternalStore } from "react";
import { Check } from "lucide-react";

import { useI18n } from "@pactor-app/runtime";

import { useHostI18n } from "../../i18n/messages";
import {
  applyConsoleThemeFromRemote,
  saveConsoleTheme,
  skinToPlatformTheme,
  type PlatformConsoleTheme,
} from "../../lib/platform-console-settings";
import {
  DEFAULT_SKIN,
  PREFERENCES_CHANGE_EVENT,
  resolveSkin,
  selectSkin,
  SKINS,
  SKIN_STORAGE_KEY,
  type SkinValue,
} from "../../skin";
import { themeText } from "../../themes";

function subscribe(onChange: () => void) {
  window.addEventListener("storage", onChange);
  window.addEventListener(PREFERENCES_CHANGE_EVENT, onChange);
  return () => {
    window.removeEventListener("storage", onChange);
    window.removeEventListener(PREFERENCES_CHANGE_EVENT, onChange);
  };
}

function snapshot() {
  return resolveSkin(localStorage.getItem(SKIN_STORAGE_KEY));
}

export interface ThemeGalleryProps {
  theme?: PlatformConsoleTheme | string;
  skinKey?: string;
  loading?: boolean | string;
}

function isLoadingFlag(value: boolean | string | undefined): boolean {
  if (value === true) {
    return true;
  }
  if (typeof value === "string") {
    return value === "true" || value === "1";
  }
  return false;
}

export function ThemeGallery({
  theme: remoteTheme,
  skinKey: remoteSkinKey,
  loading: loadingProp,
}: ThemeGalleryProps) {
  const selected = useSyncExternalStore(subscribe, snapshot, () => DEFAULT_SKIN);
  const { locale, t: tHost } = useHostI18n();
  const { t } = useI18n();
  const loading = isLoadingFlag(loadingProp);
  const [savingSkin, setSavingSkin] = useState<SkinValue | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);

  useEffect(() => {
    if (loading) {
      return;
    }
    if (
      (remoteSkinKey && remoteSkinKey.trim().length > 0) ||
      (remoteTheme && String(remoteTheme).trim().length > 0)
    ) {
      applyConsoleThemeFromRemote({
        skinKey: remoteSkinKey,
        theme: remoteTheme,
      });
    }
  }, [loading, remoteSkinKey, remoteTheme]);

  async function onSelect(skin: SkinValue) {
    setSaveError(null);
    selectSkin(skin);
    const theme = skinToPlatformTheme(skin);
    if (theme == null) {
      return;
    }
    setSavingSkin(skin);
    try {
      await saveConsoleTheme(theme);
    } catch (error) {
      setSaveError(
        error instanceof Error ? error.message : t("common.themePage.saveError"),
      );
    } finally {
      setSavingSkin(null);
    }
  }

  if (loading) {
    return (
      <p className="theme-gallery-status" role="status">
        {t("common.themePage.loading")}
      </p>
    );
  }

  return (
    <>
      {saveError ? (
        <p className="theme-gallery-error" role="alert">
          {saveError}
        </p>
      ) : null}
      <div className="theme-grid">
        {SKINS.map((theme) => {
          const text = themeText(theme, locale);
          const isSelected = selected === theme.value;
          const isSaving = savingSkin === theme.value;
          return (
            <button
              type="button"
              key={theme.value}
              className={`theme-card${isSelected ? " is-selected" : ""}${isSaving ? " is-saving" : ""}`}
              disabled={savingSkin != null}
              onClick={() => void onSelect(theme.value)}
              aria-pressed={isSelected}
              aria-busy={isSaving}
            >
              <div className="theme-preview" style={{ backgroundColor: theme.color }}>
                <div className="theme-preview-window">
                  <span style={{ backgroundColor: theme.color }} />
                  <div>
                    <i />
                    <i />
                    <i />
                  </div>
                </div>
                {isSelected ? (
                  <span className="theme-selected">
                    <Check size={14} aria-hidden="true" /> {tHost("theme.applied")}
                  </span>
                ) : null}
              </div>
              <div className="theme-card-body">
                <div>
                  <strong>{text.label}</strong>
                  <p>
                    {text.family} · {text.source}
                  </p>
                </div>
                <span className="theme-hex">{theme.color}</span>
              </div>
            </button>
          );
        })}
      </div>
    </>
  );
}
