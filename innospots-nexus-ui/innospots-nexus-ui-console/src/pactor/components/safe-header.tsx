"use client";

import {
  Header,
  type HeaderProps,
  type HeaderUser,
} from "@pactor-app/ui/components";
import { useI18n } from "@pactor-app/runtime";

function resolveDisplayName(user: HeaderUser, fallback: string): string {
  if (typeof user.name === "string" && user.name.trim().length > 0) {
    return user.name.trim();
  }
  const extended = user as HeaderUser & { username?: string };
  if (typeof extended.username === "string" && extended.username.trim().length > 0) {
    return extended.username.trim();
  }
  return fallback;
}

/**
 * Header 包装：@pactor-app/ui 在 user.name 为空时仍渲染头像并调用 charAt(0)。
 * 布局 DSL 在 session 未就绪或仅有 username 时提供稳定展示名。
 */
export function SafeHeader(props: HeaderProps) {
  const { t } = useI18n();
  const { user, ...rest } = props;
  if (user === undefined) {
    return <Header {...rest} />;
  }
  return (
    <Header
      {...rest}
      user={{
        ...user,
        name: resolveDisplayName(user, t("common.ui.user")),
      }}
    />
  );
}
