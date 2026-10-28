"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";

import { Button, Input } from "@pactor-app/ui/components";
import { useI18n } from "@pactor-app/runtime";

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
}, translate: (source: string) => string): Promise<void> {
    const response = await fetch("/api/auth/platform-login", {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
    });
    const payload = (await response.json()) as NexusEnvelope<unknown>;
    if (!response.ok || !isLoginSuccess(payload)) {
        throw new Error(
            payload.message
                ? payload.message
                : `${translate("common.ui.sign-in-failed")} (HTTP ${response.status})`,
        );
    }
}

function AuthLinks() {
    const { t } = useI18n();
    const [policy, setPolicy] = useState<PublicRegistrationPolicy | null>(null);
    useEffect(() => {
        void fetchPublicRegistrationPolicy()
            .then(setPolicy)
            .catch(() => setPolicy(null));
    }, []);
    return (
        <div className="mt-4 flex flex-wrap justify-center gap-x-3 gap-y-1 text-sm">
            <Link className="text-primary hover:underline" href="/password/reset">
                {t("common.ui.forgot-password")}
            </Link>
            {policy?.openRegistrationEnabled ? (
                <Link className="text-primary hover:underline" href="/register/open">
                    {t("common.ui.open-registration")}
                </Link>
            ) : null}
            {policy?.approvalRegistrationEnabled ? (
                <Link className="text-primary hover:underline" href="/register/approval">
                    {t("common.ui.register-pending-approval")}
                </Link>
            ) : null}
            {policy?.inviteRegistrationEnabled ? (
                <Link className="text-primary hover:underline" href="/register/invite">
                    {t("common.ui.invitation-code-registration")}
                </Link>
            ) : null}
        </div>
    );
}

export function PlatformLoginForm() {
    const { t } = useI18n();
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
    }, [captcha]);

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
            }, t);
            reloadAfterAuth("/");
            return;
        } catch (err) {
            const message = err instanceof Error ? err.message : "common.ui.sign-in-failed";
            if (message.includes("PENDING_APPROVAL") || message.includes("待审核")) {
                router.push(`/pending-approval?loginName=${encodeURIComponent(login.trim())}`);
                return;
            }
            setError(message);
            await refreshCaptcha();
        } finally {
            setLoading(false);
        }
    }, [captcha, captchaCode, login, password, refreshCaptcha, router, showCaptcha, t]);

    const captchaSrc = useMemo(
        () => resolveCaptchaImageSrc(captcha?.imageBase64, captcha?.imageMimeType),
        [captcha?.imageBase64, captcha?.imageMimeType],
    );

    return (
        <AuthPageShell
            title={t("common.ui.sign-in-to-operations-platform")}
            subtitle={t("common.ui.sign-in-with-your-platform-account")}
        >
            <div className="flex flex-col gap-3">
                <label className="text-sm">
                    {t("common.ui.login-name")}
                    <Input className="mt-1" value={login} onChange={(v) => setLogin(String(v))} />
                </label>
                <label className="text-sm">
                    {t("common.ui.password")}
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
                            {t("common.ui.verification-code")}
                            <Input
                                className="mt-1"
                                value={captchaCode}
                                placeholder={t("common.ui.enter-the-verification-code")}
                                onChange={(v) => setCaptchaCode(String(v))}
                            />
                        </label>
                        <div className="flex w-fit max-w-full items-center gap-2 self-start">
                            {/* 验证码是运行时 data URL，无法交给 next/image 做构建期优化。 */}
                            {/* eslint-disable-next-line @next/next/no-img-element */}
                            <img
                                src={captchaSrc}
                                alt={t("common.ui.verification-code")}
                                className="block h-12 w-auto shrink-0 rounded border object-contain"
                            />
                            <Button size="small" onClick={() => void refreshCaptcha()}>
                                {t("common.ui.refresh")}
                            </Button>
                        </div>
                    </div>
                )}
                {error && <p className="text-sm text-destructive">{t(error)}</p>}
                <Button variant="primary" disabled={loading} onClick={() => void submit()}>
                    {loading ? t("common.ui.signing-in") : t("common.ui.sign-in")}
                </Button>
                <AuthLinks />
            </div>
        </AuthPageShell>
    );
}

