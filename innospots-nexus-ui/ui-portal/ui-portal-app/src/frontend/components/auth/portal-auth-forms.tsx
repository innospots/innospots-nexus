"use client";

import Link from "next/link";
import type { FormEvent, ReactNode } from "react";
import { useState } from "react";

import { Button, Input } from "@pactor-app/ui/components";

import { encryptPasswordForApi } from "../../lib/rsa-password";

type AuthEnvelope = {
  success?: boolean;
  code?: string | number;
  message?: string;
};

function isSuccess(envelope: AuthEnvelope): boolean {
  if (envelope.success === false) {
    return false;
  }
  const code = envelope.code;
  if (code === undefined) {
    return true;
  }
  return code === 0 || code === "0" || code === "OK";
}

/** 登录/注册成功后回到模块根路径（兼容 /login 与 /portal/login）。 */
function redirectAfterAuth(): void {
  const path = window.location.pathname.replace(/\/(?:login|register)\/?$/, "");
  window.location.assign(path.length > 0 ? path : "/");
}

async function postAuth(
  path: string,
  body: Record<string, unknown>,
): Promise<AuthEnvelope & { ok: boolean }> {
  const response = await fetch(path, {
    method: "POST",
    credentials: "include",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(body),
  });
  const payload = (await response.json().catch(() => ({}))) as AuthEnvelope;
  return {
    ...payload,
    ok: response.ok && isSuccess(payload),
  };
}

function AuthFrame({
  title,
  subtitle,
  children,
  footer,
}: {
  title: string;
  subtitle?: string;
  children: ReactNode;
  footer?: ReactNode;
}) {
  return (
    <div className="portal-auth-page">
      <div className="portal-auth-brand">
        <span className="portal-auth-mark">N</span>
        <span className="portal-auth-brand-name">Nexus Portal</span>
      </div>
      <div className="portal-auth-card">
        <h1 className="portal-auth-title">{title}</h1>
        {subtitle ? <p className="portal-auth-subtitle">{subtitle}</p> : null}
        <div className="portal-auth-body">{children}</div>
        {footer ? <div className="portal-auth-footer">{footer}</div> : null}
      </div>
    </div>
  );
}

function Field({
  label,
  htmlFor,
  hint,
  children,
}: {
  label: string;
  htmlFor: string;
  hint?: string;
  children: ReactNode;
}) {
  return (
    <div className="portal-field">
      <label className="portal-field-label" htmlFor={htmlFor}>
        {label}
      </label>
      {children}
      {hint ? <span className="portal-field-hint">{hint}</span> : null}
    </div>
  );
}

export function PortalLoginForm() {
  const [login, setLogin] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function submit() {
    setError(null);
    if (!login.trim() || !password) {
      setError("请填写登录名与密码");
      return;
    }
    setLoading(true);
    try {
      const encryptedPassword = await encryptPasswordForApi(password);
      const result = await postAuth("/api/auth/login", {
        login: login.trim(),
        encryptedPassword,
      });
      if (!result.ok) {
        setError(result.message ?? "登录失败，请检查登录名与密码");
        return;
      }
      redirectAfterAuth();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "登录失败");
    } finally {
      setLoading(false);
    }
  }

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    void submit();
  }

  return (
    <AuthFrame
      title="登录"
      subtitle="使用租户账号进入 Nexus 用户门户"
      footer={
        <span>
          还没有账号？
          <Link className="portal-auth-link" href="/register">
            立即注册
          </Link>
        </span>
      }
    >
      <form className="portal-form" onSubmit={onSubmit}>
        <Field label="登录名" htmlFor="portal-login-name">
          <Input
            id="portal-login-name"
            value={login}
            placeholder="用户名 / 邮箱 / 手机号"
            onChange={setLogin}
          />
        </Field>
        <Field label="密码" htmlFor="portal-login-password">
          <Input
            id="portal-login-password"
            type="password"
            value={password}
            placeholder="请输入密码"
            onChange={setPassword}
          />
        </Field>
        {error ? <p className="portal-form-error">{error}</p> : null}
        <Button
          variant="primary"
          loading={loading}
          disabled={loading}
          onClick={() => void submit()}
        >
          登录
        </Button>
      </form>
    </AuthFrame>
  );
}

export function PortalRegisterForm() {
  const [userName, setUserName] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function submit() {
    setError(null);
    if (userName.trim().length < 2) {
      setError("登录名至少需要 2 个字符");
      return;
    }
    if (password.length < 6) {
      setError("密码至少需要 6 个字符");
      return;
    }
    if (password !== confirm) {
      setError("两次输入的密码不一致");
      return;
    }
    setLoading(true);
    try {
      const encryptedPassword = await encryptPasswordForApi(password);
      const result = await postAuth("/api/registration/register", {
        userName: userName.trim(),
        displayName: displayName.trim() || userName.trim(),
        email: email.trim() || undefined,
        encryptedPassword,
      });
      if (!result.ok) {
        setError(result.message ?? "注册失败，请稍后重试");
        return;
      }
      redirectAfterAuth();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "注册失败");
    } finally {
      setLoading(false);
    }
  }

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    void submit();
  }

  return (
    <AuthFrame
      title="注册"
      subtitle="创建租户身份，注册成功后自动登录"
      footer={
        <span>
          已有账号？
          <Link className="portal-auth-link" href="/login">
            返回登录
          </Link>
        </span>
      }
    >
      <form className="portal-form" onSubmit={onSubmit}>
        <Field label="登录名" htmlFor="portal-register-name" hint="登录名唯一，注册后不可修改">
          <Input
            id="portal-register-name"
            value={userName}
            placeholder="例如 zhangsan"
            onChange={setUserName}
          />
        </Field>
        <Field label="显示名" htmlFor="portal-register-display">
          <Input
            id="portal-register-display"
            value={displayName}
            placeholder="展示在页面上的名称，可留空"
            onChange={setDisplayName}
          />
        </Field>
        <Field label="邮箱" htmlFor="portal-register-email" hint="选填，可用于后续找回密码">
          <Input
            id="portal-register-email"
            value={email}
            placeholder="name@example.com"
            onChange={setEmail}
          />
        </Field>
        <Field label="密码" htmlFor="portal-register-password">
          <Input
            id="portal-register-password"
            type="password"
            value={password}
            placeholder="至少 6 个字符"
            onChange={setPassword}
          />
        </Field>
        <Field label="确认密码" htmlFor="portal-register-confirm">
          <Input
            id="portal-register-confirm"
            type="password"
            value={confirm}
            placeholder="再次输入密码"
            onChange={setConfirm}
          />
        </Field>
        {error ? <p className="portal-form-error">{error}</p> : null}
        <Button
          variant="primary"
          loading={loading}
          disabled={loading}
          onClick={() => void submit()}
        >
          注册并登录
        </Button>
      </form>
    </AuthFrame>
  );
}
