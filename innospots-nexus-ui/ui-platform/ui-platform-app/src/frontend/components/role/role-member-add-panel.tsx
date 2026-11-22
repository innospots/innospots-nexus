"use client";

import { useCallback, useEffect, useMemo, useState } from "react";

import { Button, Input } from "@pactor-app/ui/components";

import { useI18n } from "@pactor-app/runtime";

import { platformJson } from "../../lib/platform-api";

type PlatformUserRow = {
    platformUserId: string;
    loginName: string;
    displayName?: string | null;
    email?: string | null;
};

type PageResult<T> = {
    records: T[];
    total: number;
    pageNo: number;
    pageSize: number;
};

export interface RoleMemberAddPanelProps {
    roleId: string;
    roleName?: string | null;
    onCancel?: () => void;
    onSuccess?: () => void;
}

function formatErrorMessage(message: string, translate: (key: string) => string): string {
    if (message.startsWith("common.") || message.startsWith("role.")) {
        return translate(message);
    }
    return message;
}

export function RoleMemberAddPanel({ roleId, roleName, onCancel, onSuccess }: RoleMemberAddPanelProps) {
    const { t, locale, defaultLocale } = useI18n();
    const effectiveRoleId = typeof roleId === "string" ? roleId.trim() : "";
    const [searchInput, setSearchInput] = useState("");
    const [debouncedSearch, setDebouncedSearch] = useState("");
    const [loadingUsers, setLoadingUsers] = useState(true);
    const [userRows, setUserRows] = useState<PlatformUserRow[]>([]);
    const [boundUserIds, setBoundUserIds] = useState<Set<string>>(() => new Set());
    const [pending, setPending] = useState<PlatformUserRow[]>([]);
    const [submitting, setSubmitting] = useState(false);
    const [errorKey, setErrorKey] = useState<string | null>(null);

    const pendingIds = useMemo(() => new Set(pending.map((row) => row.platformUserId)), [pending]);

    const errorMessage = errorKey ? formatErrorMessage(errorKey, t) : null;

    useEffect(() => {
        const timer = window.setTimeout(() => {
            setDebouncedSearch(searchInput.trim());
        }, 300);
        return () => {
            window.clearTimeout(timer);
        };
    }, [searchInput]);

    useEffect(() => {
        if (!effectiveRoleId) {
            return;
        }
        let cancelled = false;
        (async () => {
            try {
                const page = await platformJson<PageResult<{ subjectId: string; subjectType: string }>>(
                    `/roles/${encodeURIComponent(effectiveRoleId)}/bindings?subjectType=USER&pageNo=1&pageSize=500`,
                    { method: "GET" },
                    locale,
                    defaultLocale,
                );
                if (cancelled) {
                    return;
                }
                const ids = new Set<string>();
                for (const row of page.records ?? []) {
                    if (row.subjectType === "USER" && row.subjectId) {
                        ids.add(row.subjectId);
                    }
                }
                setBoundUserIds(ids);
            } catch {
                if (!cancelled) {
                    setBoundUserIds(new Set());
                }
            }
        })();
        return () => {
            cancelled = true;
        };
    }, [defaultLocale, effectiveRoleId, locale]);

    useEffect(() => {
        if (!effectiveRoleId) {
            setErrorKey("role.role-detail-basic.add-member-missing-role-id");
            setLoadingUsers(false);
            return;
        }
        let cancelled = false;
        setLoadingUsers(true);
        setErrorKey(null);
        (async () => {
            try {
                const params = new URLSearchParams({
                    pageNo: "1",
                    pageSize: "50",
                });
                if (debouncedSearch.length > 0) {
                    params.set("input", debouncedSearch);
                }
                const page = await platformJson<PageResult<PlatformUserRow>>(
                    `/users?${params.toString()}`,
                    { method: "GET" },
                    locale,
                    defaultLocale,
                );
                if (cancelled) {
                    return;
                }
                setUserRows(page.records ?? []);
            } catch (err) {
                if (cancelled) {
                    return;
                }
                const message = err instanceof Error ? err.message : "common.ui.request-failed";
                setErrorKey(message);
                setUserRows([]);
            } finally {
                if (!cancelled) {
                    setLoadingUsers(false);
                }
            }
        })();
        return () => {
            cancelled = true;
        };
    }, [debouncedSearch, defaultLocale, effectiveRoleId, locale]);

    const togglePending = useCallback((user: PlatformUserRow) => {
        setPending((current) => {
            if (current.some((row) => row.platformUserId === user.platformUserId)) {
                return current.filter((row) => row.platformUserId !== user.platformUserId);
            }
            return [...current, user];
        });
    }, []);

    const removePending = useCallback((platformUserId: string) => {
        setPending((current) => current.filter((row) => row.platformUserId !== platformUserId));
    }, []);

    const submit = useCallback(async () => {
        if (!effectiveRoleId) {
            setErrorKey("role.role-detail-basic.add-member-missing-role-id");
            return;
        }
        if (pending.length === 0) {
            setErrorKey("role.role-detail-basic.add-member-select-at-least-one");
            return;
        }
        setSubmitting(true);
        setErrorKey(null);
        try {
            await platformJson<void>(
                `/roles/${encodeURIComponent(effectiveRoleId)}/bindings`,
                {
                    method: "POST",
                    body: JSON.stringify({
                        subjectType: "USER",
                        subjectIds: pending.map((row) => row.platformUserId),
                    }),
                },
                locale,
                defaultLocale,
            );
            onSuccess?.();
        } catch (err) {
            const message = err instanceof Error ? err.message : "common.ui.request-failed";
            setErrorKey(message);
        } finally {
            setSubmitting(false);
        }
    }, [defaultLocale, effectiveRoleId, locale, onSuccess, pending]);

    const selectableUsers = useMemo(() => {
        return userRows.filter((row) => !boundUserIds.has(row.platformUserId));
    }, [boundUserIds, userRows]);

    const showInitialLoading = loadingUsers && userRows.length === 0;

    return (
        <div className="flex flex-col gap-4">
            {roleName ? (
                <p className="text-sm text-muted-foreground m-0">
                    {t("role.role-detail-basic.add-to-role", { roleName })}
                </p>
            ) : null}
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 md:min-h-[360px]">
                <div className="flex flex-col gap-2 rounded-md border border-border p-3">
                    <div className="text-sm font-medium">{t("role.role-detail-basic.add-member-available-users")}</div>
                    <Input
                        value={searchInput}
                        placeholder={t("role.role-detail-basic.add-member-search-login-name")}
                        onChange={(value) => {
                            setSearchInput(value);
                        }}
                    />
                    {showInitialLoading ? (
                        <p className="text-sm text-muted-foreground m-0">{t("common.ui.loading")}</p>
                    ) : null}
                    <div
                        className={`flex flex-col gap-1 overflow-y-auto max-h-[280px] mt-1${loadingUsers ? " opacity-60" : ""}`}
                    >
                        {selectableUsers.length === 0 && !showInitialLoading ? (
                            <p className="text-sm text-muted-foreground m-0">
                                {t("role.role-detail-basic.add-member-no-users")}
                            </p>
                        ) : null}
                        {selectableUsers.map((user) => {
                            const checked = pendingIds.has(user.platformUserId);
                            const label = user.displayName?.trim()
                                ? `${user.loginName} · ${user.displayName}`
                                : user.loginName;
                            return (
                                <label
                                    key={user.platformUserId}
                                    className="flex items-start gap-2 rounded px-2 py-1.5 text-sm hover:bg-muted/60 cursor-pointer"
                                >
                                    <input
                                        type="checkbox"
                                        className="mt-0.5"
                                        checked={checked}
                                        disabled={loadingUsers}
                                        onChange={() => {
                                            togglePending(user);
                                        }}
                                    />
                                    <span className="flex flex-col gap-0.5 min-w-0">
                                        <span className="font-medium truncate">{label}</span>
                                        {user.email ? (
                                            <span className="text-muted-foreground text-xs truncate">{user.email}</span>
                                        ) : null}
                                    </span>
                                </label>
                            );
                        })}
                    </div>
                </div>
                <div className="flex flex-col gap-2 rounded-md border border-border p-3">
                    <div className="text-sm font-medium">{t("role.role-detail-basic.add-member-pending-list")}</div>
                    <div className="flex flex-col gap-1 overflow-y-auto max-h-[320px] mt-1 flex-1">
                        {pending.length === 0 ? (
                            <p className="text-sm text-muted-foreground m-0">
                                {t("role.role-detail-basic.add-member-pending-empty")}
                            </p>
                        ) : null}
                        {pending.map((user) => (
                            <div
                                key={user.platformUserId}
                                className="flex items-center justify-between gap-2 rounded bg-muted/40 px-2 py-1.5 text-sm"
                            >
                                <span className="min-w-0 truncate">
                                    {user.loginName}
                                    {user.displayName ? ` · ${user.displayName}` : ""}
                                </span>
                                <Button
                                    variant="link"
                                    size="small"
                                    text={t("role.role-detail-basic.add-member-remove-from-pending")}
                                    onClick={() => {
                                        removePending(user.platformUserId);
                                    }}
                                />
                            </div>
                        ))}
                    </div>
                </div>
            </div>
            {errorMessage ? <p className="text-sm text-destructive m-0">{errorMessage}</p> : null}
            <div className="flex justify-end gap-2 pt-2">
                <Button
                    text={t("role.role-detail-basic.cancel")}
                    onClick={() => {
                        onCancel?.();
                    }}
                />
                <Button
                    variant="primary"
                    text={t("role.role-detail-basic.add-member-submit")}
                    disabled={submitting || pending.length === 0 || !effectiveRoleId}
                    onClick={() => {
                        void submit();
                    }}
                />
            </div>
        </div>
    );
}
