/**
 * UI 模块客户端入口：供 Web Shell 构建期静态 import（exports ./client）。
 * 样式由 `pactor module build` 预编译 `src/app/globals.css` 输出到
 * `dist-module/client/index.css`，Shell 通过 `<pkg>/styles.css` 引入。
 */
import type { ActionHandler } from "@pactor-app/runtime";
import type { ComponentType } from "react";

import { pactorActions } from "../src/frontend/actions";
import { pactorComponents } from "../src/frontend/components";

export const extensions = {
  components: pactorComponents as Record<string, ComponentType<unknown>>,
  actions: pactorActions as Record<string, ActionHandler>,
};
