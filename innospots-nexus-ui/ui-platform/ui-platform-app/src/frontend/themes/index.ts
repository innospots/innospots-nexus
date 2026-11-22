/**
 * 主题目录：直接使用 `@pactor-app/theme` 的最新 14 套配色（含默认 qing-hui），
 * 与 Shell 的 SkinHeadScript / SkinGallery 共用同一份皮肤清单。
 */
import {
  DEFAULT_THEME,
  THEMES as PACTOR_THEMES,
  type ThemeName,
} from "@pactor-app/theme";

export interface Theme {
  value: ThemeName;
  label: string;
  labelEn: string;
  family: string;
  familyEn: string;
  color: string;
  source: string;
  sourceEn: string;
}

export const THEMES: readonly Theme[] = PACTOR_THEMES.map((theme) => ({
  value: theme.name,
  label: theme.label,
  labelEn: theme.labelEn,
  family: theme.family,
  familyEn: theme.familyEn,
  color: theme.color,
  source: theme.source,
  sourceEn: theme.sourceEn,
}));

export { DEFAULT_THEME };

export function themeText(theme: Theme, locale: string) {
  const english = locale === "en-US";
  return {
    label: english ? theme.labelEn : theme.label,
    family: english ? theme.familyEn : theme.family,
    source: english ? theme.sourceEn : theme.source,
  };
}
