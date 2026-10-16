"use client";

import { type MenuItem, type MenuProps } from "@pactor-app/ui/components";
import { usePathname } from "next/navigation";
import { useMemo } from "react";

import { PersistentInlineMenu } from "./persistent-inline-menu";

/** 按当前路径在可见菜单树中解析选中项（最长路径前缀匹配，兼容详情页）。 */
function resolveSelectedKey(
  pathname: string,
  items: MenuItem[],
): string | undefined {
  let best: string | undefined;
  let bestLen = -1;

  const walk = (nodes: MenuItem[]) => {
    for (const node of nodes) {
      if (node.children?.length) {
        walk(node.children);
      }
      const key = node.key;
      if (!key.startsWith("/")) {
        continue;
      }
      if (pathname === key || pathname.startsWith(`${key}/`)) {
        if (key.length > bestLen) {
          bestLen = key.length;
          best = key;
        }
      }
    }
  };

  walk(items);
  return best;
}

/**
 * 覆盖 @pactor-app/app 内置 SitemapMenu：路由切换后保持侧栏分组展开状态。
 */
export function ShellSitemapMenu({
  items = [],
  collapsible,
  onSelect,
  className,
  style,
}: MenuProps) {
  const pathname = usePathname();
  const selectedKey = useMemo(
    () => resolveSelectedKey(pathname, items),
    [pathname, items],
  );

  return (
    <PersistentInlineMenu
      items={items}
      collapsible={collapsible}
      selectedKey={selectedKey}
      onSelect={onSelect}
      className={className}
      style={style}
    />
  );
}
