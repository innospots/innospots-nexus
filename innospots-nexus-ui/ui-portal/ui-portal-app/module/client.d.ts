import type { ActionHandler } from "@pactor-app/runtime";
import type { ComponentType } from "react";

/**
 * Shell 装配期类型契约：`<pkg>/client` 只发布构建产物（dist-module），
 * 由本声明文件提供类型。
 */
export declare const extensions: {
  components: Record<string, ComponentType<unknown>>;
  actions: Record<string, ActionHandler>;
};
