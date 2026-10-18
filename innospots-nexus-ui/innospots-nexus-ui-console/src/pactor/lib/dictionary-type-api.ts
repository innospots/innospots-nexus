type NexusEnvelope<T> = {
    success?: boolean;
    code?: string | number;
    message?: string;
    data?: T;
};

type PageResult<T> = {
    records?: T[];
};

export type DictionaryTypeVo = {
    dictionaryTypeId?: string;
    typeCode?: string;
    typeName?: string | Record<string, string>;
    status?: string;
    sortOrder?: number | null;
    builtIn?: boolean;
    createdAt?: string;
    updatedAt?: string;
};

export type DictionaryItemVo = {
    itemValue?: string;
    itemName?: string;
    status?: string;
    sortOrder?: number | null;
};

function unwrap<T>(body: NexusEnvelope<T> | T): T {
    if (body && typeof body === "object" && "data" in body) {
        const envelope = body as NexusEnvelope<T>;
        if (envelope.success === false) {
            throw new Error(envelope.message ?? `业务错误码 ${String(envelope.code)}`);
        }
        return envelope.data as T;
    }
    return body as T;
}

async function platformGet<T>(path: string): Promise<T> {
    const suffix = path.startsWith("/") ? path : `/${path}`;
    const response = await fetch(`/api/d/platform${suffix}`);
    const body = (await response.json()) as NexusEnvelope<T> | T;
    if (!response.ok) {
        const message =
            body && typeof body === "object" && "message" in body
                ? String((body as NexusEnvelope<T>).message)
                : response.statusText;
        throw new Error(message || "请求失败");
    }
    return unwrap(body);
}

/**
 * 按字典类型 ID 加载类型详情与字典项列表（管理端预览/编辑页共用）。
 */
export async function fetchDictionaryTypeBundle(dictionaryTypeId: string): Promise<{
    type: DictionaryTypeVo;
    items: DictionaryItemVo[];
}> {
    const id = dictionaryTypeId.trim();
    if (id.length === 0) {
        throw new Error("缺少字典类型 ID");
    }
    const type = await platformGet<DictionaryTypeVo & { items?: DictionaryItemVo[] }>(
        `/dictionary-types/${encodeURIComponent(id)}`,
    );
    return {
        type,
        items: Array.isArray(type.items) ? type.items : [],
    };
}
