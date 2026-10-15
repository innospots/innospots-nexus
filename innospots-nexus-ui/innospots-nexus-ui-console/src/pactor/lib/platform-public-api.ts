type NexusEnvelope<T> = {
    success?: boolean;
    code?: string | number;
    message?: string;
    data?: T;
};

function isSuccess(envelope: NexusEnvelope<unknown>): boolean {
    if (envelope.success === false) {
        return false;
    }
    const code = envelope.code;
    if (code === undefined) {
        return true;
    }
    return code === 0 || code === "0" || code === "OK";
}

/**
 * 调用 {@code /api/d/platform/public/*}；依赖路由对 public 路径的匿名转发。
 */
export async function platformPublicFetch<T>(
    path: string,
    init?: RequestInit,
): Promise<T> {
    const suffix = path.startsWith("/") ? path : `/${path}`;
    const url = suffix.startsWith("/api/d/")
        ? suffix
        : `/api/d/platform${suffix}`;
    const response = await fetch(url, {
        ...init,
        headers: {
            "Content-Type": "application/json",
            ...(init?.headers ?? {}),
        },
    });
    const body = (await response.json()) as NexusEnvelope<T> | T;
    if (body && typeof body === "object" && "data" in body) {
        const envelope = body as NexusEnvelope<T>;
        if (!response.ok || !isSuccess(envelope)) {
            throw new Error(envelope.message ?? `请求失败（HTTP ${response.status}）`);
        }
        return envelope.data as T;
    }
    if (!response.ok) {
        throw new Error(`请求失败（HTTP ${response.status}）`);
    }
    return body as T;
}
