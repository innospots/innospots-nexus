/**
 * 为代码内静态文案生成稳定词条键，与 Console 语言包生成脚本保持一致。
 */
export function uiMessageKey(source: string): string {
  let hash = 0x811c9dc5;
  for (let index = 0; index < source.length; index++) {
    hash ^= source.charCodeAt(index);
    hash = Math.imul(hash, 0x01000193);
  }
  return `common.ui.${(hash >>> 0).toString(36)}`;
}
