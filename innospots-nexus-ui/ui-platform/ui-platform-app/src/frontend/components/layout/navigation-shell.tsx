"use client";

import {
  ChevronRight,
  Home,
  Settings,
  Users,
  type LucideIcon,
} from "lucide-react";
import Link from "next/link";
import { MenuRegistry, type MenuItem } from "@pactor-app/router";
import { RailTooltip } from "@pactor-app/ui/components";
import { usePathname, useRouter } from "next/navigation";
import type { ReactNode, RefObject } from "react";
import { useEffect, useMemo, useRef, useState } from "react";

import { navigationConfig } from "../../config";
import { useHostI18n } from "../../i18n/messages";
import { useNavigationLayoutMode, useConsoleSiteName } from "../../navigation-layout-context";
import type { SessionUser } from "../../types/session";
import { AccountMenu } from "./account-menu";

export type { SessionUser };

export interface NavMenu {
  id?: string;
  key?: string;
  label?: string;
  type?: string;
  pageId?: string;
  path?: string;
  title?: string;
  icon?: string;
  hidden?: boolean;
  activeMenu?: string;
  permission?: string | string[] | { code: string | string[] };
  children?: NavMenu[];
}

interface NavigationShellProps {
  menus?: NavMenu[];
  user?: SessionUser;
  children?: ReactNode;
}

const ICONS: Record<string, LucideIcon> = {
  home: Home,
  users: Users,
  settings: Settings,
};

function iconFor(name: string | undefined): LucideIcon {
  return ICONS[(name ?? "").toLowerCase()] ?? Home;
}

function isGroup(item: NavMenu): boolean {
  return item.type === "group" || (item.children !== undefined && item.children.length > 0);
}

function visibleChildren(group: NavMenu): NavMenu[] {
  return (group.children ?? []).filter((item) => !item.hidden && navPath(item));
}

function navId(item: NavMenu): string | undefined {
  return item.id ?? item.key ?? item.path;
}

function navPath(item: NavMenu): string | undefined {
  return item.path ?? (item.key?.startsWith("/") ? item.key : undefined);
}

function menuTitle(item: NavMenu): string {
  return item.title ?? item.label ?? "";
}

export function createNavigationRegistry(menus: NavMenu[]): MenuRegistry {
  function convert(items: NavMenu[]): MenuItem[] {
    return items.flatMap((item) => {
      const id = navId(item);
      if (!id) {
        return [];
      }
      const type = isGroup(item) ? "group" : item.type === "link" ? "link" : "page";
      return [{
        id,
        type,
        title: menuTitle(item),
        path: navPath(item),
        pageId: item.pageId,
        hidden: item.hidden,
        activeMenu: item.activeMenu,
        ...(item.children ? { children: convert(item.children) } : {}),
      }];
    });
  }
  return new MenuRegistry(convert(menus));
}

export function findNavigationMenu(registry: MenuRegistry, pathname: string): MenuItem | undefined {
  const segments = pathname.split("/").filter(Boolean);
  while (segments.length > 0) {
    const hit = registry.findByPath(`/${segments.join("/")}`)?.menu;
    if (hit) {
      return hit;
    }
    segments.pop();
  }
  return registry.findByPath("/")?.menu;
}

function groupContains(group: NavMenu, currentId: string | undefined): boolean {
  if (!currentId) {
    return false;
  }
  return visibleChildren(group).some(
    (item) =>
      navId(item) === currentId ||
      item.pageId === currentId ||
      item.activeMenu === currentId,
  );
}

function menuLink(
  item: NavMenu,
  currentMenuId: string | undefined,
  className: string,
) {
  const path = navPath(item)!;
  const active = navId(item) === currentMenuId || item.pageId === currentMenuId;
  return (
    <Link
      key={navId(item)}
      href={path}
      aria-current={active ? "page" : undefined}
      className={active ? `${className} is-active` : className}
    >
      {menuTitle(item)}
    </Link>
  );
}

