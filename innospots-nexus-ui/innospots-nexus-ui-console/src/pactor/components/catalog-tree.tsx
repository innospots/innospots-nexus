"use client";

import { useMemo } from "react";

import { Tree, type TreeNodeData } from "@pactor-app/ui/components";

type CatalogNode = {
  resourceKey?: string | null;
  displayName?: string | null;
  children?: CatalogNode[] | null;
};

function mapCatalogNodes(nodes: CatalogNode[] | null | undefined): TreeNodeData[] {
  if (!Array.isArray(nodes)) {
    return [];
  }
  const mapped: TreeNodeData[] = [];
  for (const raw of nodes) {
    if (raw == null) {
      continue;
    }
    const key = raw.resourceKey?.trim() ?? "";
    if (key.length === 0) {
      continue;
    }
    const title = raw.displayName?.trim() || key;
    const children = mapCatalogNodes(raw.children);
    mapped.push({
      key,
      title,
      ...(children.length > 0 ? { children } : {}),
    });
  }
  return mapped;
}

export interface CatalogTreeProps {
  treeData?: CatalogNode[] | null;
  className?: string;
  onSelect?: (keys: string[]) => void;
}

/**
 * 将 Console catalog/tree API 节点（resourceKey、displayName）映射为 Tree 所需的 key、title。
 */
export function CatalogTree({ treeData, className, onSelect }: CatalogTreeProps) {
  const nodes = useMemo(() => mapCatalogNodes(treeData), [treeData]);
  return (
    <Tree
      className={className}
      treeData={nodes}
      onSelect={onSelect}
    />
  );
}
