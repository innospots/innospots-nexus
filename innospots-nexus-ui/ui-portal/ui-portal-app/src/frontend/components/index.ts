"use client";

/**
 * 页面 DSL 引用的自定义组件注册表，经 SitemapApp 的 `components` prop 全局注册。
 */
import { PortalLoginForm, PortalRegisterForm } from "./auth/portal-auth-forms";
import { PortalShell } from "./layout/portal-shell";
import { PortalAiSettings } from "./settings/portal-ai-settings";
import { PortalAppearanceSettings } from "./settings/portal-appearance-settings";
import { PortalProfileSettings } from "./settings/portal-profile-settings";

export const pactorComponents: Record<string, unknown> = {
  PortalLoginForm,
  PortalRegisterForm,
  PortalProfileSettings,
  PortalAppearanceSettings,
  PortalAiSettings,
  PortalShell,
};
