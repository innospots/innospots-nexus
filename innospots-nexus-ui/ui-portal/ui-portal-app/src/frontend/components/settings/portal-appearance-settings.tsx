"use client";

import { useEffect } from "react";

import {
  setUiDensity,
  useUiDensity,
  type UiDensity,
} from "../../lib/ui-density";
import {
  applySkinStylesheet,
  selectSkin,
  SKINS,
  useThemeSkin,
} from "../../lib/theme-skin";
import { SettingsPage, SettingsSection } from "./settings-layout";

const DENSITIES: Array<{ value: UiDensity; label: string; hint: string }> = [
  { value: "default", label: "标准", hint: "默认的间距与控件高度" },
  { value: "compact", label: "紧凑", hint: "更小的间距，适合密集信息" },
  { value: "comfortable", label: "宽松", hint: "更大的间距，阅读更轻松" },
];

export function PortalAppearanceSettings() {
  const density = useUiDensity();
  const skin = useThemeSkin();

  useEffect(() => {
    void applySkinStylesheet(skin);
  }, [skin]);

  return (
    <SettingsPage title="界面设置" description="选择主题皮肤并调整信息密度">
      <SettingsSection
        title="主题皮肤"
        hint="来自 Pactor 预置主题包，选择后立即应用并保存在当前浏览器。"
      >
        <div className="portal-skin-grid" role="radiogroup" aria-label="主题皮肤">
          {SKINS.map((theme) => (
            <button
              key={theme.name}
              type="button"
              role="radio"
              aria-checked={skin === theme.name}
              aria-label={theme.label}
              className={
                skin === theme.name
                  ? "portal-skin-card is-active"
                  : "portal-skin-card"
              }
              onClick={() => selectSkin(theme.name)}
            >
              <span
                className="portal-skin-preview"
                style={{ background: theme.color }}
                aria-hidden="true"
              >
                <span className="portal-skin-preview-window">
                  <span
                    className="portal-skin-preview-dot"
                    style={{ background: theme.color }}
                  />
                  <span className="portal-skin-preview-lines">
                    <i />
                    <i />
                    <i />
                  </span>
                </span>
              </span>
              <span className="portal-skin-name">{theme.label}</span>
              <span className="portal-skin-desc">
                {theme.family} · {theme.source}
              </span>
            </button>
          ))}
        </div>
      </SettingsSection>

      <SettingsSection title="界面密度" hint="影响页面间距、控件高度与卡片留白。">
        <div className="portal-choice-group" role="radiogroup" aria-label="界面密度">
          {DENSITIES.map((item) => (
            <button
              key={item.value}
              type="button"
              role="radio"
              aria-checked={density === item.value}
              className={
                density === item.value
                  ? "portal-choice is-active"
                  : "portal-choice"
              }
              onClick={() => setUiDensity(item.value)}
            >
              <span className="portal-choice-label">{item.label}</span>
              <span className="portal-choice-hint">{item.hint}</span>
            </button>
          ))}
        </div>
      </SettingsSection>

      <SettingsSection title="预览" hint="以上设置立即生效。">
        <div className="portal-preview">
          <div className="portal-preview-header">
            <span className="portal-preview-title">示例卡片</span>
            <span className="portal-preview-badge">预览</span>
          </div>
          <p className="portal-preview-text">
            当前皮肤与界面密度会同步应用到导航、表单与内容卡片。
          </p>
          <div className="portal-preview-actions">
            <span className="portal-preview-button">主要操作</span>
            <span className="portal-preview-button is-ghost">次要操作</span>
          </div>
        </div>
      </SettingsSection>
    </SettingsPage>
  );
}
