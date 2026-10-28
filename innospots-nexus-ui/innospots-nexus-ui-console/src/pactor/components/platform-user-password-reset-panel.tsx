"use client";

import { useCallback, useState } from "react";

import { Button, Input } from "@pactor-app/ui/components";

import { useI18n } from "@pactor-app/runtime";

import { encryptPasswordForApi } from "../lib/rsa-password";

export interface PlatformUserPasswordResetPanelProps {
    platformUserId: string;
    onCancel?: () => void;
    onSuccess?: () => void;
}

export function PlatformUserPasswordResetPanel({
    platformUserId,
    onCancel,
    onSuccess,
}: PlatformUserPasswordResetPanelProps) {
    const { t } = useI18n();
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const submit = useCallback(async () => {
        if (!platformUserId) {
            setError("common.ui.invalid-user-id");
            return;
        }
        if (password.length === 0) {
            setError("common.ui.enter-a-new-password");
            return;
        }
        if (password !== confirmPassword) {
            setError("common.ui.passwords-do-not-match");
            return;
        }
        setSubmitting(true);
        setError(null);
        try {
            const encryptedPassword = await encryptPasswordForApi(password);
            const response = await fetch(`/api/d/platform/users/${platformUserId}/password`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ encryptedPassword }),
            });
            const envelope = (await response.json()) as {
                success?: boolean;
                message?: string;
            };
            if (!response.ok || envelope.success === false) {
                throw new Error(
                    envelope.message ?? `${t("common.ui.reset-failed")} (HTTP ${response.status})`,
                );
            }
            setPassword("");
            setConfirmPassword("");
            onSuccess?.();
        } catch (err) {
            setError(err instanceof Error ? err.message : "common.ui.reset-failed");
        } finally {
            setSubmitting(false);
        }
    }, [confirmPassword, onSuccess, password, platformUserId, t]);

    return (
        <div className="flex flex-col gap-4">
            <div className="grid gap-3">
                <label className="flex flex-col gap-1 text-sm">
                    {t("common.ui.new-password-2")}
                    <Input
                        type="password"
                        value={password}
                        onChange={(value) => setPassword(String(value))}
                        placeholder={t("common.ui.at-least-8-characters-with-uppercase-lowercase-and-numbers")}
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
            {error && <p className="text-sm text-destructive">{t(error)}</p>}
            <div className="flex justify-end gap-2">
                <Button onClick={() => onCancel?.()}>{t("common.ui.cancel")}</Button>
                <Button variant="primary" disabled={submitting} onClick={() => void submit()}>
                    {submitting ? t("common.ui.submitting") : t("common.ui.confirm-reset")}
                </Button>
            </div>
        </div>
    );
}
