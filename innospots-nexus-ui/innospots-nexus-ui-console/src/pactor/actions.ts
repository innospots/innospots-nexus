/**
 * 页面级自定义动作注册表，经 SitemapApp 的 `actions` prop 全局注册。
 * 模板默认为空；需要 DSL 表达式做不到的能力（数组操作 / 定时器 / 剪贴板等）
 * 时在此注册自定义动作，handler 可经 ctx.context.state 直接读写页面
 * StateStore，写入即触发 DSL 树重渲染（与内置 setState 动作同一通道）。
 *
 * ActionHandler 类型见 @pactor-app/runtime。
 */

import type { ActionHandler } from "@pactor-app/runtime";

import { collectDictionaryTypeSavePayload } from "./lib/dictionary-type-save-registry";

const TENANT_CODE_PATTERN = /^[a-zA-Z0-9_]+$/;

export const pactorActions: Record<string, ActionHandler> = {
    "app.locale": async (action, ctx) => {
        const locale = typeof action.params?.locale === "string"
            ? action.params.locale.trim()
            : "";
        if (locale.length === 0) {
            return {
                ok: false,
                error: new Error(ctx.context.i18n.t("common.ui.change-failed")),
            };
        }
        const response = await fetch("/api/i18n/locale", {
            method: "POST",
            headers: { "content-type": "application/json" },
            body: JSON.stringify({ locale }),
        });
        if (!response.ok) {
            return {
                ok: false,
                error: new Error(ctx.context.i18n.t("common.ui.change-failed")),
            };
        }
        window.location.reload();
        return { ok: true };
    },
    /**
     * 从当前挂载的 DictionaryTypeSavePanel（hideFooter）收集校验后的保存载荷，
     * 供 Drawer / overlay 的 onOk 动作链使用（结果写入 event）。
     */
    "dictionaryType.collectSave": (_action, ctx) => {
        const payload = collectDictionaryTypeSavePayload();
        if (payload == null) {
            return {
                ok: false,
                error: new Error(ctx.context.i18n.t("common.ui.complete-the-dictionary-form-before-saving")),
            };
        }
        return { ok: true, data: payload };
    },
    "dictionaryType.persist": async (_action, ctx) => {
        const payload = collectDictionaryTypeSavePayload();
        if (payload == null) {
            return {
                ok: false,
                error: new Error(ctx.context.i18n.t("common.ui.complete-the-dictionary-form-before-saving")),
            };
        }
        const editingTypeId = ctx.context.state.get("editingTypeId");
        const dictionaryTypeId =
            typeof editingTypeId === "string" ? editingTypeId.trim() : "";
        if (payload.mode === "edit" && dictionaryTypeId.length === 0) {
            return {
                ok: false,
                error: new Error(ctx.context.i18n.t("common.ui.dictionary-type-id-is-missing")),
            };
        }
        const http = ctx.context.http;
        if (payload.mode === "create") {
            await http.request({
                method: "POST",
                url: "/api/d/platform/dictionary-types",
                body: {
                    typeCode: payload.typeCode,
                    typeName: payload.typeName,
                    items: payload.items,
                },
            });
        } else {
            await http.request({
                method: "PUT",
                url: `/api/d/platform/dictionary-types/${encodeURIComponent(dictionaryTypeId)}`,
                body: {
                    typeName: payload.typeName,
                    items: payload.items,
                },
            });
        }
        return { ok: true, data: payload };
    },
    "validate.tenantCode": (action, ctx) => {
        const value = action.params?.["value"];
        const code = typeof value === "string" ? value.trim() : "";
        if (code.length === 0) {
            return {
                ok: false,
                error: new Error(ctx.context.i18n.t("common.ui.enter-a-tenant-code")),
            };
        }
        if (!TENANT_CODE_PATTERN.test(code)) {
            return {
                ok: false,
                error: new Error(ctx.context.i18n.t("common.ui.tenant-codes-support-only-letters-numbers-and-underscores")),
            };
        }
        return { ok: true };
    },
};
