"use client";

import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";

import {
  applyConsoleThemeFromRemote,
  CONSOLE_SETTINGS_CHANGE_EVENT,
  DEFAULT_CONSOLE_SITE_NAME,
  fetchConsoleSiteName,
  fetchConsoleTheme,
  fetchNavigationLayoutMode,
  type PlatformNavigationLayoutMode,
} from "./lib/platform-console-settings";

const STORAGE_KEY = "nexus-console-navigation-layout-mode";
const SITE_NAME_STORAGE_KEY = "nexus-console-site-name";

let memoryCache: PlatformNavigationLayoutMode | null = null;
let siteNameMemoryCache: string | null = null;

function readStoredMode(): PlatformNavigationLayoutMode | null {
  if (memoryCache) {
    return memoryCache;
  }
  if (typeof window === "undefined") {
    return null;
  }
  const raw = sessionStorage.getItem(STORAGE_KEY);
  if (raw === "RAIL_DUAL" || raw === "SINGLE_GROUP") {
    memoryCache = raw;
    return raw;
  }
  return null;
}

const SYNC_EVENT = "nexus-navigation-layout-sync";

function persistMode(mode: PlatformNavigationLayoutMode): void {
  memoryCache = mode;
  if (typeof window !== "undefined") {
    sessionStorage.setItem(STORAGE_KEY, mode);
  }
}

function readStoredSiteName(): string | null {
  if (siteNameMemoryCache) {
    return siteNameMemoryCache;
  }
  if (typeof window === "undefined") {
    return null;
  }
  const raw = sessionStorage.getItem(SITE_NAME_STORAGE_KEY);
  if (raw && raw.trim().length > 0) {
    siteNameMemoryCache = raw;
    return raw;
  }
  return null;
}

function persistSiteName(name: string): void {
  const trimmed = name.trim();
  if (trimmed.length === 0) {
    return;
  }
  siteNameMemoryCache = trimmed;
  if (typeof window !== "undefined") {
    sessionStorage.setItem(SITE_NAME_STORAGE_KEY, trimmed);
  }
}

const SITE_NAME_SYNC_EVENT = "nexus-console-site-name-sync";

const NavigationLayoutContext = createContext<PlatformNavigationLayoutMode>("RAIL_DUAL");
const ConsoleSiteNameContext = createContext<string>(DEFAULT_CONSOLE_SITE_NAME);

export function NavigationLayoutProvider({ children }: { children: ReactNode }) {
  const [mode, setMode] = useState<PlatformNavigationLayoutMode>(
    () => readStoredMode() ?? "RAIL_DUAL",
  );
  const [siteName, setSiteName] = useState<string>(
    () => readStoredSiteName() ?? DEFAULT_CONSOLE_SITE_NAME,
  );

  useEffect(() => {
    let cancelled = false;
    fetchNavigationLayoutMode().then((next) => {
      if (cancelled) {
        return;
      }
      persistMode(next);
      setMode((prev) => (prev === next ? prev : next));
    });
    fetchConsoleSiteName().then((next) => {
      if (cancelled) {
        return;
      }
      persistSiteName(next);
      setSiteName((prev) => (prev === next ? prev : next));
    });
    fetchConsoleTheme().then((remote) => {
      if (cancelled || remote == null) {
        return;
      }
      applyConsoleThemeFromRemote(remote);
    });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    const onSync = (event: Event) => {
      const detail = (event as CustomEvent<PlatformNavigationLayoutMode>).detail;
      if (detail === "RAIL_DUAL" || detail === "SINGLE_GROUP") {
        setMode((prev) => (prev === detail ? prev : detail));
      }
    };
    window.addEventListener(SYNC_EVENT, onSync);
    return () => {
      window.removeEventListener(SYNC_EVENT, onSync);
    };
  }, []);

  useEffect(() => {
    const onSiteNameSync = (event: Event) => {
      const detail = (event as CustomEvent<string>).detail;
      if (typeof detail !== "string" || detail.trim().length === 0) {
        return;
      }
      persistSiteName(detail);
      setSiteName((prev) => (prev === detail ? prev : detail));
    };
    window.addEventListener(SITE_NAME_SYNC_EVENT, onSiteNameSync);
    return () => {
      window.removeEventListener(SITE_NAME_SYNC_EVENT, onSiteNameSync);
    };
  }, []);

  useEffect(() => {
    const onChange = () => {
      fetchNavigationLayoutMode().then((next) => {
        persistMode(next);
        setMode((prev) => (prev === next ? prev : next));
      });
      fetchConsoleSiteName().then((next) => {
        persistSiteName(next);
        setSiteName((prev) => (prev === next ? prev : next));
      });
      fetchConsoleTheme().then((remote) => {
        if (remote != null) {
          applyConsoleThemeFromRemote(remote);
        }
      });
    };
    window.addEventListener(CONSOLE_SETTINGS_CHANGE_EVENT, onChange);
    return () => {
      window.removeEventListener(CONSOLE_SETTINGS_CHANGE_EVENT, onChange);
    };
  }, []);

  return (
    <NavigationLayoutContext.Provider value={mode}>
      <ConsoleSiteNameContext.Provider value={siteName}>
        {children}
      </ConsoleSiteNameContext.Provider>
    </NavigationLayoutContext.Provider>
  );
}

export function useNavigationLayoutMode(): PlatformNavigationLayoutMode {
  return useContext(NavigationLayoutContext);
}

export function useConsoleSiteName(): string {
  return useContext(ConsoleSiteNameContext);
}

/** 保存布局后立即写入缓存，避免 remount 时先闪默认双栏。 */
export function persistNavigationLayoutMode(mode: PlatformNavigationLayoutMode): void {
  persistMode(mode);
  if (typeof window !== "undefined") {
    window.dispatchEvent(new CustomEvent(SYNC_EVENT, { detail: mode }));
  }
}
