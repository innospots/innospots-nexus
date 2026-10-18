import type { DictionaryItemDraft } from "../components/dictionary-type-save-panel";

export type DictionaryTypeSavePayload = {
    mode: "create" | "edit";
    typeCode: string;
    typeName: Record<string, string>;
    items: DictionaryItemDraft[];
};

type SubmitHandler = () => DictionaryTypeSavePayload | null;

let activeSubmit: SubmitHandler | null = null;

export function registerDictionaryTypeSaveSubmit(handler: SubmitHandler | null): void {
    activeSubmit = handler;
}

export function collectDictionaryTypeSavePayload(): DictionaryTypeSavePayload | null {
    if (activeSubmit == null) {
        return null;
    }
    return activeSubmit();
}
