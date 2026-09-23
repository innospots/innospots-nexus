package com.innospots.nexus.service.stream.channel;

/**
 * 有界 emit 结果。
 *
 * @author Smars
 * @date 2026/09/15
 */
public enum EmitResult {
    ACCEPTED,
    DROPPED_LATEST,
    DROPPED_OLDEST
}
