"use client";

import { useMemo } from "react";

import { Button } from "@pactor-app/ui/components";

import { useI18n } from "@pactor-app/runtime";

import { DictionaryTypeSection } from "./dictionary-type-section";
import { resolveI18nText } from "./i18n-text";

type TypeVo = {
    dictionaryTypeId?: string;
    typeCode?: string;
    typeName?: string | Record<string, string>;
    status?: string;
    sortOrder?: number | null;
    builtIn?: boolean;
    createdAt?: string;
    updatedAt?: string;
};

type ItemVo = {
    itemValue?: string;
    itemName?: string;
    status?: string;
    sortOrder?: number | null;
};

const LOCALE_LABELS: Record<string, string> = {
    "zh-CN": "common.ui.simplified-chinese",
    en: "English",
    ja: "common.ui.japanese",
    ko: "한국어",
    fr: "Français",
    de: "Deutsch",
};

function localeLabel(tag: string, translate: (source: string) => string): string {
    const label = LOCALE_LABELS[tag];
    return label === undefined ? tag : translate(label);
}

function formatTypeNameEntries(typeName: TypeVo["typeName"]): { tag: string; text: string }[] {
    if (typeName == null) {
        return [];
    }
    if (typeof typeName === "string") {
        const trimmed = typeName.trim();
        if (trimmed.length === 0) {
            return [];
        }
        return [{ tag: "zh-CN", text: trimmed }];
    }
    const entries: { tag: string; text: string }[] = [];
    for (const [tag, text] of Object.entries(typeName)) {
        const trimmed = text?.trim() ?? "";
        if (trimmed.length > 0) {
            entries.push({ tag, text: trimmed });
        }
    }
    const order = ["zh-CN", "en"];
    entries.sort((a, b) => {
        const ai = order.indexOf(a.tag);
        const bi = order.indexOf(b.tag);
        if (ai === -1 && bi === -1) {
            return a.tag.localeCompare(b.tag);
        }
        if (ai === -1) {
            return 1;
        }
        if (bi === -1) {
            return -1;
        }
        return ai - bi;
    });
    return entries;
}

function statusKey(status: string | undefined): string {
    if (status === "ENABLED") {
        return "common.ui.enabled";
    }
    if (status === "DISABLED") {
        return "common.ui.disabled";
    }
    return status ?? "—";
}

function formatDateTime(value: string | undefined): string {
    if (value == null || value.trim().length === 0) {
        return "—";
    }
    return value.replace("T", " ").slice(0, 19);
}

export interface DictionaryTypeViewPanelProps {
    variant?: "drawer" | "page";
    loading?: boolean;
    typeData?: TypeVo | null;
    itemsData?: ItemVo[] | null;
    onClose?: () => void;
}

/**
 * 字典类型只读侧滑：基本信息、字典项、扩展信息分区展示。
 */
