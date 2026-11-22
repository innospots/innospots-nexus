import type { Metadata } from "next";
import { PactorApp } from "@/frontend/App";
import { getRequestI18n } from "@/backend/pages/i18n";
import { DEFAULT_SKIN, SKINS } from "@/frontend/skin";
// 顺序约定：globals（含 NavigationShell）→ ui 预建样式。主题令牌由 public/themes/<skin>.css
import "./globals.css";
import "@pactor-app/ui/styles.css";

export async function generateMetadata(): Promise<Metadata> {
  const i18n = await getRequestI18n();
  return {
    title: i18n.messages["common.metadata.title"] ?? "Nexus Console",
    description:
      i18n.messages["common.metadata.description"] ?? "Nexus management console",
  };
}

export default async function RootLayout({ children }: LayoutProps<"/">) {
  const i18n = await getRequestI18n();
  const allowedSkins = JSON.stringify(SKINS.map((skin) => skin.value));
  return (
    <html
      lang={i18n.locale}
      suppressHydrationWarning
      className={`h-full antialiased`}
    >
      <head>
        <script
          dangerouslySetInnerHTML={{
            // 首屏渲染前恢复最新主题皮肤（pactor.theme，兼容旧 mc-skin）、
            // 明暗（pactor.mode）与间距密度（pactor.size，data-pactor-size）。
            __html: `(function(){var root=document.documentElement;var legacy={"chenwunaifu":"qing-ming","taijingshenlin":"bi-shan","xuanzhimoyun":"jin-yu"};var allowed=${allowedSkins};var skin=null;try{skin=localStorage.getItem("pactor.theme")}catch(e){}if(!skin){try{skin=localStorage.getItem("mc-skin")}catch(e){}}if(legacy[skin]){skin=legacy[skin]}if(allowed.indexOf(skin)<0){skin=${JSON.stringify(DEFAULT_SKIN)}}var link=document.createElement("link");link.rel="stylesheet";link.href="/themes/"+skin+".css";link.setAttribute("data-pactor-skin",skin);document.head.appendChild(link);try{var size=localStorage.getItem("pactor.size");if(size==="compact"||size==="comfortable"){root.setAttribute("data-pactor-size",size)}}catch(e){}var m=window.matchMedia("(prefers-color-scheme: dark)");function apply(){var mode=null;try{mode=localStorage.getItem("pactor.mode")}catch(e){}if(!mode){try{mode=localStorage.getItem("mc-mode")}catch(e){}}var dark=mode==="dark"||(mode!=="light"&&m.matches);if(dark){root.setAttribute("data-pactor-theme","dark")}else{root.removeAttribute("data-pactor-theme")}}apply();m.addEventListener("change",apply)})()`,
          }}
        />
      </head>
      <body className="h-full flex flex-col">
        {/*
         * PactorApp 挂在 Next layout 而非 page：App Router 导航时 page 会
         * 重挂载、layout 常驻——sitemap 只拉取一次，切换导航只刷新内容区。
         * 登录 / 登出后须 window.location 整页跳转以按新会话重拉 sitemap。
         * children（page）恒为 null，路由由 PactorApp 内部按 sitemap 解析。
         */}
        <PactorApp
          locale={i18n.locale}
          defaultLocale={i18n.defaultLocale}
          messages={i18n.messages}
        />
        {children}
      </body>
    </html>
  );
}
