/**
 * UI 模块客户端入口：供 Web Shell 构建期静态 import（exports ./client）。
 * 样式由 `pactor module build` 预编译 `src/app/globals.css`（含 Tailwind）
 * 输出到 `dist-module/client/index.css`，Shell 通过 `<pkg>/styles.css` 引入。
 */
import type { ActionHandler } from "@pactor-app/runtime";
import type { ComponentType } from "react";

import { pactorActions } from "../src/frontend/actions";
import { pactorComponents } from "../src/frontend/components";

/**
 * 组件注册表按 DSL 的 `type` 动态注入 props，组件自身声明的必填 props
 * 无法在静态类型上统一为 unknown；在模块边界做一次收窄。
 */
export const extensions = {
  components: pactorComponents as Record<string, ComponentType<unknown>>,
  actions: pactorActions as Record<string, ActionHandler>,
};

/** 供 Shell 宿主挂载控制台布局/站点名称上下文（皮肤仍由 Shell 管理）。 */
export { NavigationLayoutProvider } from "../src/frontend/navigation-layout-context";
