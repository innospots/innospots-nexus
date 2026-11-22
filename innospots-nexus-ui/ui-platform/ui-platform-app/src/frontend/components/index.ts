"use client";

/**
 * 页面 DSL 引用的自定义组件注册表（页面级自定义组件
 * `components={{...}}`），经 SitemapApp 的 `components` prop 全局注册。
 */
import { NavigationShell } from "./layout/navigation-shell";
import { ThemeGallery } from "./settings/theme-settings";
import { NavigationLayoutSettingPanel } from "./settings/navigation-layout-setting-panel";
import { ConsoleTimezoneSettingRow } from "./settings/console-timezone-setting-row";
import { CatalogTree } from "./settings/catalog-tree";
import { ConsoleMenuTree } from "./settings/console-menu-tree";
import { PermissionGrantPanel } from "./settings/permission-grant-panel";
import { DictionaryTypeSavePanel } from "./settings/dictionary-type-save-panel";
import { DictionaryTypeViewPanel } from "./settings/dictionary-type-view-panel";
import { PlatformUserCreatePanel } from "./platform/platform-user-create-panel";
import { PlatformUserPasswordResetPanel } from "./platform/platform-user-password-reset-panel";
import { RoleMemberAddPanel } from "./role/role-member-add-panel";
import { FilterSelect } from "./shared/filter-select";
import { DrawerBackdrop } from "./shared/drawer-backdrop";
import { I18nText } from "./shared/i18n-text";
import { StatusBadge } from "./shared/status-badge";
import { LogStatus } from "./shared/log-status";
import { DebugChat } from "./shared/debug-chat";
import { CardLink, StatusToggleIcon, TextLink } from "./shared/card-links";
import {
  PlatformAccessRegistrationForm,
  PlatformInviteAcceptForm,
  PlatformInviteActivateForm,
  PlatformLoginForm,
  PlatformOpenRegistrationForm,
  PlatformPasswordChangeForm,
  PlatformPasswordResetForm,
  PlatformPendingApprovalPage,
} from "./auth/platform-auth-forms";

export const pactorComponents: Record<string, unknown> = {
  CatalogTree,
  ConsoleMenuTree,
  PermissionGrantPanel,
  FilterSelect,
  DrawerBackdrop,
  DictionaryTypeSavePanel,
  DictionaryTypeViewPanel,
  PlatformUserCreatePanel,
  PlatformUserPasswordResetPanel,
  RoleMemberAddPanel,
  PlatformLoginForm,
  PlatformPasswordResetForm,
  PlatformPasswordChangeForm,
  PlatformPendingApprovalPage,
  PlatformInviteAcceptForm,
  PlatformInviteActivateForm,
  PlatformOpenRegistrationForm,
  PlatformAccessRegistrationForm,
  I18nText,
  StatusBadge,
  LogStatus,
  DebugChat,
  ProviderCardLink: CardLink,
  ModelCardLink: CardLink,
  RouteCardLink: CardLink,
  TemplateCardLink: CardLink,
  TextLink,
  StatusToggleIcon,
  NavigationShell,
  ThemeGallery,
  NavigationLayoutSettingPanel,
  ConsoleTimezoneSettingRow,
};
