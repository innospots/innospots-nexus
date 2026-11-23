import type { Metadata } from "next";
import type { ReactNode } from "react";

import { DEFAULT_THEME, THEMES, THEME_STORAGE_KEY } from "@pactor-app/theme";

import "./globals.css";
import "pactor-app/styles.css";

export const metadata: Metadata = {
  title: "Nexus Portal",
  description: "Nexus 租户用户门户",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  const allowedThemes = JSON.stringify(THEMES.map((theme) => theme.name));
  const themeStorageKey = JSON.stringify(THEME_STORAGE_KEY);
  return (
    <html lang="zh-CN" suppressHydrationWarning className="h-full antialiased">
      <head>
        <script
          dangerouslySetInnerHTML={{
            __html: `(function(){try{var root=document.documentElement;var allowed=${allowedThemes};var skin;try{skin=localStorage.getItem(${themeStorageKey})}catch(e){}if(allowed.indexOf(skin)<0){skin=${JSON.stringify(DEFAULT_THEME)}}root.setAttribute("data-requested-skin",skin);root.setAttribute("data-pactor-skin",skin);var link=document.createElement("link");link.rel="stylesheet";link.href="/themes/"+skin+".css";link.setAttribute("data-pactor-skin",skin);document.head.appendChild(link);var mode=localStorage.getItem("pactor.mode");var m=window.matchMedia("(prefers-color-scheme: dark)");function apply(){var dark=mode==="dark"||(mode!=="light"&&m.matches);if(dark){root.setAttribute("data-pactor-theme","dark")}else{root.removeAttribute("data-pactor-theme")}}apply();m.addEventListener("change",function(){if(!mode){apply()}});var size=localStorage.getItem("pactor.size");if(size==="compact"||size==="comfortable"){root.setAttribute("data-pactor-size",size)}}catch(e){}})()`,
          }}
        />
      </head>
      <body className="flex min-h-full flex-col bg-zinc-50 text-zinc-900">
        {children}
      </body>
    </html>
  );
}
