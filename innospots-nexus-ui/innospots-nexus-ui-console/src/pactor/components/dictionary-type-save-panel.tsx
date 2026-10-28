"use client";

import { useCallback, useEffect, useMemo, useState } from "react";

import { Trash2 } from "lucide-react";

import { Button, Input, Select } from "@pactor-app/ui/components";

import { useI18n } from "@pactor-app/runtime";

import { registerDictionaryTypeSaveSubmit } from "../lib/dictionary-type-save-registry";
import { DictionaryTypeSection } from "./dictionary-type-section";

export type DictionaryItemDraft = {
  itemValue: string;
  itemName: string;
};

type TypeVo = {
  typeCode?: string;
  typeName?: string | Record<string, string>;
};

type ItemVo = {
  itemValue?: string;
  itemName?: string;
};

const LOCALE_ZH_CN = "zh-CN";
const LOCALE_EN = "en";

const LOCALE_LABELS: Record<string, string> = {
  [LOCALE_ZH_CN]: "common.ui.simplified-chinese",
  [LOCALE_EN]: "English",
  ja: "common.ui.japanese",
  ko: "한국어",
  fr: "Français",
  de: "Deutsch",
};

const DEFAULT_LOCALE_TAGS = [LOCALE_ZH_CN, LOCALE_EN];

const ADDABLE_LOCALES = ["ja", "ko", "fr", "de", "es", "pt-BR", "zh-TW"];

/** 简体中文不可删除；其余语言行可移除。 */
const REQUIRED_LOCALE_TAGS = new Set<string>([LOCALE_ZH_CN]);

const ALL_KNOWN_LOCALE_TAGS = [LOCALE_ZH_CN, LOCALE_EN, ...ADDABLE_LOCALES];

export interface DictionaryTypeSavePanelProps {
  mode?: "create" | "edit";
  variant?: "drawer" | "page";
  /** 为 true 时不渲染底部按钮，由外层 Drawer / overlay 的 okText + onOk 触发保存 */
  hideFooter?: boolean;
  typeData?: TypeVo | null;
  itemsData?: ItemVo[] | null;
  onSave?: (payload: {
    mode: "create" | "edit";
    typeCode: string;
    typeName: Record<string, string>;
    items: DictionaryItemDraft[];
  }) => void;
  onCancel?: () => void;
}

function toDraftItems(rows: ItemVo[] | null | undefined): DictionaryItemDraft[] {
  if (!Array.isArray(rows)) {
    return [];
  }
  const mapped: DictionaryItemDraft[] = [];
  for (const row of rows) {
    if (row == null) {
      continue;
    }
    const itemValue = row.itemValue?.trim() ?? "";
    const itemName = row.itemName?.trim() ?? "";
    if (itemValue.length === 0 && itemName.length === 0) {
      continue;
    }
    mapped.push({
      itemValue,
      itemName,
    });
  }
  return mapped;
}

function parseTypeNameLocales(value: TypeVo["typeName"]): { tags: string[]; values: Record<string, string> } {
  const values: Record<string, string> = {
    [LOCALE_ZH_CN]: "",
    [LOCALE_EN]: "",
  };
  if (value == null) {
    return { tags: [...DEFAULT_LOCALE_TAGS], values };
  }
  if (typeof value === "string") {
    values[LOCALE_ZH_CN] = value;
    return { tags: [...DEFAULT_LOCALE_TAGS], values };
  }
  const tags = new Set<string>(DEFAULT_LOCALE_TAGS);
  for (const [tag, text] of Object.entries(value)) {
    if (text == null) {
      continue;
    }
    const trimmed = String(text).trim();
    if (trimmed.length === 0) {
      continue;
    }
    tags.add(tag);
    values[tag] = trimmed;
  }
  const ordered = [...DEFAULT_LOCALE_TAGS];
  for (const tag of tags) {
    if (!ordered.includes(tag)) {
      ordered.push(tag);
    }
  }
  for (const tag of ordered) {
    if (values[tag] == null) {
      values[tag] = value[tag]?.trim() ?? "";
    }
  }
  return { tags: ordered, values };
}

