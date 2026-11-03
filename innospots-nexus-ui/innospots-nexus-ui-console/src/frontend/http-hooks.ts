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
  if (code === 0 || code === "0" || code === "OK") {
    return true;
  }
  return false;
}

/**
 * 拆包 Innospots {@code R<T>}（success / code / message / data），
 * 使数据源表达式可直接使用 {@code data.xxx} 业务载荷，而非 {@code data.xxx.data}。
 */
export const nexusHttpHooks: PactorHttpHooks = {
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
