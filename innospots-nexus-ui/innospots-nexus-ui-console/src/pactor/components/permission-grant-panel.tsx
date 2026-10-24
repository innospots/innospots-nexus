"use client";

import { useMemo } from "react";

import { resolveI18nText } from "./i18n-text";

type I18nValue = string | Record<string, string> | null | undefined;

type CatalogGrantItem = {
  resourceKey?: string | null;
  grantName?: string | null;
  displayName?: I18nValue;
  description?: I18nValue;
};

type CatalogGrantGroup = {
  group?: string | null;
  displayName?: I18nValue;
  items?: CatalogGrantItem[] | null;
};

type CatalogNode = {
  resourceKey?: string | null;
  resourceType?: string | null;
  entryPageKey?: string | null;
  displayName?: I18nValue;
  children?: CatalogNode[] | null;
  grants?: {
    viewGroups?: CatalogGrantGroup[] | null;
    operationGroups?: CatalogGrantGroup[] | null;
  } | null;
};

type PermissionGrant = {
  resourceKey?: string | null;
  constraintDefinition?: string | null;
};

function normalizeCatalogSource(raw: unknown): CatalogNode[] {
  if (Array.isArray(raw)) {
    return raw as CatalogNode[];
  }
  if (typeof raw === "object" && raw !== null) {
    const record = raw as Record<string, unknown>;
    if (Array.isArray(record.children)) {
      return record.children as CatalogNode[];
    }
    if (Array.isArray(record.data)) {
      return record.data as CatalogNode[];
    }
  }
  return [];
}

function normalizeGrants(raw: unknown): PermissionGrant[] {
  if (Array.isArray(raw)) {
    return raw.filter((item): item is PermissionGrant => typeof item === "object" && item !== null);
  }
  if (typeof raw === "object" && raw !== null) {
    const grants = (raw as Record<string, unknown>).grants;
    if (Array.isArray(grants)) {
      return grants.filter(
        (item): item is PermissionGrant => typeof item === "object" && item !== null,
      );
    }
  }
  return [];
}

function grantKey(item: CatalogGrantItem | PermissionGrant | null | undefined): string {
  return item?.resourceKey?.trim() ?? "";
}

function findCatalogPath(
  nodes: CatalogNode[],
  selectedKey: string,
  ancestors: CatalogNode[] = [],
): CatalogNode[] | null {
  for (const node of nodes) {
    if (node == null) {
      continue;
    }
    const nextPath = [...ancestors, node];
    if (grantKey(node) === selectedKey) {
      return nextPath;
    }
    const children = Array.isArray(node.children) ? node.children : [];
    const found = findCatalogPath(children, selectedKey, nextPath);
    if (found != null) {
      return found;
    }
  }
  return null;
}

function resolveGrantModule(path: CatalogNode[] | null): CatalogNode | null {
  if (path == null) {
    return null;
  }
  for (let index = path.length - 1; index >= 0; index -= 1) {
    const node = path[index];
    if (node.resourceType === "MODULE" && node.grants != null) {
      return node;
    }
  }
  return null;
}

function collectCatalogGrantKeys(nodes: CatalogNode[], keys: string[] = []): string[] {
  for (const node of nodes) {
    const grants = node?.grants;
    if (grants != null) {
      for (const group of [...(grants.viewGroups ?? []), ...(grants.operationGroups ?? [])]) {
        for (const item of group.items ?? []) {
          const key = grantKey(item);
          if (key.length > 0) {
            keys.push(key);
          }
        }
      }
    }
    collectCatalogGrantKeys(Array.isArray(node?.children) ? node.children : [], keys);
  }
  return keys;
}

function itemTitle(item: CatalogGrantItem): string {
  return resolveI18nText(item.displayName) || item.grantName?.trim() || grantKey(item);
}

function itemDescription(item: CatalogGrantItem): string {
  return resolveI18nText(item.description);
}

