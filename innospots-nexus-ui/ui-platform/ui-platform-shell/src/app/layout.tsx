import type { Metadata } from "next";
import type { ReactNode } from "react";

import { SkinHeadScript } from "@pactor-app/shell/client";

import "./globals.css";
// 聚合样式入口（ui → blocks → chart → chat）：模块页面
// 使用 chart 组件，必须引入聚合样式而不是仅 ui 分表。
import "pactor-app/styles.css";

export const metadata: Metadata = {
  title: "InnoSpots Web Shell",
  description: "统一 Pactor 模块化 Web 入口",
};

export default function RootLayout({
  children,
}: {
  children: ReactNode;
}) {
  return (
    <html lang="zh-CN" suppressHydrationWarning className="h-full antialiased">
      <head>
        {/* 旧版 mc-skin / mc-mode 迁移到 Shell 偏好键，并恢复密度（防首屏闪烁） */}
        <script
          dangerouslySetInnerHTML={{
            __html: `(function(){try{var t=localStorage.getItem("pactor.theme");if(!t){var l=localStorage.getItem("mc-skin");if(l){localStorage.setItem("pactor.theme",l)}}var m=localStorage.getItem("pactor.mode");if(!m){var lm=localStorage.getItem("mc-mode");if(lm){localStorage.setItem("pactor.mode",lm)}}var s=localStorage.getItem("pactor.size");if(s==="compact"||s==="comfortable"){document.documentElement.setAttribute("data-pactor-size",s)}}catch(e){}})()`,
          }}
        />
        {/*
          首屏防闪烁：恢复持久化皮肤 <link> 与 light/dark 属性。
          无持久化选择时使用宿主基础皮肤 —— 已更新为 @pactor-app/theme
          的最新默认色系 qing-hui（青灰冷调，见 modules/styles/theme.css）。
        */}
        <SkinHeadScript />
      </head>
      <body className="flex min-h-full flex-col bg-zinc-50 text-zinc-900">
        {children}
      </body>
    </html>
  );
}
