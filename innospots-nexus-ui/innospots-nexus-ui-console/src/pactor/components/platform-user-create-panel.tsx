"use client";

import { useCallback, useMemo, useState } from "react";

import { Button, Input } from "@pactor-app/ui/components";

import { encryptPasswordForApi } from "../lib/rsa-password";

type ProvisionMode = "invite" | "direct";
type ContactType = "email" | "mobile";

export interface PlatformUserCreatePanelProps {
    onCancel?: () => void;
    onCreated?: (user: { platformUserId?: string; loginName?: string }) => void;
}

export function PlatformUserCreatePanel({ onCancel, onCreated }: PlatformUserCreatePanelProps) {
    const [mode, setMode] = useState<ProvisionMode>("invite");
    const [contactType, setContactType] = useState<ContactType>("email");
    const [loginName, setLoginName] = useState("");
    const [displayName, setDisplayName] = useState("");
    const [email, setEmail] = useState("");
    const [mobile, setMobile] = useState("");
    const [employeeNo, setEmployeeNo] = useState("");
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
        setEmployeeNo("");
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
            if (employeeNo.trim()) {
                body.employeeNo = employeeNo.trim();
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
                throw new Error(envelope.message ?? `创建失败（HTTP ${response.status}）`);
            }
            onCreated?.(envelope.data ?? {});
        } catch (err) {
            setError(err instanceof Error ? err.message : "创建失败");
        } finally {
            setSubmitting(false);
        }
    }, [
        canSubmitDirect,
        confirmPassword,
        contactType,
        displayName,
        email,
        employeeNo,
        loginName,
        mobile,
        onCreated,
        password,
        roleCodes,
    ]);

    return (
        <div className="flex flex-col gap-4">
            <div className="flex flex-col gap-2">
                <span className="text-sm font-medium">开通方式 *</span>
                <div className="flex flex-wrap gap-4 text-sm">
                    <label className="flex cursor-pointer items-center gap-2">
                        <input
                            type="radio"
                            name="provisionMode"
                            checked={mode === "invite"}
                            onChange={() => resetModeFields("invite")}
                        />
                        发送邀请（推荐）
                    </label>
                    <label className="flex cursor-pointer items-center gap-2">
                        <input
                            type="radio"
                            name="provisionMode"
                            checked={mode === "direct"}
                            onChange={() => resetModeFields("direct")}
                        />
                        直接创建账号（实施权限）
                    </label>
                </div>
            </div>

            <div className="rounded-md border border-border p-4">
                <p className="mb-3 text-sm font-medium">基本信息</p>
                <div className="grid gap-3 md:grid-cols-2">
                    <label className="flex flex-col gap-1 text-sm">
                        登录名 {mode === "direct" ? "*" : ""}
                        <Input
                            value={loginName}
                            onChange={(value) => setLoginName(String(value))}
                            placeholder={mode === "direct" ? "2–64 字符，创建后不可改" : "可选，接受邀请时可修改"}
                        />
                    </label>
                    <label className="flex flex-col gap-1 text-sm">
                        显示名称
                        <Input
                            value={displayName}
                            onChange={(value) => setDisplayName(String(value))}
                            placeholder="在控制台展示的名称"
                        />
                    </label>
                </div>
                <div className="mt-3 flex flex-col gap-2">
                    <span className="text-sm font-medium">联系方式 *（二选一）</span>
                    <div className="flex gap-4 text-sm">
                        <label className="flex cursor-pointer items-center gap-2">
                            <input
                                type="radio"
                                checked={contactType === "email"}
                                onChange={() => setContactType("email")}
                            />
                            邮箱
                        </label>
                        <label className="flex cursor-pointer items-center gap-2">
                            <input
                                type="radio"
                                checked={contactType === "mobile"}
                                onChange={() => setContactType("mobile")}
                            />
                            手机号码
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
                            placeholder="11 位手机号"
                        />
                    )}
                </div>
                {mode === "direct" && (
                    <label className="mt-3 flex flex-col gap-1 text-sm">
                        员工编号（可选）
                        <Input
                            value={employeeNo}
                            onChange={(value) => setEmployeeNo(String(value))}
                            placeholder="如 OPS-001"
                        />
                    </label>
                )}
            </div>

            {mode === "invite" && (
                <div className="rounded-md border border-dashed border-amber-500/50 bg-amber-500/5 p-4 text-sm text-muted-foreground">
                    <p className="font-medium text-foreground">邀请开通</p>
                    <p className="mt-1">
                        POST /platform/invites 尚未就绪。请切换为「直接创建账号」完成开通，或等待邀请 API 发布后在本页提交邀请单。
                    </p>
                </div>
            )}

            {mode === "direct" && (
                <div className="rounded-md border border-border p-4">
                    <p className="mb-3 text-sm font-medium">账号与密码</p>
                    <div className="grid gap-3 md:grid-cols-2">
                        <label className="flex flex-col gap-1 text-sm">
                            初始密码 *
                            <Input
                                type="password"
                                value={password}
                                onChange={(value) => setPassword(String(value))}
                            />
                        </label>
                        <label className="flex flex-col gap-1 text-sm">
                            确认密码 *
                            <Input
                                type="password"
                                value={confirmPassword}
                                onChange={(value) => setConfirmPassword(String(value))}
                            />
                        </label>
                    </div>
                    <label className="mt-3 flex flex-col gap-1 text-sm">
                        默认角色（可选，逗号分隔角色编码）
                        <Input
                            value={roleCodes}
                            onChange={(value) => setRoleCodes(String(value))}
                            placeholder="如 PLATFORM_OPERATOR"
                        />
                    </label>
                </div>
            )}

            {error && <p className="text-sm text-destructive">{error}</p>}

            <div className="flex justify-end gap-2">
                <Button onClick={() => onCancel?.()}>
                    取消
                </Button>
                {mode === "direct" && (
                    <Button
                        variant="primary"
                        disabled={submitting || !canSubmitDirect}
                        onClick={() => void submitDirect()}
                    >
                        {submitting ? "提交中…" : "提交"}
                    </Button>
                )}
            </div>
        </div>
    );
}