function GrantCheckbox({
  item,
  checked,
  currentPage,
  onChange,
}: {
  item: CatalogGrantItem;
  checked: boolean;
  currentPage?: boolean;
  onChange: (checked: boolean) => void;
}) {
  const description = itemDescription(item);
  return (
    <label className="flex cursor-pointer items-start gap-3 rounded-md border border-border/70 px-3 py-2.5 hover:bg-muted/40">
      <input
        type="checkbox"
        className="mt-0.5 h-4 w-4 shrink-0 accent-primary"
        checked={checked}
        onChange={(event) => onChange(event.target.checked)}
      />
      <span className="min-w-0">
        <span className="flex flex-wrap items-center gap-2 text-sm font-medium text-foreground">
          {itemTitle(item)}
          {currentPage ? (
            <span className="rounded bg-primary/10 px-1.5 py-0.5 text-[11px] font-normal text-primary">
              当前页面
            </span>
          ) : null}
        </span>
        {description.length > 0 ? (
          <span className="mt-1 block text-xs leading-5 text-muted-foreground">
            {description}
          </span>
        ) : null}
        <span className="mt-1 block break-all font-mono text-[11px] text-muted-foreground/80">
          {grantKey(item)}
        </span>
      </span>
    </label>
  );
}

export interface PermissionGrantPanelProps {
  catalog?: unknown;
  selectedResourceKey?: string;
  grants?: unknown;
  draftGrants?: unknown;
  className?: string;
  onChange?: (event: { grants: PermissionGrant[]; dirty: boolean }) => void;
}

/**
 * 角色权限看板：MENU 自动定位所属 MODULE，展示页面访问与按组操作权限。
 */
