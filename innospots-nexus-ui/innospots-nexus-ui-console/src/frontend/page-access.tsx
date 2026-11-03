"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";

import { pageAccessEndpoint } from "./config";
import type { ConsoleLocale } from "./locale";
import { translateGate } from "./i18n/messages";

export interface PageAccessResult {
  allowed: boolean;
  reason?: string;
  redirectTo?: string;
}

export interface CommittedPage {
  pathname: string;
  search: string;
}

type GateStatus =
  | { status: "checking" }
  | { status: "allowed" }
  | { status: "denied"; reason: string; redirectTo?: string };

interface Denial {
  pathname: string;
  reason: string;
  redirectTo?: string;
}

export async function verifyPageAccess(
  pathname: string,
  locale?: ConsoleLocale | null,
): Promise<PageAccessResult> {
  try {
    const response = await fetch(pageAccessEndpoint, {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({ path: pathname }),
      cache: "no-store",
      credentials: "same-origin",
    });
    if (!response.ok) {
      return {
        allowed: false,
        reason: translateGate(locale, "gate.error.http", { status: response.status }),
      };
    }
    const result = (await response.json()) as Partial<PageAccessResult>;
    return {
      allowed: result.allowed === true,
      reason: result.reason,
      redirectTo: result.redirectTo,
    };
  } catch (error) {
    return {
      allowed: false,
      reason: translateGate(locale, "gate.error.unavailable", {
        message: error instanceof Error ? error.message : String(error),
      }),
    };
  }
}

export function usePageAccessGate(
  pathname: string,
  search: string,
  replace: (to: string) => void,
  locale?: ConsoleLocale | null,
): {
  committed: CommittedPage | null;
  status: GateStatus;
  retry: () => void;
} {
  const [committedPathname, setCommittedPathname] = useState<string | null>(null);
  const [committedSearch, setCommittedSearch] = useState("");
  const [denial, setDenial] = useState<Denial | null>(null);
  const sequence = useRef(0);

  const applyResult = useCallback(
    (
      targetPathname: string,
      targetSearch: string,
      result: PageAccessResult,
    ) => {
      if (result.allowed) {
        setDenial(null);
        setCommittedPathname(targetPathname);
        setCommittedSearch(targetSearch);
        return;
      }
      setDenial({
        pathname: targetPathname,
        reason: result.reason ?? translateGate(locale, "gate.denied.noAccess"),
        redirectTo: result.redirectTo,
      });
      if (
        result.redirectTo !== undefined
        && result.redirectTo !== targetPathname
      ) {
        replace(result.redirectTo);
      }
    },
    [replace, locale],
  );

  const runCheck = useCallback(
    (targetPathname: string, targetSearch: string) => {
      const requestId = ++sequence.current;
      void verifyPageAccess(targetPathname, locale).then((result) => {
        if (requestId === sequence.current) {
          applyResult(targetPathname, targetSearch, result);
        }
      });
    },
    [applyResult, locale],
  );

  useEffect(() => {
    if (committedPathname === pathname) {
      return;
    }
    runCheck(pathname, search);
    return () => {
      sequence.current += 1;
    };
  }, [pathname, search, committedPathname, runCheck]);

  const retry = useCallback(() => {
    runCheck(pathname, search);
  }, [runCheck, pathname, search]);

  const committed = useMemo<CommittedPage | null>(() => {
    if (committedPathname === null) {
      return null;
    }
    return {
      pathname: committedPathname,
      search: committedPathname === pathname ? search : committedSearch,
    };
  }, [committedPathname, committedSearch, pathname, search]);

  const status = useMemo<GateStatus>(() => {
    if (committedPathname !== null && committedPathname === pathname) {
      return { status: "allowed" };
    }
    if (denial !== null && denial.pathname === pathname) {
      return denial.redirectTo !== undefined
        ? {
            status: "denied",
            reason: denial.reason,
            redirectTo: denial.redirectTo,
          }
        : { status: "denied", reason: denial.reason };
    }
    return { status: "checking" };
  }, [committedPathname, pathname, denial]);

  return { committed, status, retry };
}

export function PageAccessGateLoading({ text }: { text: string }) {
  return (
    <div
      style={{
        minHeight: "60vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        color: "var(--muted-foreground, #6b7280)",
        fontSize: 14,
      }}
    >
      {text}
    </div>
  );
}

export function PageAccessPending({ locale }: { locale?: ConsoleLocale | null }) {
  return (
    <div
      role="status"
      style={{
        position: "fixed",
        top: 12,
        left: "50%",
        transform: "translateX(-50%)",
        zIndex: 1000,
        padding: "6px 14px",
        borderRadius: 999,
        border: "1px solid var(--border, #e5e7eb)",
        background: "var(--card, #ffffff)",
        boxShadow: "0 4px 12px rgb(0 0 0 / 0.08)",
        color: "var(--muted-foreground, #6b7280)",
        fontSize: 12,
      }}
    >
      {translateGate(locale, "gate.checking")}
    </div>
  );
}

const buttonStyle = {
  padding: "6px 16px",
  borderRadius: 6,
  border: "1px solid var(--border, #d1d5db)",
  background: "var(--card, #ffffff)",
  color: "var(--foreground, #111827)",
  cursor: "pointer",
  fontSize: 14,
} as const;

const primaryButtonStyle = {
  ...buttonStyle,
  borderColor: "transparent",
  background: "var(--primary, #111827)",
  color: "var(--primary-foreground, #ffffff)",
} as const;

export function PageAccessDenied({
  reason,
  onRetry,
  onBack,
  onHome,
  locale,
}: {
  reason: string;
  onRetry: () => void;
  onBack?: () => void;
  onHome: () => void;
  locale?: ConsoleLocale | null;
}) {
  return (
    <div
      style={{
        minHeight: "60vh",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: 12,
        padding: 24,
        textAlign: "center",
      }}
    >
      <div style={{ fontSize: 20, fontWeight: 600 }}>
        {translateGate(locale, "gate.denied.title")}
      </div>
      <div style={{ color: "var(--muted-foreground, #6b7280)" }}>{reason}</div>
      <div style={{ display: "flex", gap: 8, marginTop: 4 }}>
        <button type="button" style={primaryButtonStyle} onClick={onRetry}>
          {translateGate(locale, "gate.denied.retry")}
        </button>
        {onBack !== undefined ? (
          <button type="button" style={buttonStyle} onClick={onBack}>
            {translateGate(locale, "gate.denied.back")}
          </button>
        ) : null}
        <button type="button" style={buttonStyle} onClick={onHome}>
          {translateGate(locale, "gate.denied.home")}
        </button>
      </div>
    </div>
  );
}
