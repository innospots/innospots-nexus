"use client";

import { useEffect, useState } from "react";

import { Button } from "@pactor-app/ui/components";

import { logoutAndRedirect } from "../../lib/auth-session";
import { SettingsPage, SettingsSection } from "./settings-layout";

interface SessionPayload {
  authenticated?: boolean;
  user?: { userName?: string; displayName?: string };
}

export function PortalProfileSettings() {
  const [session, setSession] = useState<SessionPayload | null>(null);
  const [loggingOut, setLoggingOut] = useState(false);

  useEffect(() => {
    void fetch("/api/auth/session", {
      cache: "no-store",
      credentials: "include",
    })
      .then((response) => response.json() as Promise<SessionPayload>)
      .then(setSession)
      .catch(() => setSession({ authenticated: false }));
  }, []);

  const userName = session?.user?.userName ?? "—";
  const displayName = session?.user?.displayName ?? "—";

  return (
    <SettingsPage title="个人设置" description="查看账号信息与管理当前会话">
      <SettingsSection title="账号信息">
        <dl className="portal-descriptions">
          <div className="portal-descriptions-row">
            <dt>登录名</dt>
            <dd>{userName}</dd>
          </div>
          <div className="portal-descriptions-row">
            <dt>显示名</dt>
            <dd>{displayName}</dd>
          </div>
          <div className="portal-descriptions-row">
            <dt>会话状态</dt>
            <dd>{session?.authenticated ? "已登录" : "未登录"}</dd>
          </div>
        </dl>
      </SettingsSection>

      <SettingsSection
        title="安全"
        hint="修改密码与绑定邮箱将于后续版本开放。"
      >
        <dl className="portal-descriptions">
          <div className="portal-descriptions-row">
            <dt>登录密码</dt>
            <dd className="portal-text-muted">已设置</dd>
          </div>
          <div className="portal-descriptions-row">
            <dt>邮箱绑定</dt>
            <dd className="portal-text-muted">未绑定</dd>
          </div>
        </dl>
      </SettingsSection>

      <SettingsSection title="会话" hint="退出后需要重新登录才能进入工作台。">
        <Button
          variant="default"
          loading={loggingOut}
          disabled={loggingOut}
          onClick={() => {
            setLoggingOut(true);
            void logoutAndRedirect();
          }}
        >
          退出登录
        </Button>
      </SettingsSection>
    </SettingsPage>
  );
}
