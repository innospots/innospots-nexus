import type { PactorHttpHooks } from "pactor-app";

type NexusEnvelope = {
  success?: boolean;
  code?: string | number;
  message?: string;
  data?: unknown;
};

function isNexusEnvelope(value: unknown): value is NexusEnvelope {
  return typeof value === "object" && value !== null && "data" in value;
}

function isNexusSuccess(envelope: NexusEnvelope): boolean {
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
 * 拆包 Innospots `R<T>`（success / code / message / data），供页面 DSL 的
 * dataSources 直接消费业务载荷（与 ui-platform-app 的 http-hooks 一致）。
 */
export const nexusHttpHooks: PactorHttpHooks = {
  request(config) {
    const method = (config.method ?? "GET").toUpperCase();
    if (!["POST", "PUT", "PATCH", "DELETE"].includes(method)) {
      return config;
    }
    // Jersey 端点声明 @Consumes(APPLICATION_JSON)：写请求即使无 body 也需要 Content-Type，
    // 否则返回 415。这里统一补齐，DSL 的 request action 无需逐条声明。
    return {
      ...config,
      headers: {
        accept: "application/json",
        "content-type": "application/json",
        ...config.headers,
      },
    };
  },
  response(data) {
    if (!isNexusEnvelope(data)) {
      return data;
    }
    if (!isNexusSuccess(data)) {
      throw new Error(data.message ?? `业务错误码 ${String(data.code)}`);
    }
    return data.data;
  },
};
