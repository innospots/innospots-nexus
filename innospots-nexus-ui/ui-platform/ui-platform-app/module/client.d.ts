import type { ActionHandler } from "@pactor-app/runtime";
import type { ComponentType, ReactNode } from "react";

/**
 * Shell 装配期类型契约：`<pkg>/client` 只发布构建产物（dist-module），
 * 由本声明文件提供类型，避免消费方通过 allowJs 推断打包后的实现。
 */
export declare const extensions: {
  components: Record<string, ComponentType<unknown>>;
  actions: Record<string, ActionHandler>;
};

export declare function NavigationLayoutProvider(props: {
  children: ReactNode;
  themeSync?: boolean;
}): ReactNode;
