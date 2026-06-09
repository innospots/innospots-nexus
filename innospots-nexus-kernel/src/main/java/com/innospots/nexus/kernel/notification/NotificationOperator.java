package com.innospots.nexus.kernel.notification;

import java.util.List;
import java.util.Optional;

/**
 * Port for message notification operations.
 */
public interface NotificationOperator {

    /**
     * Sends or schedules a notification message.
     *
     * @param message notification message
     * @return persisted message
     */
    NotificationMessage send(NotificationMessage message);

    /**
     * Finds a notification message.
     *
     * @param messageId message identifier
     * @return message when found
     */
    Optional<NotificationMessage> findMessage(String messageId);

    /**
     * Lists messages for a receiver.
     *
     * @param receiverId receiver user identifier
     * @return receiver messages
     */
    List<NotificationMessage> listMessages(String receiverId);
}
