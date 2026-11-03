"use client";

import { useEffect, useSyncExternalStore } from "react";
import { Check } from "lucide-react";
import { useI18n } from "@pactor-app/runtime";

import { useHostI18n } from "../../i18n/messages";
import {
  fetchConsoleTheme,
  saveConsoleTheme,
  skinToPlatformTheme,
  THEME_TO_SKIN,
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

function applyPlatformTheme(theme: PlatformConsoleTheme) {
  const skin = THEME_TO_SKIN[theme] as SkinValue;
  selectSkin(skin);
}

export function ThemeGallery() {
  const selected = useSyncExternalStore(subscribe, snapshot, () => DEFAULT_SKIN);
  const { t: tCommon } = useI18n();
  const { t } = useHostI18n();

  useEffect(() => {
    let cancelled = false;
    fetchConsoleTheme()
      .then((remote) => {
        if (cancelled || remote == null) {
          return;
        }
        applyPlatformTheme(remote.theme);
      })
      .catch(() => {
        // 无后端时保留本地 mc-skin
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function onSelect(skin: SkinValue) {
    selectSkin(skin);
    const theme = skinToPlatformTheme(skin);
    if (theme == null) {
      return;
    }
    try {
      await saveConsoleTheme(theme);
    } catch {
      // 本地已应用；后端不可达时不阻断
    }
  }

  return (
    <div className="theme-grid">
      {SKINS.map((theme) => (
        <button
          type="button"
          key={theme.value}
          className={`theme-card${selected === theme.value ? " is-selected" : ""}`}
          onClick={() => onSelect(theme.value as SkinValue)}
          aria-pressed={selected === theme.value}
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
            {selected === theme.value ? (
              <span className="theme-selected">
                <Check size={14} aria-hidden="true" /> {t("theme.applied")}
              </span>
            ) : null}
          </div>
          <div className="theme-card-body">
            <div>
              <strong>{tCommon(theme.label)}</strong>
            </div>
            <span className="theme-hex">{theme.color}</span>
          </div>
        </button>
      ))}
    </div>
  );
}
