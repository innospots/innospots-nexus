"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  LayoutDashboard,
  Palette,
  Sparkles,
  UserRound,
  type LucideIcon,
} from "lucide-react";
import type { ReactNode } from "react";
import { useMemo, useState } from "react";

import {
  Dialog,
  RailTooltip,
  UserMenu,
} from "@pactor-app/ui/components";

import type { NavMenu, SessionUser } from "../../types/session";
import { logoutAndRedirect } from "../../lib/auth-session";
import { setThemeMode, useThemeMode } from "../../lib/theme-mode";

interface PortalShellProps {
  user?: SessionUser;
  menus?: NavMenu[];
  children?: ReactNode;
}

const ICONS: Record<string, LucideIcon> = {
  workspace: LayoutDashboard,
  dashboard: LayoutDashboard,
  home: LayoutDashboard,
  "user-round": UserRound,
  user: UserRound,
  palette: Palette,
  sparkles: Sparkles,
  ai: Sparkles,
};

function iconFor(name: string | undefined): LucideIcon {
  return ICONS[(name ?? "").toLowerCase()] ?? LayoutDashboard;
}

function navPath(item: NavMenu): string | undefined {
  return item.path ?? (item.key?.startsWith("/") ? item.key : undefined);
}

function navTitle(item: NavMenu): string {
  return item.title ?? item.label ?? navPath(item) ?? "";
}

function childrenOf(item: NavMenu): NavMenu[] {
  return (item.children ?? []).filter(
    (child) => !child.hidden && navPath(child) !== undefined,
  );
}

function isGroup(item: NavMenu): boolean {
  return (item.children?.length ?? 0) > 0;
}

function matches(pathname: string, path: string | undefined): boolean {
  if (!path) return false;
  return pathname === path || (path !== "/" && pathname.startsWith(`${path}/`));
}

/** 门户外壳：左轨一级菜单 + 二级菜单侧栏 + 底部账户菜单。 */
export function PortalShell({ user, menus = [], children }: PortalShellProps) {
  const pathname = usePathname() ?? "/";
  const router = useRouter();
  const basePath =
    pathname === "/portal" || pathname.startsWith("/portal/") ? "/portal" : "";
  const rootPath = basePath || "/";

  const railItems = useMemo(
    () =>
      menus.filter(
        (item) =>
          !item.hidden &&
          !isGroup(item) &&
          navPath(item) === rootPath,
      ),
    [menus, rootPath],
  );
  const groups = useMemo(
    () => menus.filter((item) => !item.hidden && isGroup(item)),
    [menus],
  );
  const settingsGroup = useMemo(
    () => groups.find((group) => (group.id ?? group.key) === "settings"),
    [groups],
  );
  const activeGroup = useMemo(
    () =>
      groups.find((group) =>
        childrenOf(group).some((child) => matches(pathname, navPath(child))),
      ),
    [groups, pathname],
  );
  const accountItems = useMemo(() => {
    const settingsPath = childrenOf(settingsGroup ?? {})
      .map(navPath)
      .find((path) => path !== undefined);
    return [
      ...(settingsPath
        ? [{ label: "设置", value: settingsPath, icon: "settings" }]
        : []),
      { label: "切换团队", value: "team-switch", icon: "users" },
    ];
  }, [settingsGroup]);
  const displayName = user?.name?.trim() || user?.username || "账户";
  const [teamDialogOpen, setTeamDialogOpen] = useState(false);
  const themeMode = useThemeMode();

  return (
    <div className="portal-shell">
      <nav className="portal-rail" aria-label="一级导航">
        <Link className="portal-rail-mark" href="/" aria-label="返回首页">
          N
        </Link>
        <div className="portal-rail-items">
          {railItems.map((item) => {
            const path = navPath(item)!;
            const Icon = iconFor(item.icon);
            const active = pathname === path || pathname === `${path}/`;
            const title = navTitle(item);
            return (
              <RailTooltip key={item.id ?? path} label={title}>
                <Link
                  href={path}
                  aria-label={title}
                  aria-current={active ? "page" : undefined}
                  className={
                    active
                      ? "portal-rail-item is-active"
                      : "portal-rail-item"
                  }
                >
                  <Icon size={19} strokeWidth={1.8} />
                </Link>
              </RailTooltip>
            );
          })}
        </div>
        <div className="portal-rail-footer">
          <UserMenu
            user={{ name: displayName, email: user?.username ?? "" }}
            items={accountItems}
            showName={false}
            showTheme
            showLanguage
            showSize={false}
            theme={themeMode}
            onThemeChange={setThemeMode}
            side="top"
            align="start"
            onSelect={(value) => {
              if (value === "team-switch") {
                setTeamDialogOpen(true);
                return;
              }
              router.push(value);
            }}
            onSignOut={() => logoutAndRedirect()}
          />
        </div>
      </nav>

      <Dialog
        open={teamDialogOpen}
        onClose={() => setTeamDialogOpen(false)}
        title="切换团队"
        description="选择要进入的团队"
      >
        <div className="portal-team-list">
          <button
            type="button"
            className="portal-team-item is-active"
            onClick={() => setTeamDialogOpen(false)}
          >
            <span className="portal-team-name">默认团队</span>
            <span className="portal-team-meta">当前</span>
          </button>
          <p className="portal-team-hint">
            多团队切换将在接入租户成员接口后开放。
          </p>
        </div>
      </Dialog>

      {activeGroup ? (
        <aside className="portal-sidebar" aria-label={`${navTitle(activeGroup)}菜单`}>
          <div className="portal-sidebar-title">{navTitle(activeGroup)}</div>
          <nav className="portal-sidebar-nav">
            {childrenOf(activeGroup).map((child) => {
              const path = navPath(child)!;
              const Icon = iconFor(child.icon);
              const active = matches(pathname, path);
              return (
                <Link
                  key={child.id ?? path}
                  href={path}
                  aria-current={active ? "page" : undefined}
                  className={
                    active
                      ? "portal-sidebar-item is-active"
                      : "portal-sidebar-item"
                  }
                >
                  <Icon size={16} strokeWidth={1.8} aria-hidden="true" />
                  <span>{navTitle(child)}</span>
                </Link>
              );
            })}
          </nav>
        </aside>
      ) : null}

      <main className="portal-main">{children}</main>
    </div>
  );
}