export function PermissionGrantPanel({
  catalog,
  selectedResourceKey,
  grants,
  draftGrants,
  className,
  onChange,
}: PermissionGrantPanelProps) {
  const catalogNodes = useMemo(() => normalizeCatalogSource(catalog), [catalog]);
  const selectedKey = selectedResourceKey?.trim() ?? "";
  const selectedPath = useMemo(
    () => findCatalogPath(catalogNodes, selectedKey),
    [catalogNodes, selectedKey],
  );
  const moduleNode = useMemo(() => resolveGrantModule(selectedPath), [selectedPath]);
  const selectedNode = selectedPath?.[selectedPath.length - 1] ?? null;

  const orderedGrantKeys = useMemo(
    () => Array.from(new Set(collectCatalogGrantKeys(catalogNodes))),
    [catalogNodes],
  );
  const knownGrantKeys = useMemo(() => new Set(orderedGrantKeys), [orderedGrantKeys]);
  const savedGrants = useMemo(
    () => normalizeGrants(grants).filter((item) => knownGrantKeys.has(grantKey(item))),
    [grants, knownGrantKeys],
  );
  const effectiveGrants = useMemo(() => {
    const source = Array.isArray(draftGrants) ? normalizeGrants(draftGrants) : savedGrants;
    return source.filter((item) => knownGrantKeys.has(grantKey(item)));
  }, [draftGrants, savedGrants, knownGrantKeys]);
  const selectedKeys = useMemo(
    () => new Set(effectiveGrants.map((item) => grantKey(item)).filter((key) => key.length > 0)),
    [effectiveGrants],
  );

  const viewGroups = useMemo(() => moduleNode?.grants?.viewGroups ?? [], [moduleNode]);
  const operationGroups = useMemo(
    () => moduleNode?.grants?.operationGroups ?? [],
    [moduleNode],
  );
  const moduleItemKeys = useMemo(
    () =>
      [...viewGroups, ...operationGroups]
        .flatMap((group) => (group.items ?? []).map((item) => grantKey(item)))
        .filter((key) => key.length > 0),
    [viewGroups, operationGroups],
  );
  const selectedCount = moduleItemKeys.filter((key) => selectedKeys.has(key)).length;

  const toggleGrant = (key: string, checked: boolean) => {
    const nextKeys = new Set(selectedKeys);
    if (checked) {
      nextKeys.add(key);
    } else {
      nextKeys.delete(key);
    }
    const nextGrants = orderedGrantKeys
      .filter((candidate) => nextKeys.has(candidate))
      .map((candidate) => ({ resourceKey: candidate }));
    onChange?.({ grants: nextGrants, dirty: true });
  };

  if (selectedKey.length === 0) {
    return (
      <div className="flex min-h-56 items-center justify-center rounded-md border border-dashed border-border px-6 py-10 text-center text-sm text-muted-foreground">
        请从左侧选择页面或模块，查看并设置访问与操作权限。
      </div>
    );
  }

  if (moduleNode == null) {
    return (
      <div className="flex min-h-56 items-center justify-center rounded-md border border-dashed border-border px-6 py-10 text-center text-sm text-muted-foreground">
        当前节点没有可配置的权限模块。
      </div>
    );
  }

  const selectedName = selectedNode != null ? itemTitle(selectedNode) : selectedKey;
  const moduleName = itemTitle(moduleNode);
  const viewItemCount = viewGroups.reduce(
    (total, group) => total + (group.items?.length ?? 0),
    0,
  );
  const operationItemCount = operationGroups.reduce(
    (total, group) => total + (group.items?.length ?? 0),
    0,
  );

  return (
    <div className={`flex flex-col gap-4 ${className ?? ""}`}>
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <div className="text-base font-semibold text-foreground">{moduleName}</div>
          <div className="mt-1 text-xs text-muted-foreground">
            {selectedNode?.resourceType === "MENU"
              ? `已从菜单“${selectedName}”定位到所属模块`
              : "当前模块授权"}
          </div>
        </div>
        <div className="rounded-full border border-border px-3 py-1 text-xs text-muted-foreground">
          已选 {selectedCount} / {moduleItemKeys.length}
        </div>
      </div>

      <p className="rounded-md bg-muted/50 px-3 py-2 text-xs leading-5 text-muted-foreground">
        查看访问权限决定导航菜单和页面是否可见；操作权限只控制接口动作，不会单独展示菜单。
      </p>

      <section className="rounded-lg border border-border p-4">
        <div className="flex items-center justify-between gap-2">
          <h4 className="text-sm font-semibold text-foreground">查看访问权限</h4>
          <span className="text-xs text-muted-foreground">{viewItemCount} 项</span>
        </div>
        {viewItemCount === 0 ? (
          <p className="mt-3 text-sm text-muted-foreground">该模块没有可配置的页面访问项。</p>
        ) : (
          <div className="mt-3 space-y-4">
            {viewGroups.map((group, groupIndex) => {
              const items = group.items ?? [];
              if (items.length === 0) {
                return null;
              }
              return (
                <div key={`view-${group.group ?? "default"}-${groupIndex}`}>
                  <div className="mb-2 flex items-center gap-2">
                    <span className="text-xs font-medium text-muted-foreground">
                      {resolveI18nText(group.displayName) || group.group || "查看"}
                    </span>
                    <span className="h-px flex-1 bg-border" />
                  </div>
                  <div className="grid gap-2 xl:grid-cols-2">
                    {items.map((item) => {
                      const key = grantKey(item);
                      return (
                        <GrantCheckbox
                          key={key}
                          item={item}
                          checked={selectedKeys.has(key)}
                          onChange={(checked) => toggleGrant(key, checked)}
                        />
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>

      <section className="rounded-lg border border-border p-4">
        <div className="flex items-center justify-between gap-2">
          <h4 className="text-sm font-semibold text-foreground">操作权限</h4>
          <span className="text-xs text-muted-foreground">{operationItemCount} 项</span>
        </div>
        {operationItemCount === 0 ? (
          <p className="mt-3 text-sm text-muted-foreground">该模块没有单独的操作授权项。</p>
        ) : (
          <div className="mt-3 space-y-4">
            {operationGroups.map((group, groupIndex) => {
              const items = group.items ?? [];
              if (items.length === 0) {
                return null;
              }
              return (
                <div key={`${group.group ?? "default"}-${groupIndex}`}>
                  <div className="mb-2 flex items-center gap-2">
                    <span className="text-xs font-medium text-muted-foreground">
                      {resolveI18nText(group.displayName) || group.group || "操作"}
                    </span>
                    <span className="h-px flex-1 bg-border" />
                  </div>
                  <div className="grid gap-2 xl:grid-cols-2">
                    {items.map((item) => {
                      const key = grantKey(item);
                      return (
                        <GrantCheckbox
                          key={key}
                          item={item}
                          checked={selectedKeys.has(key)}
                          onChange={(checked) => toggleGrant(key, checked)}
                        />
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>
    </div>
  );
}
