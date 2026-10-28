"use client";

import { useCallback, useMemo, useState } from "react";

import { Button, Input } from "@pactor-app/ui/components";

import { useI18n } from "@pactor-app/runtime";

import { encryptPasswordForApi } from "../lib/rsa-password";

type ProvisionMode = "invite" | "direct";
type ContactType = "email" | "mobile";

export interface PlatformUserCreatePanelProps {
    onCancel?: () => void;
    onCreated?: (user: { platformUserId?: string; loginName?: string }) => void;
}

export function PlatformUserCreatePanel({ onCancel, onCreated }: PlatformUserCreatePanelProps) {
    const { t } = useI18n();
    const [mode, setMode] = useState<ProvisionMode>("invite");
    const [contactType, setContactType] = useState<ContactType>("email");
    const [loginName, setLoginName] = useState("");
    const [displayName, setDisplayName] = useState("");
    const [email, setEmail] = useState("");
    const [mobile, setMobile] = useState("");
    const [roleCodes, setRoleCodes] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const contactValue = contactType === "email" ? email.trim() : mobile.trim();

    const canSubmitDirect = useMemo(() => {
        if (loginName.trim().length < 2) {
            return false;
        }
        if (contactValue.length === 0) {
            return false;
        }
        if (password.length === 0 || password !== confirmPassword) {
            return false;
        }
        return true;
    }, [confirmPassword, contactValue, loginName, password]);

    const resetModeFields = useCallback((next: ProvisionMode) => {
        setMode(next);
        setRoleCodes("");
        setPassword("");
        setConfirmPassword("");
        setError(null);
    }, []);

    const submitDirect = useCallback(async () => {
        if (!canSubmitDirect) {
            setError("common.ui.enter-a-login-name-and-contact-details-and-make-sure-both-passwords-matc");
            return;
        }
        setSubmitting(true);
        setError(null);
        try {
            const encryptedPassword = await encryptPasswordForApi(password);
            const body: Record<string, string> = {
                loginName: loginName.trim(),
                encryptedPassword,
            };
            if (displayName.trim()) {
                body.displayName = displayName.trim();
            }
            if (contactType === "email") {
                body.email = email.trim();
            } else {
                body.mobile = mobile.trim();
            }
            if (roleCodes.trim()) {
                body.roleCodes = roleCodes.trim();
            }
            const response = await fetch("/api/d/platform/users", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body),
            });
            const envelope = (await response.json()) as {
                success?: boolean;
                message?: string;
                data?: { platformUserId?: string; loginName?: string };
            };
            if (!response.ok || envelope.success === false) {
                throw new Error(
                    envelope.message ?? `${t("common.ui.create-failed")} (HTTP ${response.status})`,
                );
            }
            onCreated?.(envelope.data ?? {});
        } catch (err) {
            setError(err instanceof Error ? err.message : "common.ui.create-failed");
        } finally {
            setSubmitting(false);
        }
    }, [
        canSubmitDirect,
        contactType,
        displayName,
        email,
        loginName,
        mobile,
        onCreated,
        password,
        roleCodes,
        t,
    ]);

    return (
        <div className="flex flex-col gap-4">
            <div className="flex flex-col gap-2">
                <span className="text-sm font-medium">{t("common.ui.provisioning-method")}</span>
                <div className="flex flex-wrap gap-4 text-sm">
                    <label className="flex cursor-pointer items-center gap-2">
                        <input
                            type="radio"
                            name="provisionMode"
                            checked={mode === "invite"}
                            onChange={() => resetModeFields("invite")}
                        />
                        {t("common.ui.send-invitation-recommended")}
                    </label>
                    <label className="flex cursor-pointer items-center gap-2">
                        <input
                            type="radio"
                            name="provisionMode"
                            checked={mode === "direct"}
                            onChange={() => resetModeFields("direct")}
                        />
                        {t("common.ui.create-account-directly-implementation-permission")}
                    </label>
                </div>
            </div>

            <div className="rounded-md border border-border p-4">
                <p className="mb-3 text-sm font-medium">{t("common.ui.basic-information")}</p>
                <div className="grid gap-3 md:grid-cols-2">
                    <label className="flex flex-col gap-1 text-sm">
                        {t("common.ui.login-name")} {mode === "direct" ? "*" : ""}
                        <Input
                            value={loginName}
                            onChange={(value) => setLoginName(String(value))}
                            placeholder={
                                mode === "direct"
                                    ? t("common.ui.2-64-characters-immutable-after-creation")
                                    : t("common.ui.optional-editable-when-accepting-the-invitation")
                            }
                        />
                    </label>
                    <label className="flex flex-col gap-1 text-sm">
                        {t("common.ui.display-name")}
                        <Input
                            value={displayName}
                            onChange={(value) => setDisplayName(String(value))}
                            placeholder={t("common.ui.name-shown-in-the-console")}
                        />
                    </label>
                </div>
                <div className="mt-3 flex flex-col gap-2">
                    <span className="text-sm font-medium">{t("common.ui.contact-choose-one")}</span>
                    <div className="flex gap-4 text-sm">
                        <label className="flex cursor-pointer items-center gap-2">
                            <input
                                type="radio"
                                checked={contactType === "email"}
                                onChange={() => setContactType("email")}
                            />
                            {t("common.ui.email")}
                        </label>
                        <label className="flex cursor-pointer items-center gap-2">
                            <input
                                type="radio"
                                checked={contactType === "mobile"}
                                onChange={() => setContactType("mobile")}
                            />
                            {t("common.ui.mobile-number")}
                        </label>
                    </div>
                    {contactType === "email" ? (
                        <Input
                            value={email}
                            onChange={(value) => setEmail(String(value))}
                            placeholder="user@example.com"
                        />
                    ) : (
                        <Input
                            value={mobile}
                            onChange={(value) => setMobile(String(value))}
                            placeholder={t("common.ui.11-digit-mobile-number")}
                        />
                    )}
                </div>
            </div>

            {mode === "invite" && (
                <div className="rounded-md border border-dashed border-amber-500/50 bg-amber-500/5 p-4 text-sm text-muted-foreground">
                    <p className="font-medium text-foreground">{t("common.ui.invitation")}</p>
                    <p className="mt-1">
                        {t("common.ui.post-platform-invites-is-not-ready-switch-to-create-account-directly-to")}
                    </p>
                </div>
            )}

            {mode === "direct" && (
                <div className="rounded-md border border-border p-4">
                    <p className="mb-3 text-sm font-medium">{t("common.ui.account-and-password")}</p>
                    <div className="grid gap-3 md:grid-cols-2">
                        <label className="flex flex-col gap-1 text-sm">
                            {t("common.ui.initial-password")}
                            <Input
                                type="password"
                                value={password}
                                onChange={(value) => setPassword(String(value))}
                            />
                        </label>
                        <label className="flex flex-col gap-1 text-sm">
                            {t("common.ui.confirm-password-2")}
                            <Input
                                type="password"
                                value={confirmPassword}
                                onChange={(value) => setConfirmPassword(String(value))}
                            />
                        </label>
                    </div>
                    <label className="mt-3 flex flex-col gap-1 text-sm">
                        {t("common.ui.default-roles-optional-comma-separated-role-codes")}
                        <Input
                            value={roleCodes}
                            onChange={(value) => setRoleCodes(String(value))}
                            placeholder={t("common.ui.e-g-platform-operator")}
                        />
                    </label>
                </div>
            )}

            {error && <p className="text-sm text-destructive">{t(error)}</p>}

            <div className="flex justify-end gap-2">
                <Button onClick={() => onCancel?.()}>
                    {t("common.ui.cancel")}
                </Button>
                {mode === "direct" && (
                    <Button
                        variant="primary"
                        disabled={submitting || !canSubmitDirect}
                        onClick={() => void submitDirect()}
                    >
                        {submitting ? t("common.ui.submitting") : t("common.ui.submit")}
                    </Button>
                )}
            </div>
        </div>
    );
}