function localeLabel(tag: string, translate: (source: string) => string): string {
  const label = LOCALE_LABELS[tag];
  return label === undefined ? tag : translate(label);
}

/**
 * 字典类型侧滑层：基本信息（多语言类型名称）+ 字典项。
 */
export function DictionaryTypeSavePanel({
  mode = "create",
  variant = "drawer",
  hideFooter = false,
  typeData,
  itemsData,
  onSave,
  onCancel,
}: DictionaryTypeSavePanelProps) {
  const { t } = useI18n();
  const isPage = variant === "page";
  const isCreate = mode === "create";
  const [typeCode, setTypeCode] = useState("");
  const [localeTags, setLocaleTags] = useState<string[]>([...DEFAULT_LOCALE_TAGS]);
  const [typeNameByLocale, setTypeNameByLocale] = useState<Record<string, string>>({
    [LOCALE_ZH_CN]: "",
    [LOCALE_EN]: "",
  });
  const [items, setItems] = useState<DictionaryItemDraft[]>([]);
  const [draftValue, setDraftValue] = useState("");
  const [draftName, setDraftName] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [localeToAdd, setLocaleToAdd] = useState("");

  const typeKey = useMemo(
    () => `${mode}:${typeData?.typeCode ?? ""}:${JSON.stringify(typeData?.typeName ?? "")}`,
    [mode, typeData?.typeCode, typeData?.typeName],
  );

  useEffect(() => {
    if (isCreate) {
      // 创建态的重置需在 props 切换后同步派生表单状态。
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setTypeCode("");
      setLocaleTags([...DEFAULT_LOCALE_TAGS]);
      setTypeNameByLocale({
        [LOCALE_ZH_CN]: "",
        [LOCALE_EN]: "",
      });
      setItems([]);
      setDraftValue("");
      setDraftName("");
      setError(null);
      return;
    }
    const parsed = parseTypeNameLocales(typeData?.typeName);
    setTypeCode(typeData?.typeCode ?? "");
    setLocaleTags(parsed.tags);
    setTypeNameByLocale(parsed.values);
    setItems(toDraftItems(itemsData));
    setDraftValue("");
    setDraftName("");
    setError(null);
  }, [isCreate, typeKey, itemsData, typeData?.typeCode, typeData?.typeName]);

  const addableLocales = useMemo(() => {
    return ALL_KNOWN_LOCALE_TAGS.filter((tag) => !localeTags.includes(tag));
  }, [localeTags]);

  useEffect(() => {
    if (addableLocales.length === 0) {
      // 可选语言随当前语言列表派生，清理失效选择。
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setLocaleToAdd("");
      return;
    }
    if (!addableLocales.includes(localeToAdd)) {
      setLocaleToAdd(addableLocales[0]);
    }
  }, [addableLocales, localeToAdd]);

  const addLocale = useCallback(() => {
    const tag = localeToAdd.trim();
    if (tag.length === 0 || localeTags.includes(tag)) {
      return;
    }
    setLocaleTags((prev) => [...prev, tag]);
    setTypeNameByLocale((prev) => ({ ...prev, [tag]: prev[tag] ?? "" }));
  }, [localeTags, localeToAdd]);

  const removeLocale = useCallback((tag: string) => {
    if (REQUIRED_LOCALE_TAGS.has(tag)) {
      return;
    }
    setLocaleTags((prev) => prev.filter((item) => item !== tag));
    setTypeNameByLocale((prev) => {
      const next = { ...prev };
      delete next[tag];
      return next;
    });
  }, []);

  const setLocaleValue = useCallback((tag: string, text: string) => {
    setTypeNameByLocale((prev) => ({ ...prev, [tag]: text }));
  }, []);

  const addItem = useCallback(() => {
    const value = draftValue.trim();
    const name = draftName.trim();
    if (value.length === 0 || name.length === 0) {
      setError("common.ui.dictionary-item-value-and-name-are-required");
      return;
    }
    if (items.some((row) => row.itemValue === value)) {
      setError("common.ui.dictionary-item-values-must-be-unique-within-a-type");
      return;
    }
    setItems((prev) => [{ itemValue: value, itemName: name }, ...prev]);
    setDraftValue("");
    setDraftName("");
    setError(null);
  }, [draftName, draftValue, items]);

  const removeItem = useCallback((index: number) => {
    setItems((prev) => prev.filter((_, i) => i !== index));
  }, []);

  const buildSavePayload = useCallback((): {
    mode: "create" | "edit";
    typeCode: string;
    typeName: Record<string, string>;
    items: DictionaryItemDraft[];
  } | null => {
    const code = typeCode.trim();
    const zhName = typeNameByLocale[LOCALE_ZH_CN]?.trim() ?? "";
    if (code.length === 0 || zhName.length === 0) {
      setError("common.ui.type-code-and-simplified-chinese-type-name-are-required");
      return null;
    }
    if (items.length === 0) {
      setError("common.ui.configure-at-least-one-dictionary-item");
      return null;
    }
    const typeName: Record<string, string> = {};
    for (const tag of localeTags) {
      const trimmed = typeNameByLocale[tag]?.trim() ?? "";
      if (trimmed.length > 0) {
        typeName[tag] = trimmed;
      }
    }
    typeName[LOCALE_ZH_CN] = zhName;
    setError(null);
    return {
      mode: isCreate ? "create" : "edit",
      typeCode: code,
      typeName,
      items,
    };
  }, [isCreate, items, localeTags, typeCode, typeNameByLocale]);

  useEffect(() => {
    if (!hideFooter) {
      registerDictionaryTypeSaveSubmit(null);
      return;
    }
    registerDictionaryTypeSaveSubmit(buildSavePayload);
    return () => {
      registerDictionaryTypeSaveSubmit(null);
    };
  }, [buildSavePayload, hideFooter]);

  const handleSave = useCallback(() => {
    const payload = buildSavePayload();
    if (payload == null) {
      return;
    }
    onSave?.(payload);
  }, [buildSavePayload, onSave]);

  const rootClass = isPage ? "flex flex-col" : "flex min-h-[calc(100dvh-8.5rem)] flex-col";
  const footerClass = isPage
      ? "mt-6 flex justify-end gap-2 border-t border-border pt-4"
      : "sticky bottom-0 z-10 -mx-6 mt-4 shrink-0 border-t border-border bg-popover px-6 py-3 shadow-[0_-6px_16px_-8px_rgba(0,0,0,0.12)]";

  return (
    <div className={rootClass}>
      <div className={`min-h-0 flex-1 space-y-4 ${isPage ? "" : "overflow-y-auto pr-1"}`}>
        <DictionaryTypeSection title={t("common.ui.basic-information")}>
          <div className="space-y-3">
            <label className="block text-sm">
              <span className="text-destructive">*</span> {t("common.ui.type-code")}
              <Input
                className="mt-1"
                value={typeCode}
                disabled={!isCreate}
                onChange={(event) => setTypeCode(String(event))}
                placeholder={t("common.ui.e-g-user-status-2")}
              />
            </label>
            <div className="block text-sm">
              <div className="mb-2">
                <span className="text-destructive">*</span> {t("common.ui.type-name")}
              </div>
              <div className="space-y-2">
                {localeTags.map((tag) => (
                  <div key={tag} className="flex items-center gap-2 text-sm">
                    <span className="w-24 shrink-0 text-muted-foreground">
                      {localeLabel(tag, t)}
                    </span>
                    <Input
                      className="min-w-0 flex-1"
                      value={typeNameByLocale[tag] ?? ""}
                      onChange={(event) => setLocaleValue(tag, String(event))}
                      placeholder={tag === LOCALE_ZH_CN ? t("common.ui.e-g-user-status") : "User status"}
                    />
                    {REQUIRED_LOCALE_TAGS.has(tag) ? (
                      <span className="size-8 shrink-0" aria-hidden="true" />
                    ) : (
                      <button
                        type="button"
                        className="inline-flex size-8 shrink-0 items-center justify-center rounded-md text-muted-foreground transition-colors hover:bg-muted hover:text-destructive"
                        aria-label={t("common.ui.delete-locale", { locale: localeLabel(tag, t) })}
                        onClick={() => removeLocale(tag)}
                      >
                        <Trash2 className="size-4" aria-hidden="true" />
                      </button>
                    )}
                  </div>
                ))}
              </div>
              {addableLocales.length > 0 ? (
                <div className="mt-2 flex flex-wrap items-center gap-2">
                  <Select
                    className="mb-0 w-[11rem]"
                    controlClassName="w-[11rem]"
                    placeholder={t("common.ui.select-language")}
                    value={localeToAdd}
                    allowClear={false}
                    showSearch={addableLocales.length > 6}
                    options={addableLocales.map((tag) => ({
                      label: localeLabel(tag, t),
                      value: tag,
                    }))}
                    onChange={(value) => setLocaleToAdd(String(value))}
                  />
                  <Button
                    variant="link"
                    size="small"
                    className="h-auto px-0"
                    disabled={localeToAdd.length === 0}
                    onClick={addLocale}
                  >
                    {t("common.ui.add-language")}
                  </Button>
                </div>
              ) : null}
            </div>
            {isCreate ? (
              <p className="text-xs text-muted-foreground">
                {t("common.ui.new-operations-platform-dictionaries-use-the-platform-security-domain-an")}
              </p>
            ) : null}
          </div>
        </DictionaryTypeSection>
        <DictionaryTypeSection title={t("common.ui.dictionary-items")}>
          <div className="mb-3 space-y-2 rounded-md border border-dashed border-border bg-background/50 p-3">
            <div className="text-xs font-medium text-muted-foreground">
              {t("common.ui.add-dictionary-item-2")}
            </div>
            <div className="grid gap-2 sm:grid-cols-2">
              <Input
                value={draftValue}
                onChange={(event) => setDraftValue(String(event))}
                placeholder={t("common.ui.value-2")}
              />
              <Input
                value={draftName}
                onChange={(event) => setDraftName(String(event))}
                placeholder={t("common.ui.name-2")}
              />
            </div>
            <Button variant="default" size="small" onClick={addItem}>
              {t("common.ui.add-dictionary-item")}
            </Button>
          </div>
          {items.length === 0 ? (
            <p className="text-sm text-muted-foreground">{t("common.ui.no-dictionary-items")}</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border text-left text-muted-foreground">
                  <th className="py-2 pr-2 font-medium">{t("common.ui.value")}</th>
                  <th className="py-2 pr-2 font-medium">{t("common.ui.name")}</th>
                  <th className="py-2 font-medium">{t("common.ui.actions")}</th>
                </tr>
              </thead>
              <tbody>
                {items.map((row, index) => (
                  <tr key={`${row.itemValue}-${index}`} className="border-b border-border/60">
                    <td className="py-2 pr-2 font-mono text-xs">{row.itemValue}</td>
                    <td className="py-2 pr-2">{row.itemName}</td>
                    <td className="py-2">
                      <Button
                        variant="link"
                        size="small"
                        className="h-auto p-0 text-destructive"
                        onClick={() => removeItem(index)}
                      >
                        {t("common.ui.remove")}
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </DictionaryTypeSection>
      </div>
      {hideFooter ? (
        error != null ? <p className="mt-2 text-sm text-destructive">{t(error)}</p> : null
      ) : (
        <div className={footerClass}>
          {error != null ? <p className="mb-2 text-sm text-destructive">{t(error)}</p> : null}
          <div className="flex justify-end gap-2">
            <Button variant="default" onClick={() => onCancel?.()}>
              {t("common.ui.cancel")}
            </Button>
            <Button variant="primary" onClick={handleSave}>
              {t("common.ui.save-all")}
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
