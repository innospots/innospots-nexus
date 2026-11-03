/**
 * UI 模块客户端入口：供 Web Shell 构建期静态 import（exports ./client）。
 */
import { pactorActions } from "../src/frontend/actions";
import { pactorComponents } from "../src/frontend/components";

export const extensions = {
  components: pactorComponents,
  actions: pactorActions,
};
