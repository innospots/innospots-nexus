package com.innospots.nexus.platform.support;

import org.slf4j.Logger;

/**
 * 平台 Service 层变更操作（创建/更新/删除）的统一 INFO 日志格式。
 */
public final class PlatformMutationLog {

    private PlatformMutationLog() {
    }

    /**
     * 输出 {@code traceId={} ...} 形式的结构化 INFO 日志。
     *
     * @param log     调用方 Logger
     * @param message SLF4J 消息模板（不含 traceId 前缀）
     * @param args    模板参数
     */
    public static void info(Logger log, String message, Object... args) {
        Object[] traceArgs = new Object[args.length + 1];
        traceArgs[0] = PlatformRequestTrace.currentTraceId();
        System.arraycopy(args, 0, traceArgs, 1, args.length);
        log.info("traceId={} " + message, traceArgs);
    }
}
