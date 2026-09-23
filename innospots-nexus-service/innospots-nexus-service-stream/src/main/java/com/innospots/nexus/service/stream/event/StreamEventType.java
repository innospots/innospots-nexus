package com.innospots.nexus.service.stream.event;

/**
 * SSE/NDJSON 线格式事件类型名。
 *
 * @author Smars
 * @date 2026/09/15
 */
public enum StreamEventType {

    STREAM_OPEN("stream.open"),
    MESSAGE("message"),
    MESSAGE_DELTA("message.delta"),
    PROGRESS("progress"),
    STATUS("status"),
    ERROR("error"),
    STREAM_COMPLETED("stream.completed"),
    STREAM_CANCELLED("stream.cancelled"),
    HEARTBEAT("heartbeat");

    private final String wireName;

    StreamEventType(String wireName) {
        this.wireName = wireName;
    }

    /**
     * 返回线格式类型名。
     *
     * @return wire 名称
     */
    public String wireName() {
        return wireName;
    }
}
