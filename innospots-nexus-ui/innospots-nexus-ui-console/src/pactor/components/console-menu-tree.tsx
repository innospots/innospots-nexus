"use client";

import { useMemo } from "react";

import { Tree, type TreeNodeData } from "@pactor-app/ui/components";

type ConsoleMenuNode = {
  menuId?: string | null;
  menuName?: string | null;
  children?: ConsoleMenuNode[] | null;
};

function normalizeMenuTreeSource(raw: unknown): ConsoleMenuNode[] {
  if (Array.isArray(raw)) {
    return raw;
  }
  if (typeof raw === "object" && raw !== null) {
    const children = (raw as { children?: unknown }).children;
    if (Array.isArray(children)) {
      return children;
    }
  }
  return [];
}

function mapMenuNodes(nodes: ConsoleMenuNode[]): TreeNodeData[] {
  const mapped: TreeNodeData[] = [];
  for (const raw of nodes) {
    if (raw == null) {
      continue;
    }
    const key = raw.menuId?.trim() ?? "";
    if (key.length === 0) {
      continue;
    }
    const title = raw.menuName?.trim() || key;
    const children = mapMenuNodes(
      Array.isArray(raw.children) ? raw.children : [],
    );
    mapped.push({
      key,
      title,
      ...(children.length > 0 ? { children } : {}),
    });
  }
  return mapped;
}

export interface ConsoleMenuTreeProps {
  treeData?: unknown;
  className?: string;
  onSelect?: (keys: string[]) => void;
}

/**
 * 控制台菜单树：兼容 GET 空占位 `{}`、列表或带 children 的包裹结构。
 */
export function ConsoleMenuTree({
  treeData,
  className,
  onSelect,
}: ConsoleMenuTreeProps) {
  const nodes = useMemo(() => {
    return mapMenuNodes(normalizeMenuTreeSource(treeData));
  }, [treeData]);

  return (
    <Tree
      className={className}
      treeData={nodes}
      onSelect={onSelect}
    />
  );
}