export function PlatformPasswordResetForm() {
    const { t } = useI18n();
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
            setError("common.ui.passwords-do-not-match");
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
            setError(err instanceof Error ? err.message : "common.ui.reset-failed");
        } finally {
            setLoading(false);
        }
    }, [confirm, identity, password, router, type, verificationCode]);

    return (
        <AuthPageShell title={t("common.ui.reset-password")} subtitle={t("common.ui.verify-your-identity-then-set-a-new-password")}>
            <div className="flex flex-col gap-3">
                <label className="text-sm">
                    {t("common.ui.login-name-email-mobile")}
                    <Input className="mt-1" value={identity} onChange={(v) => setIdentity(String(v))} />
                </label>
                <div className="flex gap-4 text-sm">
                    <label className="flex items-center gap-2">
                        <input type="radio" checked={type === "EMAIL"} onChange={() => setType("EMAIL")} />
                        {t("common.ui.email-verification")}
                    </label>
                    <label className="flex items-center gap-2">
                        <input type="radio" checked={type === "MOBILE"} onChange={() => setType("MOBILE")} />
                        {t("common.ui.sms-verification")}
                    </label>
                </div>
                <label className="text-sm">
                    {t("common.ui.verification-code")}
                    <Input
                        className="mt-1"
                        value={verificationCode}
                        onChange={(v) => setVerificationCode(String(v))}
                    />
                </label>
                <label className="text-sm">
                    {t("common.ui.new-password")}
                    <Input
                        className="mt-1"
                        type="password"
                        value={password}
                        onChange={(v) => setPassword(String(v))}
                    />
                </label>
                <label className="text-sm">
                    {t("common.ui.confirm-password")}
                    <Input
                        className="mt-1"
                        type="password"
                        value={confirm}
                        onChange={(v) => setConfirm(String(v))}
                    />
                </label>
                {error && <p className="text-sm text-destructive">{t(error)}</p>}
                <div className="flex gap-2">
                    <Button variant="primary" disabled={loading} onClick={() => void submit()}>
                        {t("common.ui.confirm-reset")}
                    </Button>
                    <Link href="/login" className="text-sm leading-9 text-primary hover:underline">
                        {t("common.ui.back-to-sign-in")}
                    </Link>
                </div>
            </div>
        </AuthPageShell>
    );
}

export function PlatformPasswordChangeForm() {
    const { t } = useI18n();
    const [oldPassword, setOldPassword] = useState("");
    const [password, setPassword] = useState("");
    const [confirm, setConfirm] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const submit = useCallback(async () => {
        if (password !== confirm) {
            setError("common.ui.new-passwords-do-not-match");
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
                    throw new Error(body.message ?? "common.ui.change-failed");
                }
            });
            setError(null);
            setOldPassword("");
            setPassword("");
            setConfirm("");
        } catch (err) {
            setError(err instanceof Error ? err.message : "common.ui.change-failed");
        } finally {
            setLoading(false);
        }
    }, [confirm, oldPassword, password]);

    return (
        <div className="max-w-lg">
            <h2 className="text-lg font-semibold">{t("common.ui.change-password")}</h2>
            <p className="mt-1 text-sm text-muted-foreground">POST /api/d/platform/auth/password/change</p>
            <div className="mt-4 flex flex-col gap-3">
                <label className="text-sm">
                    {t("common.ui.current-password")}
                    <Input
                        className="mt-1"
                        type="password"
                        value={oldPassword}
                        onChange={(v) => setOldPassword(String(v))}
                    />
                </label>
                <label className="text-sm">
                    {t("common.ui.new-password")}
                    <Input
                        className="mt-1"
                        type="password"
                        value={password}
                        onChange={(v) => setPassword(String(v))}
                    />
                </label>
                <label className="text-sm">
                    {t("common.ui.confirm-new-password")}
                    <Input
                        className="mt-1"
                        type="password"
                        value={confirm}
                        onChange={(v) => setConfirm(String(v))}
                    />
                </label>
                {error && <p className="text-sm text-destructive">{t(error)}</p>}
                <Button variant="primary" disabled={loading} onClick={() => void submit()}>
                    {t("common.ui.save")}
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
    const { t } = useI18n();
    return (
        <AuthPageShell
            title={t("common.ui.registration-submitted-pending-approval")}
            subtitle={loginName
                ? t("common.ui.your-account-loginname", { loginName })
                : undefined}
        >
            <p className="text-sm text-muted-foreground">
                {t("common.ui.you-will-be-notified-after-an-administrator-approves-the-registration-th")}
            </p>
            <Link href="/login" className="mt-4 inline-block text-sm text-primary hover:underline">
                {t("common.ui.back-to-sign-in")}
            </Link>
        </AuthPageShell>
    );
}

