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

/** 拆包 Innospots `R<T>`，供页面 DSL 的 dataSources 直接消费业务载荷。 */
export const nexusHttpHooks: PactorHttpHooks = {
  request(config) {
    const method = (config.method ?? "GET").toUpperCase();
    if (!["POST", "PUT", "PATCH", "DELETE"].includes(method)) {
      return config;
    }
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
