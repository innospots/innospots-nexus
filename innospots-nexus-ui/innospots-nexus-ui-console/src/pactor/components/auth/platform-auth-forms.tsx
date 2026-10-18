"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";

import { Button, Input } from "@pactor-app/ui/components";

import { reloadAfterAuth } from "../../lib/auth-session";
import { resolveCaptchaImageSrc } from "../../lib/captcha-image-src";
import { encryptPasswordForApi } from "../../lib/rsa-password";
import { platformPublicFetch } from "../../lib/platform-public-api";
import {
    fetchPublicRegistrationPolicy,
    type PublicRegistrationPolicy,
} from "../../lib/registration-policy";
import { AuthPageShell } from "./auth-page-shell";

type CaptchaVo = {
    clientKey?: string;
    imageBase64?: string;
    imageMimeType?: string;
};

type InvitePreview = {
    maskedEmail?: string;
    maskedMobile?: string;
    loginName?: string;
    expiresAt?: string;
    status?: string;
};

async function postJson<T>(path: string, body: unknown): Promise<T> {
    const suffix = path.startsWith("/") ? path : `/${path}`;
    return platformPublicFetch<T>(suffix, {
        method: "POST",
        body: JSON.stringify(body),
    });
}

type NexusEnvelope<T> = {
    success?: boolean;
    code?: string | number;
    message?: string;
    data?: T;
};

function isLoginSuccess(envelope: NexusEnvelope<unknown>): boolean {
    if (envelope.success === false) {
        return false;
    }
    if (envelope.success === true) {
        return true;
    }
    const code = envelope.code;
    if (code === undefined) {
        return true;
    }
    return code === 0 || code === "0" || code === "OK";
}

/** 经 Next BFF 登录，写入 httpOnly 会话供 sitemap / 数据转发鉴权。 */
async function platformLogin(body: {
    login: string;
    encryptedPassword: string;
    captchaClientKey: string;
    captchaCode: string;
}): Promise<void> {
    const response = await fetch("/api/auth/platform-login", {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
    });
    const payload = (await response.json()) as NexusEnvelope<unknown>;
    if (!response.ok || !isLoginSuccess(payload)) {
        throw new Error(payload.message ?? `登录失败（HTTP ${response.status}）`);
    }
}

function AuthLinks() {
    const [policy, setPolicy] = useState<PublicRegistrationPolicy | null>(null);
    useEffect(() => {
        void fetchPublicRegistrationPolicy()
            .then(setPolicy)
            .catch(() => setPolicy(null));
    }, []);
    return (
        <div className="mt-4 flex flex-wrap justify-center gap-x-3 gap-y-1 text-sm">
            <Link className="text-primary hover:underline" href="/password/reset">忘记密码？</Link>
            {policy?.openRegistrationEnabled ? (
                <Link className="text-primary hover:underline" href="/register/open">开放注册</Link>
            ) : null}
            {policy?.approvalRegistrationEnabled ? (
                <Link className="text-primary hover:underline" href="/register/approval">注册（待审核）</Link>
            ) : null}
            {policy?.inviteRegistrationEnabled ? (
                <Link className="text-primary hover:underline" href="/register/invite">邀请码注册</Link>
            ) : null}
        </div>
    );
}

