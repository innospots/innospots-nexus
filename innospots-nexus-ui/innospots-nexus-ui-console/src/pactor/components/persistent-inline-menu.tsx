"use client";

/**
 * 包装 @pactor-app/ui Menu：将用户展开的目录 key 写入模块级 Set，
 * 布局 DSL 重挂载时通过 defaultExpandedKeys 恢复展开状态。
 */
import { Menu, type MenuItem, type MenuProps } from "@pactor-app/ui/components";
import { useEffect, useMemo, useRef } from "react";

const expandedStore = new Set<string>();

function collectAncestors(
  items: MenuItem[],
  target: string,
  into: Set<string>,
  trail: string[] = [],
): boolean {
  for (const item of items) {
    if (item.key === target) {
      for (const key of trail) {
        into.add(key);
      }
      return true;
    }
    if (
      item.children?.length &&
      collectAncestors(item.children, target, into, [...trail, item.key])
    ) {
      return true;
    }
  }
  return false;
}

function mergeDefaultExpandedKeys(
  items: MenuItem[],
  selectedKey: string | undefined,
  defaultExpandedKeys?: string[],
): string[] {
  const merged = new Set(defaultExpandedKeys ?? []);
  for (const key of expandedStore) {
    merged.add(key);
  }
  if (selectedKey !== undefined) {
    collectAncestors(items, selectedKey, merged);
  }
  expandedStore.clear();
  for (const key of merged) {
    expandedStore.add(key);
  }
  return Array.from(merged);
}

export function PersistentInlineMenu({
  items = [],
  selectedKey,
  defaultExpandedKeys,
  ...rest
}: MenuProps) {
  const wrapperRef = useRef<HTMLDivElement>(null);

  const mergedDefaultExpandedKeys = useMemo(
    () => mergeDefaultExpandedKeys(items, selectedKey, defaultExpandedKeys),
    [defaultExpandedKeys, items, selectedKey],
  );

  useEffect(() => {
    mergeDefaultExpandedKeys(items, selectedKey, defaultExpandedKeys);
  }, [defaultExpandedKeys, items, selectedKey]);

  useEffect(() => {
    const root = wrapperRef.current;
    if (root === null) {
      return;
    }
    const onCaptureClick = (event: MouseEvent) => {
      const target = event.target;
      if (!(target instanceof Element)) {
        return;
      }
      const button = target.closest("[data-slot=menu-item]");
      if (button === null) {
        return;
      }
      if (button.querySelector("[data-slot=menu-item-chevron]") === null) {
        return;
      }
      const entry = button.closest("[data-slot=menu-entry]");
      if (entry === null) {
        return;
      }
      const key = entry.getAttribute("data-key");
      if (key === null || key === "") {
        return;
      }
      if (expandedStore.has(key)) {
        expandedStore.delete(key);
      } else {
        expandedStore.add(key);
      }
    };
    root.addEventListener("click", onCaptureClick, true);
    return () => {
      root.removeEventListener("click", onCaptureClick, true);
    };
  }, []);

  return (
    <div ref={wrapperRef} className="contents">
      <Menu
        {...rest}
        items={items}
        selectedKey={selectedKey}
        defaultExpandedKeys={mergedDefaultExpandedKeys}
      />
    </div>
  );
}
