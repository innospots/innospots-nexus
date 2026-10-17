"use client";

import type { ReactNode } from "react";

export interface DictionaryTypeSectionProps {
    title: string;
    children: ReactNode;
    className?: string;
}

/** 字典侧滑层分区：标题 + 圆角边框内容区。 */
export function DictionaryTypeSection({ title, children, className }: DictionaryTypeSectionProps) {
    return (
        <section className={className ?? "rounded-lg border border-border bg-muted/20 p-4"}>
            <h3 className="mb-3 text-sm font-semibold text-foreground">{title}</h3>
            {children}
        </section>
    );
}
