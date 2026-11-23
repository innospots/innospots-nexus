// 模块自带样式：装配器按各模块 manifest 的 styles 生成 import。
import "@generated/pactor/module-styles";

// 皮肤 / 主题令牌由 Shell 统一提供。
import "@/modules/styles/theme.css";

// AppShell 外壳与 SkinSwitcher 样式。
import "@pactor-app/shell/styles.css";

import type { ReactNode } from "react";
import { cookies } from "next/headers";

import { loadModuleMessages } from "@/backend/messages";
import { WorkspaceShell } from "@/frontend/WorkspaceShell";

/**
 * 工作区 layout 挂载 WorkspaceShell（AppShell + SitemapApp，跨导航常驻）。
 * 具体 URL 是否合法由 [...slug] page 在服务端用装配路由表校验。
 */
export default async function WorkspaceLayout({
  children,
}: {
  children: ReactNode;
}) {
  const store = await cookies();
  const locale = store.get("locale")?.value === "en-US" ? "en-US" : "zh-CN";
  const messages = loadModuleMessages("portal", locale);

  return (
    <>
      <WorkspaceShell locale={locale} messages={messages} />
      {children}
    </>
  );
}