export function PlatformLoginForm() {
    const router = useRouter();
    const [login, setLogin] = useState("");
    const [password, setPassword] = useState("");
    const [captchaCode, setCaptchaCode] = useState("");
    const [captcha, setCaptcha] = useState<CaptchaVo | null>(null);
    const [showCaptcha, setShowCaptcha] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const refreshCaptcha = useCallback(async () => {
        try {
            const data = await postJson<CaptchaVo>("/public/auth/captcha/issue", {
                clientKey: captcha?.clientKey ?? "",
            });
            setCaptcha(data);
            setCaptchaCode("");
            setShowCaptcha(true);
        } catch {
            setShowCaptcha(false);
        }
    }, [captcha?.clientKey]);

    useEffect(() => {
        void (async () => {
            try {
                const data = await postJson<CaptchaVo>("/public/auth/captcha/issue", {
                    clientKey: "",
                });
                setCaptcha(data);
                setShowCaptcha(true);
            } catch {
                setShowCaptcha(false);
            }
        })();
    }, []);

    const submit = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const encryptedPassword = await encryptPasswordForApi(password);
            await platformLogin({
                login: login.trim(),
                encryptedPassword,
                captchaClientKey: captcha?.clientKey ?? "",
                captchaCode: showCaptcha ? captchaCode : "",
            });
            reloadAfterAuth("/");
            return;
        } catch (err) {
            const message = err instanceof Error ? err.message : "登录失败";
            if (message.includes("PENDING_APPROVAL") || message.includes("待审核")) {
                router.push(`/pending-approval?loginName=${encodeURIComponent(login.trim())}`);
                return;
            }
            setError(message);
            await refreshCaptcha();
        } finally {
            setLoading(false);
        }
    }, [captcha?.clientKey, captchaCode, login, password, refreshCaptcha, router, showCaptcha]);

    const captchaSrc = useMemo(
        () => resolveCaptchaImageSrc(captcha?.imageBase64, captcha?.imageMimeType),
        [captcha?.imageBase64, captcha?.imageMimeType],
    );

    return (
        <AuthPageShell title="登录到运营管理平台" subtitle="使用平台账号登录">
            <div className="flex flex-col gap-3">
                <label className="text-sm">
                    登录名
                    <Input className="mt-1" value={login} onChange={(v) => setLogin(String(v))} />
                </label>
                <label className="text-sm">
                    密码
                    <Input
                        className="mt-1"
                        type="password"
                        value={password}
                        onChange={(v) => setPassword(String(v))}
                    />
                </label>
                {showCaptcha && captchaSrc && (
                    <div className="flex flex-col gap-2">
                        <label className="text-sm">
                            验证码
                            <Input
                                className="mt-1"
                                value={captchaCode}
                                placeholder="请输入验证码"
                                onChange={(v) => setCaptchaCode(String(v))}
                            />
                        </label>
                        <div className="flex w-fit max-w-full items-center gap-2 self-start">
                            <img
                                src={captchaSrc}
                                alt="验证码"
                                className="block h-12 w-auto shrink-0 rounded border object-contain"
                            />
                            <Button size="small" onClick={() => void refreshCaptcha()}>换一张</Button>
                        </div>
                    </div>
                )}
                {error && <p className="text-sm text-destructive">{error}</p>}
                <Button variant="primary" disabled={loading} onClick={() => void submit()}>
                    {loading ? "登录中…" : "登 录"}
                </Button>
                <AuthLinks />
            </div>
        </AuthPageShell>
    );
}

export function PlatformPasswordResetForm() {
    const router = useRouter();
    const [identity, setIdentity] = useState("");
    const [type, setType] = useState<"EMAIL" | "MOBILE">("EMAIL");
    const [verificationCode, setVerificationCode] = useState("");
    const [password, setPassword] = useState("");
    const [confirm, setConfirm] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const submit = useCallback(async () => {
        if (password !== confirm) {
            setError("两次密码不一致");
            return;
        }
        setLoading(true);
        setError(null);
        try {
            const newEncryptedPassword = await encryptPasswordForApi(password);
            await postJson("/public/auth/password/reset", {
                identity: identity.trim(),
                verificationCode: verificationCode.trim(),
                type,
                newEncryptedPassword,
            });
            router.push("/login");
        } catch (err) {
            setError(err instanceof Error ? err.message : "重置失败");
        } finally {
            setLoading(false);
        }
    }, [confirm, identity, password, router, type, verificationCode]);

    return (
        <AuthPageShell title="重置密码" subtitle="验证身份后设置新密码">
            <div className="flex flex-col gap-3">
                <label className="text-sm">
                    登录名 / 邮箱 / 手机
                    <Input className="mt-1" value={identity} onChange={(v) => setIdentity(String(v))} />
                </label>
                <div className="flex gap-4 text-sm">
                    <label className="flex items-center gap-2">
                        <input type="radio" checked={type === "EMAIL"} onChange={() => setType("EMAIL")} />
                        邮箱验证
                    </label>
                    <label className="flex items-center gap-2">
                        <input type="radio" checked={type === "MOBILE"} onChange={() => setType("MOBILE")} />
                        短信验证
                    </label>
                </div>
                <label className="text-sm">
                    验证码
                    <Input
                        className="mt-1"
                        value={verificationCode}
                        onChange={(v) => setVerificationCode(String(v))}
                    />
                </label>
                <label className="text-sm">
                    新密码
                    <Input
                        className="mt-1"
                        type="password"
                        value={password}
                        onChange={(v) => setPassword(String(v))}
                    />
                </label>
                <label className="text-sm">
                    确认密码
                    <Input
                        className="mt-1"
                        type="password"
                        value={confirm}
                        onChange={(v) => setConfirm(String(v))}
                    />
                </label>
                {error && <p className="text-sm text-destructive">{error}</p>}
                <div className="flex gap-2">
                    <Button variant="primary" disabled={loading} onClick={() => void submit()}>
                        确认重置
                    </Button>
                    <Link href="/login" className="text-sm leading-9 text-primary hover:underline">
                        返回登录
                    </Link>
                </div>
            </div>
        </AuthPageShell>
    );
}