export function NavigationShell({ menus = [], user, children }: NavigationShellProps) {
  const pathname = usePathname() ?? "/";
  const router = useRouter();
  const { t } = useHostI18n();
  const mainRef = useRef<HTMLDivElement>(null);
  const layoutMode = useNavigationLayoutMode();
  const consoleSiteName = useConsoleSiteName();
  const consoleSiteInitial =
    consoleSiteName.trim().charAt(0).toUpperCase() || "N";

  const topLevelMenus = useMemo(
    () =>
      menus.filter(
        (item) =>
          !item.hidden &&
          (isGroup(item) ? visibleChildren(item).length > 0 : Boolean(navPath(item))),
      ),
    [menus],
  );
  const registry = useMemo(() => createNavigationRegistry(menus), [menus]);
  const matchedMenu = findNavigationMenu(registry, pathname);
  const currentMenuId = matchedMenu?.activeMenu ?? matchedMenu?.id;
  const routedTopLevel = useMemo(
    () =>
      topLevelMenus.find(
        (item) => navId(item) === currentMenuId || groupContains(item, currentMenuId),
      ),
    [currentMenuId, topLevelMenus],
  );
  const selectedTopLevel = routedTopLevel;
  const activeGroup =
    selectedTopLevel && isGroup(selectedTopLevel) ? selectedTopLevel : undefined;

  useEffect(() => {
    const content = mainRef.current?.querySelector<HTMLElement>(".shell-content");
    content?.scrollTo({ top: 0, left: 0, behavior: "instant" });
  }, [pathname]);

  function selectGroup(group: NavMenu) {
    if (!groupContains(group, currentMenuId)) {
      const first = visibleChildren(group)[0];
      const firstPath = first ? navPath(first) : undefined;
      if (firstPath) {
        router.push(firstPath);
      }
    }
  }

  const shellClass =
    layoutMode === "SINGLE_GROUP" ? "kn-shell kn-layout-single-group" : "kn-shell";

  if (layoutMode === "SINGLE_GROUP") {
    return (
      <SingleGroupNavigationShell
        shellClass={shellClass}
        topLevelMenus={topLevelMenus}
        currentMenuId={currentMenuId}
        user={user}
        mainRef={mainRef}
        t={t}
        consoleSiteName={consoleSiteName}
        consoleSiteInitial={consoleSiteInitial}
      >
        {children}
      </SingleGroupNavigationShell>
    );
  }

  return (
    <div className={shellClass}>
      <nav
        className={
          navigationConfig.expandRailOnHover
            ? "kn-rail kn-rail-expandable"
            : "kn-rail"
        }
        aria-label={t("nav.main")}
      >
        <div className="kn-mark" aria-hidden="true">
          {consoleSiteInitial}
        </div>
        {topLevelMenus.map((item) => {
          const Icon = iconFor(item.icon);
          const itemId = navId(item);
          const title = menuTitle(item);
          const active =
            selectedTopLevel !== undefined && navId(selectedTopLevel) === itemId;
          const content = (
            <>
              <Icon size={18} strokeWidth={1.8} />
              <span className="kn-rail-label">{title}</span>
            </>
          );

          const trigger = isGroup(item) ? (
            <button
              type="button"
              className={active ? "kn-rail-button is-active" : "kn-rail-button"}
              aria-label={title}
              aria-pressed={active}
              onClick={() => selectGroup(item)}
            >
              {content}
            </button>
          ) : (
            <Link
              href={navPath(item) ?? "/"}
              className={active ? "kn-rail-button is-active" : "kn-rail-button"}
              aria-label={title}
              aria-current={active ? "page" : undefined}
            >
              {content}
            </Link>
          );
          return navigationConfig.expandRailOnHover ? (
            <span key={itemId}>{trigger}</span>
          ) : (
            <RailTooltip key={itemId} label={title}>{trigger}</RailTooltip>
          );
        })}
        <AccountMenu user={user} />
      </nav>

      {activeGroup ? (
        <aside
          className="kn-sidebar"
          aria-label={t("nav.moduleMenu", { name: menuTitle(activeGroup) })}
        >
          <div className="kn-sidebar-title">{menuTitle(activeGroup)}</div>
          <nav className="kn-menu">
            {visibleChildren(activeGroup).map((item) =>
              menuLink(item, currentMenuId, "kn-menu-item"),
            )}
          </nav>
        </aside>
      ) : null}

      <div className="kn-main" ref={mainRef}>
        {children}
      </div>
    </div>
  );
}

