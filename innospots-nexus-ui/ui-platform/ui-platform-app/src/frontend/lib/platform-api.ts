import { resolveI18nText } from "../components/shared/i18n-text";

export type NexusEnvelope<T> = {
    success?: boolean;
    code?: string | number;
    message?: string;
    data?: T;
    display?: string | Record<string, string> | null;
};

function isNexusSuccess(envelope: NexusEnvelope<unknown>): boolean {
    if (envelope.success === false) {
        return false;
    }
    const code = envelope.code;
    if (code === undefined) {
        return true;
    }
    return code === 0 || code === "0" || code === "OK";
}

function isDevStubBody(body: unknown): boolean {
    return (
        body !== null
        && typeof body === "object"
        && "ok" in body
        && !("code" in body)
        && !("success" in body)
    );
}

/**
 * 从 Innospots {@code R<T>} 解析用户可见错误文案。
 */
export function resolveNexusErrorMessage(
    envelope: NexusEnvelope<unknown>,
    locale: string,
    defaultLocale: string,
): string {
    const fromDisplay = resolveI18nText(envelope.display, locale, defaultLocale);
    if (fromDisplay.length > 0) {
        return fromDisplay;
    }
    const message = envelope.message?.trim();
    if (message && message.length > 0 && message !== "OK") {
        return message;
    }
    if (envelope.code !== undefined && envelope.code !== "OK") {
        return `业务错误码 ${String(envelope.code)}`;
    }
    return "common.ui.request-failed";
}

/**
 * 校验并拆包 {@code R<T>}；失败时抛出带可读 message 的 Error。
 */
export function unwrapNexusEnvelope<T>(
    body: NexusEnvelope<T> | T,
    locale: string,
    defaultLocale: string,
): T {
    if (isDevStubBody(body)) {
        throw new Error("common.ui.backend-api-unavailable");
    }
    if (!body || typeof body !== "object" || !("data" in body)) {
        throw new Error("common.ui.request-failed");
    }
    const envelope = body as NexusEnvelope<T>;
    if (!isNexusSuccess(envelope)) {
        throw new Error(resolveNexusErrorMessage(envelope, locale, defaultLocale));
    }
    if (envelope.data === undefined || envelope.data === null) {
        if (envelope.success) {
            return undefined as T;
        }
        throw new Error("common.ui.response-missing-data");
    }
    return envelope.data;
}

/**
 * 调用 {@code /api/d/platform/*} 并拆包 {@code R<T>}。
 */
export async function platformJson<T>(
    path: string,
    init: RequestInit,
    locale: string,
    defaultLocale: string,
): Promise<T> {
    const suffix = path.startsWith("/") ? path : `/${path}`;
    const url = suffix.startsWith("/api/d/") ? suffix : `/api/d/platform${suffix}`;
    const response = await fetch(url, {
        ...init,
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json",
            ...(init.headers ?? {}),
        },
    });
    const body = (await response.json()) as NexusEnvelope<T> | T;
    if (!response.ok) {
        if (body && typeof body === "object" && "message" in body) {
            throw new Error(
                resolveNexusErrorMessage(body as NexusEnvelope<unknown>, locale, defaultLocale),
            );
        }
        throw new Error(`common.ui.request-failed (HTTP ${response.status})`);
    }
    return unwrapNexusEnvelope(body, locale, defaultLocale);
}