export function PlatformPasswordChangeForm() {
    const [oldPassword, setOldPassword] = useState("");
    const [password, setPassword] = useState("");
    const [confirm, setConfirm] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const submit = useCallback(async () => {
        if (password !== confirm) {
            setError("两次新密码不一致");
            return;
        }
        setLoading(true);
        setError(null);
        try {
            const oldEncryptedPassword = await encryptPasswordForApi(oldPassword);
            const newEncryptedPassword = await encryptPasswordForApi(password);
            await fetch("/api/d/platform/auth/password/change", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ oldEncryptedPassword, newEncryptedPassword }),
            }).then(async (res) => {
                if (!res.ok) {
                    const body = (await res.json()) as { message?: string };
                    throw new Error(body.message ?? "修改失败");
                }
            });
            setError(null);
            setOldPassword("");
            setPassword("");
            setConfirm("");
        } catch (err) {
            setError(err instanceof Error ? err.message : "修改失败");
        } finally {
            setLoading(false);
        }
    }, [confirm, oldPassword, password]);

    return (
        <div className="max-w-lg">
            <h2 className="text-lg font-semibold">修改密码</h2>
            <p className="mt-1 text-sm text-muted-foreground">POST /api/d/platform/auth/password/change</p>
            <div className="mt-4 flex flex-col gap-3">
                <label className="text-sm">
                    当前密码
                    <Input
                        className="mt-1"
                        type="password"
                        value={oldPassword}
                        onChange={(v) => setOldPassword(String(v))}
                    />
                </label>
                <label className="text-sm">
                    新密码
                    <Input
                        className="mt-1"
                        type="password"
                        value={password}
                        onChange={(v) => setPassword(String(v))}
                    />
                </label>
                <label className="text-sm">
                    确认新密码
                    <Input
                        className="mt-1"
                        type="password"
                        value={confirm}
                        onChange={(v) => setConfirm(String(v))}
                    />
                </label>
                {error && <p className="text-sm text-destructive">{error}</p>}
                <Button variant="primary" disabled={loading} onClick={() => void submit()}>
                    保存
                </Button>
            </div>
        </div>
    );
}

export function PlatformPendingApprovalPage() {
    const search = useSearchParams();
    return <PlatformPendingApprovalView loginName={search.get("loginName") ?? undefined} />;
}

export function PlatformPendingApprovalView({ loginName }: { loginName?: string }) {
    return (
        <AuthPageShell title="注册已提交，等待审核" subtitle={loginName ? `您的账号：${loginName}` : undefined}>
            <p className="text-sm text-muted-foreground">
                管理员审核通过后，您将收到通知，届时可使用注册时的登录名与密码登录。审核前请勿重复注册。
            </p>
            <Link href="/login" className="mt-4 inline-block text-sm text-primary hover:underline">
                返回登录
            </Link>
        </AuthPageShell>
    );
}