function SingleGroupNavigationShell({
  shellClass,
  topLevelMenus,
  currentMenuId,
  user,
  mainRef,
  t,
  consoleSiteName,
  consoleSiteInitial,
  children,
}: {
  shellClass: string;
  topLevelMenus: NavMenu[];
  currentMenuId: string | undefined;
  user?: SessionUser;
  mainRef: RefObject<HTMLDivElement | null>;
  t: ReturnType<typeof useHostI18n>["t"];
  consoleSiteName: string;
  consoleSiteInitial: string;
  children?: ReactNode;
}) {
  const [expandedDirs, setExpandedDirs] = useState<Record<string, boolean>>({});

  useEffect(() => {
    setExpandedDirs((prev) => {
      let changed = false;
      const next = { ...prev };
      for (const item of topLevelMenus) {
        if (!isGroup(item)) {
          continue;
        }
        const id = navId(item);
        if (!id) {
          continue;
        }
        if (next[id] !== true) {
          next[id] = true;
          changed = true;
        }
      }
      return changed ? next : prev;
    });
  }, [topLevelMenus]);

  useEffect(() => {
    setExpandedDirs((prev) => {
      let changed = false;
      const next = { ...prev };
      for (const item of topLevelMenus) {
        if (!isGroup(item)) {
          continue;
        }
        const id = navId(item);
        if (id && groupContains(item, currentMenuId) && next[id] !== true) {
          next[id] = true;
          changed = true;
        }
      }
      return changed ? next : prev;
    });
  }, [topLevelMenus, currentMenuId]);

  function toggleDir(id: string, currentlyOpen: boolean) {
    setExpandedDirs((prev) => ({
      ...prev,
      [id]: !currentlyOpen,
    }));
  }

  function isDirOpen(item: NavMenu): boolean {
    const id = navId(item);
    if (!id) {
      return false;
    }
    if (expandedDirs[id] !== undefined) {
      return expandedDirs[id];
    }
    return groupContains(item, currentMenuId);
  }

  return (
    <div className={shellClass}>
      <aside className="kn-sidebar kn-sidebar-stacked" aria-label={t("nav.main")}>
        <div className="kn-sidebar-brand">
          <div className="kn-mark kn-mark-sidebar" aria-hidden="true">
            {consoleSiteInitial}
          </div>
          <div className="kn-sidebar-title">{consoleSiteName}</div>
        </div>
        <div className="kn-sidebar-body">
          <nav className="kn-menu kn-menu-tree">
            {topLevelMenus.map((item) => {
            if (!isGroup(item)) {
              const path = navPath(item);
              if (!path) {
                return null;
              }
              return (
                <div key={navId(item)} className="kn-menu-tree-root">
                  {menuLink(item, currentMenuId, "kn-menu-item kn-menu-tree-leaf")}
                </div>
              );
            }
            const id = navId(item);
            if (!id) {
              return null;
            }
            const open = isDirOpen(item);
            const childrenItems = visibleChildren(item);
            if (childrenItems.length === 0) {
              return null;
            }
            return (
              <div key={id} className="kn-menu-tree-node">
                <button
                  type="button"
                  className={`kn-menu-tree-dir${open ? " is-open" : ""}`}
                  aria-expanded={open}
                  onClick={() => toggleDir(id, open)}
                >
                  <ChevronRight className="kn-menu-tree-chevron" size={16} aria-hidden="true" />
                  <span>{menuTitle(item)}</span>
                </button>
                <div
                  className="kn-menu-tree-children"
                  role="group"
                  aria-label={menuTitle(item)}
                  hidden={!open}
                >
                  {childrenItems.map((child) =>
                    menuLink(child, currentMenuId, "kn-menu-item kn-menu-tree-item"),
                  )}
                </div>
              </div>
            );
            })}
          </nav>
        </div>
        <AccountMenu user={user} className="account-menu-stacked" />
      </aside>
      <div className="kn-main" ref={mainRef}>
        {children}
      </div>
    </div>
  );
}
