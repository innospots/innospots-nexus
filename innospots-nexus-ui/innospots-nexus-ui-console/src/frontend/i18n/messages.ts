"use client";

import { useI18n } from "@pactor-app/runtime";

import type { ConsoleLocale } from "../locale";

const zhCN = {
  "nav.main": "主导航",
  "nav.moduleMenu": "{{name}}菜单",
  "account.menu": "账户菜单",
  "account.label": "账户",
  "account.popover": "账户和偏好设置",
  "account.defaultUser": "用户",
  "account.changePassword": "修改密码",
  "account.language": "切换语言",
  "account.colorTheme": "配色主题",
  "account.appearance": "主题",
  "account.light": "浅色",
  "account.dark": "深色",
  "account.system": "跟随系统",
  "account.signOut": "退出登录",
  "account.signOutError": "退出失败，请重试。",
  "account.languageZh": "中文",
  "account.languageEn": "English",
  "theme.applied": "已应用",
} as const;

type MessageKey = keyof typeof zhCN;

const enUS: Record<MessageKey, string> = {
  "nav.main": "Main navigation",
  "nav.moduleMenu": "{{name}} menu",
  "account.menu": "Account menu",
  "account.label": "Account",
  "account.popover": "Account and preferences",
  "account.defaultUser": "User",
  "account.changePassword": "Change password",
  "account.language": "Language",
  "account.colorTheme": "Color theme",
  "account.appearance": "Appearance",
  "account.light": "Light",
  "account.dark": "Dark",
  "account.system": "System",
  "account.signOut": "Sign out",
  "account.signOutError": "Could not sign out. Try again.",
  "account.languageZh": "中文",
  "account.languageEn": "English",
  "theme.applied": "Applied",
};

const tables: Record<ConsoleLocale, Record<MessageKey, string>> = {
  "zh-CN": zhCN,
  "en-US": enUS,
};

function interpolate(
  template: string,
  params?: Record<string, string | number>,
): string {
  if (!params) {
    return template;
  }
  return template.replace(/\{\{(\w+)\}\}/g, (raw, name: string) =>
    params[name] !== undefined ? String(params[name]) : raw,
  );
}

/** 外壳组件文案（NavigationShell / AccountMenu / ThemeGallery）。 */
export function useHostI18n(): {
  locale: ConsoleLocale;
  changeLocale: (next: string) => Promise<void>;
  t: (key: MessageKey, params?: Record<string, string | number>) => string;
} {
  const { locale: rawLocale } = useI18n();
  const locale: ConsoleLocale =
    rawLocale === "en-US" ? "en-US" : "zh-CN";

  async function changeLocale(next: string) {
    const trimmed = next.trim();
    if (trimmed.length === 0) {
      return;
    }
    const response = await fetch("/api/i18n/locale", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({ locale: trimmed }),
    });
    if (response.ok) {
      window.location.reload();
    }
  }

  return {
    locale,
    changeLocale,
    t: (key, params) =>
      interpolate(tables[locale][key] ?? tables["zh-CN"][key] ?? key, params),
  };
}
