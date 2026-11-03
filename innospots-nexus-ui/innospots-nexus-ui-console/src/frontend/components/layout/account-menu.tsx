"use client";

import { useEffect, useRef, useState, useSyncExternalStore } from "react";
import {
  KeyRound,
  Languages,
  LogOut,
  Monitor,
  Moon,
  Sun,
} from "lucide-react";
import { useRouter } from "next/navigation";

import { useHostI18n } from "../../i18n/messages";
import {
  applySkinStylesheet,
  DEFAULT_SKIN,
  PREFERENCES_CHANGE_EVENT,
  resolveSkin,
  SKIN_STORAGE_KEY,
} from "../../skin";
import type { SessionUser } from "../../types/session";

type Mode = "light" | "dark" | "system";
const MODE_KEY = "mc-mode";

function subscribe(cb: () => void) {
  window.addEventListener("storage", cb);
  window.addEventListener(PREFERENCES_CHANGE_EVENT, cb);
  return () => {
    window.removeEventListener("storage", cb);
    window.removeEventListener(PREFERENCES_CHANGE_EVENT, cb);
  };
}

function skinSnapshot() {
  return resolveSkin(localStorage.getItem(SKIN_STORAGE_KEY));
}

function modeSnapshot(): Mode {
  const mode = localStorage.getItem(MODE_KEY);
  if (mode === "light" || mode === "dark") {
    return mode;
  }
  return "system";
}

function systemSubscribe(cb: () => void) {
  const mq = window.matchMedia("(prefers-color-scheme: dark)");
  mq.addEventListener("change", cb);
  return () => mq.removeEventListener("change", cb);
}

function systemSnapshot() {
  return window.matchMedia("(prefers-color-scheme: dark)").matches;
}

export function AccountMenu({
  user,
  className,
}: {
  user?: SessionUser;
  className?: string;
}) {
  const [open, setOpen] = useState(false);
  const [error, setError] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  const router = useRouter();
  const { locale, changeLocale, t } = useHostI18n();
  const skin = useSyncExternalStore(subscribe, skinSnapshot, () => DEFAULT_SKIN);
  const mode = useSyncExternalStore(subscribe, modeSnapshot, () => "system" as Mode);
  const systemDark = useSyncExternalStore(systemSubscribe, systemSnapshot, () => false);

  useEffect(() => {
    applySkinStylesheet(skin);
  }, [skin]);

  useEffect(() => {
    if (mode === "dark" || (mode === "system" && systemDark)) {
      document.documentElement.setAttribute("data-pactor-theme", "dark");
    } else {
      document.documentElement.removeAttribute("data-pactor-theme");
    }
  }, [mode, systemDark]);

  useEffect(() => {
    if (!open) {
      return;
    }
    function outside(e: PointerEvent) {
      if (!ref.current?.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    function keyboard(e: KeyboardEvent) {
      if (e.key === "Escape") {
        setOpen(false);
        ref.current?.querySelector<HTMLButtonElement>(".account-trigger")?.focus();
      }
    }
    document.addEventListener("pointerdown", outside);
    document.addEventListener("keydown", keyboard);
    return () => {
      document.removeEventListener("pointerdown", outside);
      document.removeEventListener("keydown", keyboard);
    };
  }, [open]);

  function changeMode(value: Mode) {
    localStorage.setItem(MODE_KEY, value);
    window.dispatchEvent(new Event(PREFERENCES_CHANGE_EVENT));
  }

  async function logout() {
    try {
      const response = await fetch("/api/auth/logout", { method: "POST" });
      if (!response.ok) {
        throw new Error("Logout failed");
      }
      router.replace("/login");
      router.refresh();
    } catch {
      setError(true);
    }
  }

  const displayName = [user?.name, user?.username].find(
    (value) => value && !/^\{\{[^{}]+\}\}$/.test(value),
  );
  const name = displayName || t("account.defaultUser");
  const username = displayName ? (user?.username || "") : "";
  const initial = name.trim().slice(0, 1).toUpperCase();

  return (
    <div ref={ref} className={className ? `account-menu-wrap ${className}` : "account-menu-wrap"}>
      <button
        className="account-trigger"
        type="button"
        aria-label={t("account.menu")}
        aria-expanded={open}
        aria-controls="account-popover"
        onClick={() => setOpen(!open)}
      >
        <span className="account-trigger-avatar">{initial}</span>
        <span className="account-trigger-label">{t("account.label")}</span>
      </button>
      {open ? (
        <div
          id="account-popover"
          className="account-popover"
          role="dialog"
          aria-label={t("account.popover")}
        >
          <div className="account-identity">
            <span className="account-avatar">{initial}</span>
            <div>
              <strong>{name}</strong>
              <small>{username}</small>
            </div>
          </div>
          <button
            className="account-row account-action"
            type="button"
            onClick={() => {
              setOpen(false);
              router.push("/account/password");
            }}
          >
            <KeyRound size={19} />
            <span>{t("account.changePassword")}</span>
          </button>
          <div className="account-preferences">
            <label className="account-row">
              <Languages size={19} />
              <span>{t("account.language")}</span>
              <select
                aria-label={t("account.language")}
                value={locale}
                onChange={(e) => changeLocale(e.target.value)}
              >
                <option value="zh-CN">{t("account.languageZh")}</option>
                <option value="en-US">{t("account.languageEn")}</option>
              </select>
            </label>
            <div className="account-row">
              <Sun size={19} />
              <span>{t("account.appearance")}</span>
              <div
                className="account-mode"
                role="group"
                aria-label={t("account.appearance")}
              >
                {(
                  [
                    ["light", Sun, "account.light"],
                    ["dark", Moon, "account.dark"],
                    ["system", Monitor, "account.system"],
                  ] as const
                ).map(([value, Icon, label]) => (
                  <button
                    type="button"
                    key={value}
                    aria-label={t(label)}
                    title={t(label)}
                    aria-pressed={mode === value}
                    onClick={() => changeMode(value)}
                  >
                    <Icon size={17} />
                  </button>
                ))}
              </div>
            </div>
          </div>
          <button
            className="account-row account-action account-logout"
            type="button"
            onClick={logout}
          >
            <LogOut size={19} />
            <span>{t("account.signOut")}</span>
          </button>
          {error ? (
            <p role="alert" className="account-error">
              {t("account.signOutError")}
            </p>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}
