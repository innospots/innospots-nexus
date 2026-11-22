"use client";

import { useMemo } from "react";

import { Tree, type TreeNodeData } from "@pactor-app/ui/components";

import { useI18n } from "@pactor-app/runtime";

import { resolveI18nText } from "../shared/i18n-text";

type CatalogNode = {
  resourceKey?: string | null;
  resourceType?: string | null;
  displayName?: string | Record<string, string> | null;
  children?: CatalogNode[] | null;
};

type CatalogDomain = {
  ownerPluginId?: string | null;
  domainKey?: string | null;
  displayName?: string | Record<string, string> | null;
  icon?: string | null;
  modules?: CatalogNode[] | null;
};

function normalizeCatalogDomains(raw: unknown): CatalogDomain[] {
  if (Array.isArray(raw)) {
    const items = raw as unknown[];
    if (items.length === 0) {
      return [];
    }
    const first = items[0];
    if (typeof first === "object" && first != null && "modules" in (first as object)) {
      return items as CatalogDomain[];
    }
    return [{ modules: items as CatalogNode[] }];
  }
  if (typeof raw === "object" && raw !== null) {
    const record = raw as Record<string, unknown>;
    if (Array.isArray(record.modules)) {
      return [record as CatalogDomain];
    }
    if (Array.isArray(record.children)) {
      return normalizeCatalogDomains(record.children);
    }
    if (Array.isArray(record.data)) {
      return normalizeCatalogDomains(record.data);
    }
  }
  return [];
}

function domainTreeKey(domain: CatalogDomain): string {
  const owner = domain.ownerPluginId?.trim() ?? "";
  const key = domain.domainKey?.trim() ?? "";
  if (owner.length > 0 && key.length > 0) {
    return `domain:${owner}:${key}`;
  }
  if (key.length > 0) {
    return `domain:${key}`;
  }
  return `domain:${owner}`;
}

function mapModuleNodes(
  nodes: CatalogNode[] | null | undefined,
  locale: string,
  defaultLocale: string,
): TreeNodeData[] {
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
    const title = resolveI18nText(raw.displayName, locale, defaultLocale).trim() || key;
    const children = mapModuleNodes(raw.children, locale, defaultLocale);
    mapped.push({
      key,
      title,
      ...(children.length > 0 ? { children } : {}),
    });
  }
  return mapped;
}

function mapCatalogDomains(
  domains: CatalogDomain[],
  locale: string,
  defaultLocale: string,
): TreeNodeData[] {
  const mapped: TreeNodeData[] = [];
  for (const domain of domains) {
    const key = domainTreeKey(domain);
    const title = resolveI18nText(domain.displayName, locale, defaultLocale).trim()
      || domain.domainKey?.trim()
      || key;
    const children = mapModuleNodes(domain.modules, locale, defaultLocale);
    mapped.push({
      key,
      title,
      ...(children.length > 0 ? { children } : {}),
    });
  }
  return mapped;
}

function isDomainTreeKey(key: string): boolean {
  return key.startsWith("domain:");
}

function collectExpandableKeys(nodes: TreeNodeData[]): string[] {
  const keys: string[] = [];
  for (const node of nodes) {
    keys.push(node.key);
    if (node.children != null && node.children.length > 0) {
      keys.push(...collectExpandableKeys(node.children));
    }
  }
  return keys;
}

function filterTree(nodes: TreeNodeData[], search: string): TreeNodeData[] {
  const query = search.trim().toLowerCase();
  if (query.length === 0) {
    return nodes;
  }
  const filtered: TreeNodeData[] = [];
  for (const node of nodes) {
    const children = node.children != null ? filterTree(node.children, query) : [];
    const selfMatch =
      node.key.toLowerCase().includes(query) ||
      node.title.toLowerCase().includes(query);
    if (selfMatch || children.length > 0) {
      filtered.push({
        ...node,
        ...(children.length > 0 ? { children } : {}),
      });
    }
  }
  return filtered;
}

export interface CatalogTreeProps {
  treeData?: unknown;
  search?: string;
  className?: string;
  onSelect?: (keys: string[]) => void;
}

/** 将 permission-catalog（CatalogDomainVo[]）映射为 Tree；领域节点可展开/收起，但不参与勾选。 */
export function CatalogTree({ treeData, search, className, onSelect }: CatalogTreeProps) {
  const { t, locale, defaultLocale } = useI18n();
  const nodes = useMemo(() => {
    const mapped = mapCatalogDomains(normalizeCatalogDomains(treeData), locale, defaultLocale);
    return filterTree(mapped, search ?? "");
  }, [treeData, search, locale, defaultLocale]);

  const defaultExpandedKeys = useMemo(() => collectExpandableKeys(nodes), [nodes]);

  if (nodes.length === 0) {
    return (
      <p className="mt-2 text-sm text-muted-foreground">
        {t("common.ui.no-catalog-resources-if-you-just-installed-a-plugin-try-synchronizing-th")}
      </p>
    );
  }

  return (
    <Tree
      className={className}
      style={{ minHeight: 280 }}
      treeData={nodes}
      defaultExpandedKeys={defaultExpandedKeys}
      onSelect={(keys) => {
        if (onSelect == null) {
          return;
        }
        onSelect(keys.filter((key) => !isDomainTreeKey(key)));
      }}
    />
  );
}

export function catalogModuleForest(raw: unknown): CatalogNode[] {
  return normalizeCatalogDomains(raw).flatMap((domain) =>
    Array.isArray(domain.modules) ? domain.modules : [],
  );
}
