"use client";

import { useCallback, useMemo, useState } from "react";

import { Button, Input } from "@pactor-app/ui/components";

import { useUiText } from "@/i18n/ui";

import { encryptPasswordForApi } from "../lib/rsa-password";

type ProvisionMode = "invite" | "direct";
type ContactType = "email" | "mobile";

export interface PlatformUserCreatePanelProps {
    onCancel?: () => void;
    onCreated?: (user: { platformUserId?: string; loginName?: string }) => void;
}

export function PlatformUserCreatePanel({ onCancel, onCreated }: PlatformUserCreatePanelProps) {
    const t = useUiText();
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
            setError("请填写登录名、联系方式，并确保两次密码一致。");
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
                    envelope.message ?? `${t("创建失败")} (HTTP ${response.status})`,
                );
            }
            onCreated?.(envelope.data ?? {});
        } catch (err) {
            setError(err instanceof Error ? t(err.message) : t("创建失败"));
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
                <span className="text-sm font-medium">{t("开通方式 *")}</span>
                <div className="flex flex-wrap gap-4 text-sm">
                    <label className="flex cursor-pointer items-center gap-2">
                        <input
                            type="radio"
                            name="provisionMode"
                            checked={mode === "invite"}
                            onChange={() => resetModeFields("invite")}
                        />
                        {t("发送邀请（推荐）")}
                    </label>
                    <label className="flex cursor-pointer items-center gap-2">
                        <input
                            type="radio"
                            name="provisionMode"
                            checked={mode === "direct"}
                            onChange={() => resetModeFields("direct")}
                        />
                        {t("直接创建账号（实施权限）")}
                    </label>
                </div>
            </div>

            <div className="rounded-md border border-border p-4">
                <p className="mb-3 text-sm font-medium">{t("基本信息")}</p>
                <div className="grid gap-3 md:grid-cols-2">
                    <label className="flex flex-col gap-1 text-sm">
                        {t("登录名")} {mode === "direct" ? "*" : ""}
                        <Input
                            value={loginName}
                            onChange={(value) => setLoginName(String(value))}
                            placeholder={
                                mode === "direct"
                                    ? t("2–64 字符，创建后不可改")
                                    : t("可选，接受邀请时可修改")
                            }
                        />
                    </label>
                    <label className="flex flex-col gap-1 text-sm">
                        {t("显示名称")}
                        <Input
                            value={displayName}
                            onChange={(value) => setDisplayName(String(value))}
                            placeholder={t("在控制台展示的名称")}
                        />
                    </label>
                </div>
                <div className="mt-3 flex flex-col gap-2">
                    <span className="text-sm font-medium">{t("联系方式 *（二选一）")}</span>
                    <div className="flex gap-4 text-sm">
                        <label className="flex cursor-pointer items-center gap-2">
                            <input
                                type="radio"
                                checked={contactType === "email"}
                                onChange={() => setContactType("email")}
                            />
                            {t("邮箱")}
                        </label>
                        <label className="flex cursor-pointer items-center gap-2">
                            <input
                                type="radio"
                                checked={contactType === "mobile"}
                                onChange={() => setContactType("mobile")}
                            />
                            {t("手机号码")}
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
                            placeholder={t("11 位手机号")}
                        />
                    )}
                </div>
            </div>

            {mode === "invite" && (
                <div className="rounded-md border border-dashed border-amber-500/50 bg-amber-500/5 p-4 text-sm text-muted-foreground">
                    <p className="font-medium text-foreground">{t("邀请开通")}</p>
                    <p className="mt-1">
                        {t("POST /platform/invites 尚未就绪。请切换为「直接创建账号」完成开通，或等待邀请 API 发布后在本页提交邀请单。")}
                    </p>
                </div>
            )}

            {mode === "direct" && (
                <div className="rounded-md border border-border p-4">
                    <p className="mb-3 text-sm font-medium">{t("账号与密码")}</p>
                    <div className="grid gap-3 md:grid-cols-2">
                        <label className="flex flex-col gap-1 text-sm">
                            {t("初始密码 *")}
                            <Input
                                type="password"
                                value={password}
                                onChange={(value) => setPassword(String(value))}
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
                    <label className="mt-3 flex flex-col gap-1 text-sm">
                        {t("默认角色（可选，逗号分隔角色编码）")}
                        <Input
                            value={roleCodes}
                            onChange={(value) => setRoleCodes(String(value))}
                            placeholder={t("如 PLATFORM_OPERATOR")}
                        />
                    </label>
                </div>
            )}

            {error && <p className="text-sm text-destructive">{t(error)}</p>}

            <div className="flex justify-end gap-2">
                <Button onClick={() => onCancel?.()}>
                    {t("取消")}
                </Button>
                {mode === "direct" && (
                    <Button
                        variant="primary"
                        disabled={submitting || !canSubmitDirect}
                        onClick={() => void submitDirect()}
                    >
                        {submitting ? t("提交中…") : t("提交")}
                    </Button>
                )}
            </div>
        </div>
    );
}