export function PlatformInviteAcceptForm({ token }: { token?: string }) {
    const router = useRouter();
    const [preview, setPreview] = useState<InvitePreview | null>(null);
    const [loginName, setLoginName] = useState("");
    const [password, setPassword] = useState("");
    const [confirm, setConfirm] = useState("");
    const [verificationCode, setVerificationCode] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!token) {
            return;
        }
        void platformPublicFetch<InvitePreview>(`/public/invites/${encodeURIComponent(token)}`)
            .then((data) => {
                setPreview(data);
                if (data.loginName) {
                    setLoginName(data.loginName);
                }
            })
            .catch(() => setPreview(null));
    }, [token]);

    const issueOtp = useCallback(async () => {
        if (!token) {
            return;
        }
        await postJson(`/public/invites/${encodeURIComponent(token)}/otp`, { locale: "zh-CN" });
    }, [token]);

    const submit = useCallback(async () => {
        if (!token) {
            setError("邀请链接无效");
            return;
        }
        if (password !== confirm) {
            setError("两次密码不一致");
            return;
        }
        setLoading(true);
        setError(null);
        try {
            const encryptedPassword = await encryptPasswordForApi(password);
            await postJson(`/public/invites/${encodeURIComponent(token)}/accept`, {
                loginName: loginName.trim() || undefined,
                encryptedPassword,
                verificationCode: verificationCode.trim() || undefined,
                locale: "zh-CN",
            });
            router.push("/login");
        } catch (err) {
            setError(err instanceof Error ? err.message : "开通失败");
        } finally {
            setLoading(false);
        }
    }, [confirm, loginName, password, router, token, verificationCode]);

    const contactHint =
        preview?.maskedEmail ?? preview?.maskedMobile ?? (token ? "加载邀请信息…" : "缺少邀请令牌");

    return (
        <AuthPageShell title="接受邀请" subtitle={`邀请至：${contactHint}`}>
            <div className="flex flex-col gap-3">
                <label className="text-sm">
                    登录名（可选）
                    <Input className="mt-1" value={loginName} onChange={(v) => setLoginName(String(v))} />
                </label>
                <label className="text-sm">
                    验证码
                    <Input
                        className="mt-1"
                        value={verificationCode}
                        onChange={(v) => setVerificationCode(String(v))}
                    />
                </label>
                <Button size="small" onClick={() => void issueOtp()}>获取验证码</Button>
                <label className="text-sm">
                    密码
                    <Input
                        className="mt-1"
                        type="password"
                        value={password}
                        onChange={(v) => setPassword(String(v))}
                    />
                </label>
                <label className="text-sm">
                    确认密码
                    <Input
                        className="mt-1"
                        type="password"
                        value={confirm}
                        onChange={(v) => setConfirm(String(v))}
                    />
                </label>
                {error && <p className="text-sm text-destructive">{error}</p>}
                <Button variant="primary" disabled={loading} onClick={() => void submit()}>
                    完成开通
                </Button>
                <Link href="/login" className="text-sm text-primary hover:underline">返回登录</Link>
            </div>
        </AuthPageShell>
    );
}

