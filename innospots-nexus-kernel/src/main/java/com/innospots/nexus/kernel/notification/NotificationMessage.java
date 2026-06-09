package com.innospots.nexus.kernel.notification;

import java.time.Instant;

/**
 * Notification message entity.
 *
 * @param messageId  message identifier
 * @param receiverId receiver user identifier
 * @param title      message title
 * @param content    message content
 * @param channel    delivery channel
 * @param status     delivery status
 * @param createdAt  creation time
 */
public record NotificationMessage(
        String messageId,
        String receiverId,
        String title,
        String content,
        NotificationChannel channel,
        NotificationStatus status,
        Instant createdAt
) {
}
