"use client";

import { useCallback, useEffect, useMemo, useState } from "react";

import { Trash2 } from "lucide-react";

import { Button, Input, Select } from "@pactor-app/ui/components";

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
  [LOCALE_ZH_CN]: "简体中文",
  [LOCALE_EN]: "English",
  ja: "日本語",
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

function localeLabel(tag: string): string {
  return LOCALE_LABELS[tag] ?? tag;
}

/**
 * 字典类型侧滑层：基本信息（多语言类型名称）+ 字典项。
 */
export function DictionaryTypeSavePanel({
  mode = "create",
  typeData,
  itemsData,
  onSave,
  onCancel,
}: DictionaryTypeSavePanelProps) {
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
      setError("字典项的值与名称均不能为空");
      return;
    }
    if (items.some((row) => row.itemValue === value)) {
      setError("同一类型内字典项值不可重复");
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

  const handleSave = useCallback(() => {
    const code = typeCode.trim();
    const zhName = typeNameByLocale[LOCALE_ZH_CN]?.trim() ?? "";
    if (code.length === 0 || zhName.length === 0) {
      setError("类型编码与简体中文类型名称不能为空");
      return;
    }
    if (items.length === 0) {
      setError("请至少配置一个字典项");
      return;
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
    onSave?.({
      mode: isCreate ? "create" : "edit",
      typeCode: code,
      typeName,
      items,
    });
  }, [isCreate, items, localeTags, onSave, typeCode, typeNameByLocale]);

  return (
    <div className="flex min-h-[calc(100dvh-8.5rem)] flex-col">
      <div className="min-h-0 flex-1 space-y-4 overflow-y-auto pr-1">
        <DictionaryTypeSection title="基本信息">
          <div className="space-y-3">
            <label className="block text-sm">
              <span className="text-destructive">*</span> 类型编码
              <Input
                className="mt-1"
                value={typeCode}
                disabled={!isCreate}
                onChange={(event) => setTypeCode(String(event))}
                placeholder="如 USER_STATUS"
              />
            </label>
            <div className="block text-sm">
              <div className="mb-2">
                <span className="text-destructive">*</span> 类型名称
              </div>
              <div className="space-y-2">
                {localeTags.map((tag) => (
                  <div key={tag} className="flex items-center gap-2 text-sm">
                    <span className="w-24 shrink-0 text-muted-foreground">{localeLabel(tag)}</span>
                    <Input
                      className="min-w-0 flex-1"
                      value={typeNameByLocale[tag] ?? ""}
                      onChange={(event) => setLocaleValue(tag, String(event))}
                      placeholder={tag === LOCALE_ZH_CN ? "如 用户状态" : "User status"}
                    />
                    {REQUIRED_LOCALE_TAGS.has(tag) ? (
                      <span className="size-8 shrink-0" aria-hidden="true" />
                    ) : (
                      <button
                        type="button"
                        className="inline-flex size-8 shrink-0 items-center justify-center rounded-md text-muted-foreground transition-colors hover:bg-muted hover:text-destructive"
                        aria-label={`删除${localeLabel(tag)}`}
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
                    placeholder="选择语言"
                    value={localeToAdd}
                    allowClear={false}
                    showSearch={addableLocales.length > 6}
                    options={addableLocales.map((tag) => ({
                      label: localeLabel(tag),
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
                    + 添加语言
                  </Button>
                </div>
              ) : null}
            </div>
            {isCreate ? (
              <p className="text-xs text-muted-foreground">
                运营平台新建固定为平台安全域、启用状态；值形式当前按多值字典项保存。
              </p>
            ) : null}
          </div>
        </DictionaryTypeSection>
        <DictionaryTypeSection title="字典项">
          <div className="mb-3 space-y-2 rounded-md border border-dashed border-border bg-background/50 p-3">
            <div className="text-xs font-medium text-muted-foreground">添加字典项</div>
            <div className="grid gap-2 sm:grid-cols-2">
              <Input
                value={draftValue}
                onChange={(event) => setDraftValue(String(event))}
                placeholder="值 *"
              />
              <Input
                value={draftName}
                onChange={(event) => setDraftName(String(event))}
                placeholder="名称 *"
              />
            </div>
            <Button variant="default" size="small" onClick={addItem}>
              + 添加字典项
            </Button>
          </div>
          {items.length === 0 ? (
            <p className="text-sm text-muted-foreground">暂无字典项</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border text-left text-muted-foreground">
                  <th className="py-2 pr-2 font-medium">值</th>
                  <th className="py-2 pr-2 font-medium">名称</th>
                  <th className="py-2 font-medium">操作</th>
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
                        移除
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </DictionaryTypeSection>
      </div>
      <div
        className="sticky bottom-0 z-10 -mx-6 mt-4 shrink-0 border-t border-border bg-popover px-6 py-3 shadow-[0_-6px_16px_-8px_rgba(0,0,0,0.12)]"
      >
        {error != null ? <p className="mb-2 text-sm text-destructive">{error}</p> : null}
        <div className="flex justify-end gap-2">
          <Button variant="default" onClick={() => onCancel?.()}>
            取消
          </Button>
          <Button variant="primary" onClick={handleSave}>
            保存全部
          </Button>
        </div>
      </div>
    </div>
  );
}