export function PlatformInviteActivateForm() {
    const router = useRouter();
    useEffect(() => {
        void fetchPublicRegistrationPolicy()
            .then((loaded) => {
                if (!loaded.inviteRegistrationEnabled) {
                    router.replace("/login");
                }
            })
            .catch(() => router.replace("/login"));
    }, [router]);
    const [inviteCode, setInviteCode] = useState("");
    const [contact, setContact] = useState("");
    const [loginName, setLoginName] = useState("");
    const [password, setPassword] = useState("");
    const [confirm, setConfirm] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const submit = useCallback(async () => {
        if (password !== confirm) {
            setError("两次密码不一致");
            return;
        }
        setLoading(true);
        setError(null);
        try {
            const encryptedPassword = await encryptPasswordForApi(password);
            await postJson("/public/invites/activate-by-code", {
                inviteCode: inviteCode.trim(),
                contact: contact.trim(),
                loginName: loginName.trim() || undefined,
                encryptedPassword,
            });
            router.push("/login");
        } catch (err) {
            setError(err instanceof Error ? err.message : "注册失败");
        } finally {
            setLoading(false);
        }
    }, [confirm, contact, inviteCode, loginName, password, router]);

    return (
        <AuthPageShell title="邀请码注册" subtitle="请输入管理员提供的邀请码与预留联系方式">
            <div className="flex flex-col gap-3">
                <label className="text-sm">
                    邀请码 *
                    <Input className="mt-1" value={inviteCode} onChange={(v) => setInviteCode(String(v))} />
                </label>
                <label className="text-sm">
                    邮箱或手机 *
                    <Input className="mt-1" value={contact} onChange={(v) => setContact(String(v))} />
                </label>
                <label className="text-sm">
                    登录名
                    <Input className="mt-1" value={loginName} onChange={(v) => setLoginName(String(v))} />
                </label>
                <label className="text-sm">
                    密码 *
                    <Input
                        className="mt-1"
                        type="password"
                        value={password}
                        onChange={(v) => setPassword(String(v))}
                    />
                </label>
                <label className="text-sm">
                    确认密码 *
                    <Input
                        className="mt-1"
                        type="password"
                        value={confirm}
                        onChange={(v) => setConfirm(String(v))}
                    />
                </label>
                {error && <p className="text-sm text-destructive">{error}</p>}
                <Button variant="primary" disabled={loading} onClick={() => void submit()}>
                    完成注册
                </Button>
                <Link href="/login" className="text-sm text-primary hover:underline">返回登录</Link>
            </div>
        </AuthPageShell>
    );
}

