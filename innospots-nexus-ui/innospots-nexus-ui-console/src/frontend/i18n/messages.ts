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
  "gate.checking": "正在校验页面访问权限…",
  "gate.redirecting": "正在跳转登录页…",
  "gate.denied.title": "无访问权限",
  "gate.denied.retry": "重新校验",
  "gate.denied.back": "返回上一页",
  "gate.denied.home": "回到首页",
  "gate.denied.signOut": "退出登录",
  "gate.denied.noAccess": "无页面访问权限",
  "gate.error.http": "页面权限校验失败：HTTP {{status}}",
  "gate.error.unavailable": "页面权限校验服务不可用：{{message}}",
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
  "gate.checking": "Checking page access…",
  "gate.redirecting": "Redirecting to sign in…",
  "gate.denied.title": "Access denied",
  "gate.denied.retry": "Retry",
  "gate.denied.back": "Back",
  "gate.denied.home": "Home",
  "gate.denied.signOut": "Sign out",
  "gate.denied.noAccess": "No permission to access this page",
  "gate.error.http": "Page access check failed: HTTP {{status}}",
  "gate.error.unavailable": "Page access service unavailable: {{message}}",
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

/** 页面访问门禁文案（不依赖 Pactor Provider）。 */
export function translateGate(
  locale: ConsoleLocale | null | undefined,
  key: MessageKey,
  params?: Record<string, string | number>,
): string {
  const resolved: ConsoleLocale = locale === "en-US" ? "en-US" : "zh-CN";
  return interpolate(
    tables[resolved][key] ?? tables["zh-CN"][key] ?? key,
    params,
  );
}
