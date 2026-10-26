"use client";

import { useCallback, useState } from "react";

import { Button, Input } from "@pactor-app/ui/components";

import { useUiText } from "@/i18n/ui";

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
    const t = useUiText();
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const submit = useCallback(async () => {
        if (!platformUserId) {
            setError("用户 ID 无效");
            return;
        }
        if (password.length === 0) {
            setError("请输入新密码");
            return;
        }
        if (password !== confirmPassword) {
            setError("两次密码不一致");
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
                    envelope.message ?? `${t("重置失败")} (HTTP ${response.status})`,
                );
            }
            setPassword("");
            setConfirmPassword("");
            onSuccess?.();
        } catch (err) {
            setError(err instanceof Error ? t(err.message) : t("重置失败"));
        } finally {
            setSubmitting(false);
        }
    }, [confirmPassword, onSuccess, password, platformUserId, t]);

    return (
        <div className="flex flex-col gap-4">
            <div className="grid gap-3">
                <label className="flex flex-col gap-1 text-sm">
                    {t("新密码 *")}
                    <Input
                        type="password"
                        value={password}
                        onChange={(value) => setPassword(String(value))}
                        placeholder={t("至少 8 位，含大小写字母与数字")}
                    />
                </label>
                <label className="flex flex-col gap-1 text-sm">
                    {t("确认密码 *")}
                    <Input
                        type="password"
                        value={confirmPassword}
                        onChange={(value) => setConfirmPassword(String(value))}
                    />
                </label>
            </div>
            {error && <p className="text-sm text-destructive">{t(error)}</p>}
            <div className="flex justify-end gap-2">
                <Button onClick={() => onCancel?.()}>{t("取消")}</Button>
                <Button variant="primary" disabled={submitting} onClick={() => void submit()}>
                    {submitting ? t("提交中…") : t("确认重置")}
                </Button>
            </div>
        </div>
    );
}