export function PlatformInviteAcceptForm({ token }: { token?: string }) {
    const { t } = useI18n();
    const i18n = useI18n();
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
        await postJson(`/public/invites/${encodeURIComponent(token)}/otp`, {
            locale: i18n.locale,
        });
    }, [i18n.locale, token]);

    const submit = useCallback(async () => {
        if (!token) {
            setError("common.ui.invalid-invitation-link");
            return;
        }
        if (password !== confirm) {
            setError("common.ui.passwords-do-not-match");
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
                locale: i18n.locale,
            });
            router.push("/login");
        } catch (err) {
            setError(err instanceof Error ? err.message : "common.ui.provisioning-failed");
        } finally {
            setLoading(false);
        }
    }, [confirm, i18n.locale, loginName, password, router, token, verificationCode]);

    const contactHint =
        preview?.maskedEmail
        ?? preview?.maskedMobile
        ?? (token ? t("common.ui.loading-invitation") : t("common.ui.invitation-token-is-missing"));

    return (
        <AuthPageShell
            title={t("common.ui.accept-invitation")}
            subtitle={t("common.ui.invitation-for-contact", { contact: contactHint })}
        >
            <div className="flex flex-col gap-3">
                <label className="text-sm">
                    {t("common.ui.login-name-optional")}
                    <Input className="mt-1" value={loginName} onChange={(v) => setLoginName(String(v))} />
                </label>
                <label className="text-sm">
                    {t("common.ui.verification-code")}
                    <Input
                        className="mt-1"
                        value={verificationCode}
                        onChange={(v) => setVerificationCode(String(v))}
                    />
                </label>
                <Button size="small" onClick={() => void issueOtp()}>
                    {t("common.ui.get-code")}
                </Button>
                <label className="text-sm">
                    {t("common.ui.password")}
                    <Input
                        className="mt-1"
                        type="password"
                        value={password}
                        onChange={(v) => setPassword(String(v))}
                    />
                </label>
                <label className="text-sm">
                    {t("common.ui.confirm-password")}
                    <Input
                        className="mt-1"
                        type="password"
                        value={confirm}
                        onChange={(v) => setConfirm(String(v))}
                    />
                </label>
                {error && <p className="text-sm text-destructive">{t(error)}</p>}
                <Button variant="primary" disabled={loading} onClick={() => void submit()}>
                    {t("common.ui.complete-provisioning")}
                </Button>
                <Link href="/login" className="text-sm text-primary hover:underline">
                    {t("common.ui.back-to-sign-in")}
                </Link>
            </div>
        </AuthPageShell>
    );
}

export function PlatformInviteActivateForm() {
    const { t } = useI18n();
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
            setError("common.ui.passwords-do-not-match");
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
            setError(err instanceof Error ? err.message : "common.ui.registration-failed");
        } finally {
            setLoading(false);
        }
    }, [confirm, contact, inviteCode, loginName, password, router]);

    return (
        <AuthPageShell
            title={t("common.ui.invitation-code-registration")}
            subtitle={t("common.ui.enter-the-invitation-code-and-reserved-contact-provided-by-the-administr")}
        >
            <div className="flex flex-col gap-3">
                <label className="text-sm">
                    {t("common.ui.invitation-code")}
                    <Input className="mt-1" value={inviteCode} onChange={(v) => setInviteCode(String(v))} />
                </label>
                <label className="text-sm">
                    {t("common.ui.email-or-mobile")}
                    <Input className="mt-1" value={contact} onChange={(v) => setContact(String(v))} />
                </label>
                <label className="text-sm">
                    {t("common.ui.login-name")}
                    <Input className="mt-1" value={loginName} onChange={(v) => setLoginName(String(v))} />
                </label>
                <label className="text-sm">
                    {t("common.ui.password-2")}
                    <Input
                        className="mt-1"
                        type="password"
                        value={password}
                        onChange={(v) => setPassword(String(v))}
                    />
                </label>
                <label className="text-sm">
                    {t("common.ui.confirm-password-2")}
                    <Input
                        className="mt-1"
                        type="password"
                        value={confirm}
                        onChange={(v) => setConfirm(String(v))}
                    />
                </label>
                {error && <p className="text-sm text-destructive">{t(error)}</p>}
                <Button variant="primary" disabled={loading} onClick={() => void submit()}>
                    {t("common.ui.complete-registration")}
                </Button>
                <Link href="/login" className="text-sm text-primary hover:underline">
                    {t("common.ui.back-to-sign-in")}
                </Link>
            </div>
        </AuthPageShell>
    );
}