function ContactRegistrationForm({
    mode,
}: {
    mode: "open" | "approval";
}) {
    const router = useRouter();
    const [policy, setPolicy] = useState<PublicRegistrationPolicy | null>(null);
    const [contactType, setContactType] = useState<"email" | "mobile">("email");
    const [loginName, setLoginName] = useState("");
    const [displayName, setDisplayName] = useState("");
    const [email, setEmail] = useState("");
    const [mobile, setMobile] = useState("");
    const [description, setDescription] = useState("");
    const [password, setPassword] = useState("");
    const [confirm, setConfirm] = useState("");
    const [verificationCode, setVerificationCode] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const otpPath =
        mode === "open"
            ? "/public/registration/open/otp"
            : "/public/registration/access-requests/otp";
    const submitPath =
        mode === "open" ? "/public/registration/open" : "/public/registration/access-requests";

    const otpRequired = policy?.otpVerification !== "NONE";
    const fixedContactType =
        policy?.otpVerification === "EMAIL"
            ? "email"
            : policy?.otpVerification === "MOBILE"
              ? "mobile"
              : contactType;

    useEffect(() => {
        void fetchPublicRegistrationPolicy()
            .then((loaded) => {
                setPolicy(loaded);
                const allowed =
                    mode === "open"
                        ? loaded.openRegistrationEnabled
                        : loaded.approvalRegistrationEnabled;
                if (!allowed) {
                    router.replace("/login");
                }
                if (loaded.otpVerification === "MOBILE") {
                    setContactType("mobile");
                } else if (loaded.otpVerification === "EMAIL") {
                    setContactType("email");
                }
            })
            .catch(() => router.replace("/login"));
    }, [mode, router]);

    const issueOtp = useCallback(async () => {
        if (!otpRequired) {
            return;
        }
        const body =
            fixedContactType === "email"
                ? { email: email.trim(), locale: "zh-CN" }
                : { mobile: mobile.trim(), locale: "zh-CN" };
        await postJson(otpPath, body);
    }, [email, fixedContactType, mobile, otpPath, otpRequired]);

    const submit = useCallback(async () => {
        if (password !== confirm) {
            setError("两次密码不一致");
            return;
        }
        setLoading(true);
        setError(null);
        try {
            const encryptedPassword = await encryptPasswordForApi(password);
            const payload: Record<string, string> = {
                loginName: loginName.trim(),
                displayName: displayName.trim(),
                encryptedPassword,
                locale: "zh-CN",
            };
            if (otpRequired) {
                payload.verificationCode = verificationCode.trim();
            }
            if (otpRequired && fixedContactType === "email") {
                payload.email = email.trim();
            } else if (otpRequired && fixedContactType === "mobile") {
                payload.mobile = mobile.trim();
            }
            if (mode === "approval" && description.trim()) {
                payload.description = description.trim();
            }
            await postJson(submitPath, payload);
            if (mode === "approval") {
                router.push(`/pending-approval?loginName=${encodeURIComponent(loginName.trim())}`);
            } else {
                router.push("/login");
            }
        } catch (err) {
            setError(err instanceof Error ? err.message : "提交失败");
        } finally {
            setLoading(false);
        }
    }, [
        confirm,
        description,
        displayName,
        email,
        fixedContactType,
        loginName,
        mobile,
        mode,
        otpRequired,
        password,
        router,
        submitPath,
        verificationCode,
    ]);

    const title = mode === "open" ? "注册运营管理平台账号" : "注册（待审核）";
    const subtitle =
        mode === "open"
            ? "填写信息并验证联系方式后即可登录使用。"
            : "提交后需管理员审核，通过后方可登录。";

    return (
        <AuthPageShell title={title} subtitle={subtitle}>
            <div className="flex flex-col gap-3">
                <label className="text-sm">
                    登录名 *
                    <Input className="mt-1" value={loginName} onChange={(v) => setLoginName(String(v))} />
                </label>
                <label className="text-sm">
                    显示名称
                    <Input className="mt-1" value={displayName} onChange={(v) => setDisplayName(String(v))} />
                </label>
                {otpRequired && policy?.otpVerification !== "EMAIL" && policy?.otpVerification !== "MOBILE" ? (
                    <div className="flex gap-4 text-sm">
                        <label className="flex items-center gap-2">
                            <input
                                type="radio"
                                checked={contactType === "email"}
                                onChange={() => setContactType("email")}
                            />
                            邮箱
                        </label>
                        <label className="flex items-center gap-2">
                            <input
                                type="radio"
                                checked={contactType === "mobile"}
                                onChange={() => setContactType("mobile")}
                            />
                            手机
                        </label>
                    </div>
                ) : null}
                {otpRequired && fixedContactType === "email" ? (
                    <label className="text-sm">
                        邮箱 *
                        <Input className="mt-1" value={email} onChange={(v) => setEmail(String(v))} />
                    </label>
                ) : null}
                {otpRequired && fixedContactType === "mobile" ? (
                    <label className="text-sm">
                        手机 *
                        <Input className="mt-1" value={mobile} onChange={(v) => setMobile(String(v))} />
                    </label>
                ) : null}
                {mode === "approval" && (
                    <label className="text-sm">
                        申请说明
                        <Input
                            className="mt-1"
                            value={description}
                            onChange={(v) => setDescription(String(v))}
                        />
                    </label>
                )}
                <label className="text-sm">
                    密码 *
                    <Input
                        className="mt-1"
                        type="password"
                        value={password}
                        onChange={(v) => setPassword(String(v))}
                    />
                </label>
                <label className="text-sm">
                    确认密码 *
                    <Input
                        className="mt-1"
                        type="password"
                        value={confirm}
                        onChange={(v) => setConfirm(String(v))}
                    />
                </label>
                {otpRequired ? (
                    <>
                        <label className="text-sm">
                            验证码
                            <Input
                                className="mt-1"
                                value={verificationCode}
                                onChange={(v) => setVerificationCode(String(v))}
                            />
                        </label>
                        <Button size="small" onClick={() => void issueOtp()}>获取验证码</Button>
                    </>
                ) : null}
                {error && <p className="text-sm text-destructive">{error}</p>}
                <Button variant="primary" disabled={loading} onClick={() => void submit()}>
                    {mode === "open" ? "完成注册" : "提交注册"}
                </Button>
                <Link href="/login" className="text-sm text-primary hover:underline">返回登录</Link>
            </div>
        </AuthPageShell>
    );
}

export function PlatformOpenRegistrationForm() {
    return <ContactRegistrationForm mode="open" />;
}

export function PlatformAccessRegistrationForm() {
    return <ContactRegistrationForm mode="approval" />;
}
