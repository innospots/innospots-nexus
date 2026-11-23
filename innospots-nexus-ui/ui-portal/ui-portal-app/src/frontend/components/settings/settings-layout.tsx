"use client";

import type { ReactNode } from "react";

/** 设置页统一头部与分区容器。 */
export function SettingsPage({
  title,
  description,
  children,
}: {
  title: string;
  description: string;
  children: ReactNode;
}) {
  return (
    <div className="portal-page">
      <header className="portal-page-header">
        <h1 className="portal-page-title">{title}</h1>
        <p className="portal-page-subtitle">{description}</p>
      </header>
      {children}
    </div>
  );
}

export function SettingsSection({
  title,
  hint,
  children,
}: {
  title: string;
  hint?: string;
  children: ReactNode;
}) {
  return (
    <section className="portal-card">
      <h2 className="portal-card-title">{title}</h2>
      {hint ? <p className="portal-card-hint">{hint}</p> : null}
      {children}
    </section>
  );
}