function ContactRegistrationForm({
    mode,
}: {
    mode: "open" | "approval";
}) {
    const { t } = useI18n();
    const i18n = useI18n();
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
                ? { email: email.trim(), locale: i18n.locale }
                : { mobile: mobile.trim(), locale: i18n.locale };
        await postJson(otpPath, body);
    }, [email, fixedContactType, i18n.locale, mobile, otpPath, otpRequired]);

    const submit = useCallback(async () => {
        if (password !== confirm) {
            setError("common.ui.passwords-do-not-match");
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
                locale: i18n.locale,
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
            setError(err instanceof Error ? err.message : "common.ui.submission-failed");
        } finally {
            setLoading(false);
        }
    }, [
        confirm,
        description,
        displayName,
        email,
        fixedContactType,
        i18n.locale,
        loginName,
        mobile,
        mode,
        otpRequired,
        password,
        router,
        submitPath,
        verificationCode,
    ]);

    const title = mode === "open"
        ? "common.ui.create-an-operations-platform-account"
        : "common.ui.register-pending-approval";
    const subtitleSource =
        mode === "open"
            ? "common.ui.complete-the-form-and-verify-your-contact-details-to-sign-in-and-use-the"
            : "common.ui.an-administrator-must-approve-the-submission-before-you-can-sign-in";

    return (
        <AuthPageShell title={t(title)} subtitle={t(subtitleSource)}>
            <div className="flex flex-col gap-3">
                <label className="text-sm">
                    {t("common.ui.login-name-2")}
                    <Input className="mt-1" value={loginName} onChange={(v) => setLoginName(String(v))} />
                </label>
                <label className="text-sm">
                    {t("common.ui.display-name")}
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
                            {t("common.ui.email")}
                        </label>
                        <label className="flex items-center gap-2">
                            <input
                                type="radio"
                                checked={contactType === "mobile"}
                                onChange={() => setContactType("mobile")}
                            />
                            {t("common.ui.mobile")}
                        </label>
                    </div>
                ) : null}
                {otpRequired && fixedContactType === "email" ? (
                    <label className="text-sm">
                        {t("common.ui.email-2")}
                        <Input className="mt-1" value={email} onChange={(v) => setEmail(String(v))} />
                    </label>
                ) : null}
                {otpRequired && fixedContactType === "mobile" ? (
                    <label className="text-sm">
                        {t("common.ui.mobile-2")}
                        <Input className="mt-1" value={mobile} onChange={(v) => setMobile(String(v))} />
                    </label>
                ) : null}
                {mode === "approval" && (
                    <label className="text-sm">
                        {t("common.ui.application-notes")}
                        <Input
                            className="mt-1"
                            value={description}
                            onChange={(v) => setDescription(String(v))}
                        />
                    </label>
                )}
                <label className="text-sm">
                    {t("common.ui.password-2")}
                    <Input
                        className="mt-1"
                        type="password"
                        value={password}
                        onChange={(v) => setPassword(String(v))}
                    />
                </label>
                <label className="text-sm">
                    {t("common.ui.confirm-password-2")}
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
                            {t("common.ui.verification-code")}
                            <Input
                                className="mt-1"
                                value={verificationCode}
                                onChange={(v) => setVerificationCode(String(v))}
                            />
                        </label>
                        <Button size="small" onClick={() => void issueOtp()}>
                            {t("common.ui.get-code")}
                        </Button>
                    </>
                ) : null}
                {error && <p className="text-sm text-destructive">{t(error)}</p>}
                <Button variant="primary" disabled={loading} onClick={() => void submit()}>
                    {mode === "open" ? t("common.ui.complete-registration") : t("common.ui.submit-registration")}
                </Button>
                <Link href="/login" className="text-sm text-primary hover:underline">
                    {t("common.ui.back-to-sign-in")}
                </Link>
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
