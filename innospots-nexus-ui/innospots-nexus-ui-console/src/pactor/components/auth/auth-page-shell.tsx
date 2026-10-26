"use client";

import type { ReactNode } from "react";

import { useUiText } from "@/i18n/ui";

export interface AuthPageShellProps {
    title: string;
    subtitle?: string;
    children?: ReactNode;
}

export function AuthPageShell({ title, subtitle, children }: AuthPageShellProps) {
    const t = useUiText();
    return (
        <div className="flex min-h-[80vh] flex-col items-center justify-center px-4 py-10">
            <div className="mb-6 text-center">
                <div className="text-lg font-semibold tracking-tight">
                    {t("Innospots 运营管理平台")}
                </div>
                <div className="text-sm text-muted-foreground">Nexus Console</div>
            </div>
            <div className="w-full max-w-md rounded-xl border border-border bg-card p-6 shadow-sm">
                <h1 className="text-xl font-semibold">{title}</h1>
                {subtitle && <p className="mt-1 text-sm text-muted-foreground">{subtitle}</p>}
                <div className="mt-6">{children}</div>
            </div>
        </div>
    );
}
