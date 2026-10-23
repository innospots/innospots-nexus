/**
 * UI 模块客户端入口：供 Web Shell 构建期静态 import（exports ./client）。
 */
import { pactorActions } from "../src/pactor/actions";
import { pactorComponents } from "../src/pactor/components";

export const extensions = {
  components: pactorComponents,
  actions: pactorActions,
};