export function DictionaryTypeViewPanel({
    variant = "drawer",
    loading = false,
    typeData,
    itemsData,
    onClose,
}: DictionaryTypeViewPanelProps) {
    const i18n = useI18n();
    const { t } = i18n;
    const isPage = variant === "page";
    const typeNameEntries = useMemo(() => formatTypeNameEntries(typeData?.typeName), [typeData?.typeName]);
    const items = useMemo(() => {
        if (!Array.isArray(itemsData)) {
            return [];
        }
        return itemsData.filter((row) => row != null);
    }, [itemsData]);

    const displayTitle = resolveI18nText(typeData?.typeName, i18n.locale, i18n.defaultLocale)
        || typeData?.typeCode || "—";

    if (loading) {
        return <p className="text-sm text-muted-foreground">{t("common.ui.loading")}</p>;
    }

    const rootClass = isPage ? "flex flex-col" : "flex min-h-[calc(100dvh-8.5rem)] flex-col";
    const footerClass = isPage
        ? "mt-6 flex justify-end gap-2 border-t border-border pt-4"
        : "sticky bottom-0 z-10 -mx-6 mt-4 shrink-0 border-t border-border bg-popover px-6 py-3 shadow-[0_-6px_16px_-8px_rgba(0,0,0,0.12)]";

    return (
        <div className={rootClass}>
            <div className={`min-h-0 flex-1 space-y-4 ${isPage ? "" : "overflow-y-auto pr-1"}`}>
                <DictionaryTypeSection title={t("common.ui.basic-information")}>
                    <dl className="grid gap-3 text-sm sm:grid-cols-[7rem_1fr]">
                        <dt className="text-muted-foreground">{t("common.ui.type-name")}</dt>
                        <dd className="font-medium text-foreground">{displayTitle}</dd>
                        <dt className="text-muted-foreground">{t("common.ui.type-code")}</dt>
                        <dd className="font-mono text-xs">{typeData?.typeCode ?? "—"}</dd>
                        <dt className="text-muted-foreground">{t("common.ui.status")}</dt>
                        <dd>{t(statusKey(typeData?.status))}</dd>
                    </dl>
                    {typeNameEntries.length > 0 ? (
                        <div className="mt-4 border-t border-border/60 pt-3">
                            <div className="mb-2 text-xs font-medium text-muted-foreground">
                                {t("common.ui.localized-names")}
                            </div>
                            <dl className="space-y-2 text-sm">
                                {typeNameEntries.map((entry) => (
                                    <div key={entry.tag} className="flex gap-2">
                                        <span className="w-24 shrink-0 text-muted-foreground">
                                            {localeLabel(entry.tag, t)}
                                        </span>
                                        <span>{entry.text}</span>
                                    </div>
                                ))}
                            </dl>
                        </div>
                    ) : null}
                </DictionaryTypeSection>

                <DictionaryTypeSection title={t("common.ui.dictionary-items")}>
                    {items.length === 0 ? (
                        <p className="text-sm text-muted-foreground">{t("common.ui.no-dictionary-items")}</p>
                    ) : (
                        <table className="w-full text-sm">
                            <thead>
                                <tr className="border-b border-border text-left text-muted-foreground">
                                    <th className="py-2 pr-2 font-medium">{t("common.ui.value")}</th>
                                    <th className="py-2 pr-2 font-medium">{t("common.ui.name")}</th>
                                    <th className="py-2 pr-2 font-medium">{t("common.ui.status")}</th>
                                    <th className="py-2 font-medium">{t("common.ui.order")}</th>
                                </tr>
                            </thead>
                            <tbody>
                                {items.map((row, index) => (
                                    <tr key={`${row.itemValue ?? index}`} className="border-b border-border/60">
                                        <td className="py-2 pr-2 font-mono text-xs">{row.itemValue ?? "—"}</td>
                                        <td className="py-2 pr-2">{row.itemName ?? "—"}</td>
                                        <td className="py-2 pr-2">{t(statusKey(row.status))}</td>
                                        <td className="py-2">{row.sortOrder ?? "—"}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </DictionaryTypeSection>

                <DictionaryTypeSection title={t("common.ui.additional-information")}>
                    <dl className="grid gap-3 text-sm sm:grid-cols-[7rem_1fr]">
                        <dt className="text-muted-foreground">{t("common.ui.dictionary-id")}</dt>
                        <dd className="font-mono text-xs">{typeData?.dictionaryTypeId ?? "—"}</dd>
                        <dt className="text-muted-foreground">{t("common.ui.system-built-in")}</dt>
                        <dd>
                            {typeData?.builtIn === true
                                ? t("common.ui.yes")
                                : typeData?.builtIn === false
                                  ? t("common.ui.no")
                                  : "—"}
                        </dd>
                        <dt className="text-muted-foreground">{t("common.ui.order")}</dt>
                        <dd>{typeData?.sortOrder ?? "—"}</dd>
                        <dt className="text-muted-foreground">{t("common.ui.created-at")}</dt>
                        <dd>{formatDateTime(typeData?.createdAt)}</dd>
                        <dt className="text-muted-foreground">{t("common.ui.updated-at")}</dt>
                        <dd>{formatDateTime(typeData?.updatedAt)}</dd>
                    </dl>
                </DictionaryTypeSection>
            </div>
            {onClose != null ? (
                <div className={footerClass}>
                    <div className="flex justify-end gap-2">
                        <Button variant="primary" onClick={() => onClose()}>
                            {t("common.ui.close")}
                        </Button>
                    </div>
                </div>
            ) : null}
        </div>
    );
}
