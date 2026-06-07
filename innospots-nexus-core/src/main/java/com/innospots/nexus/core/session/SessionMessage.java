package com.innospots.nexus.core.session;

import com.innospots.nexus.base.domain.field.DomainField;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model for a single message within a conversation.
 * <p>Supports rich display properties (icon, title, source, preview URL),
 * typed payload with {@link DomainField} schema metadata, and a
 * free-form metadata map. The {@code slice} flag marks streaming
 * message fragments.</p>
 */
@Getter
public class SessionMessage {

    private final String conversationId;
    private final String messageId;
    private String sessionId;
    private String viewId;
    private String userId;
    private String userName;
    private String icon;
    private String title;
    private String source;
    private String description;
    private Object payload;
    private List<DomainField> fields = List.of();
    private SessionMessageType messageType = SessionMessageType.TEXT;
    private String downloadUrl;
    private String previewUrl;
    private LocalDateTime createdTime = LocalDateTime.now();
    private boolean slice;
    private Map<String, Object> metadata = Map.of();

    private SessionMessage(String conversationId, String messageId, SessionMessageType messageType) {
        this.conversationId = conversationId;
        this.messageId = messageId;
        this.messageType = messageType == null ? SessionMessageType.TEXT : messageType;
    }

    /**
     * Creates a session message with the required identifiers.
     *
     * @param conversationId parent conversation ID
     * @param messageId      unique message ID
     * @param messageType    message type (defaults to TEXT if null)
     */
    public static SessionMessage of(String conversationId, String messageId, SessionMessageType messageType) {
        return new SessionMessage(conversationId, messageId, messageType);
    }

    /** Returns the parent conversation identifier. */
    public String conversationId() {
        return conversationId;
    }

    /** Returns the unique message identifier. */
    public String messageId() {
        return messageId;
    }

    /** Returns the session / thread identifier. */
    public String sessionId() {
        return sessionId;
    }

    /** Sets the session identifier and returns this for chaining. */
    public SessionMessage sessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    /** Returns the view identifier. */
    public String viewId() {
        return viewId;
    }

    /** Sets the view identifier and returns this for chaining. */
    public SessionMessage viewId(String viewId) {
        this.viewId = viewId;
        return this;
    }

    /** Returns the user identifier. */
    public String userId() {
        return userId;
    }

    /** Returns the user display name. */
    public String userName() {
        return userName;
    }

    /** Sets the user identity (ID + name) and returns this for chaining. */
    public SessionMessage user(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        return this;
    }

    /** Returns the display icon URL or emoji. */
    public String icon() {
        return icon;
    }

    /** Sets the display icon and returns this for chaining. */
    public SessionMessage icon(String icon) {
        this.icon = icon;
        return this;
    }

    /** Returns the display title. */
    public String title() {
        return title;
    }

    /** Sets the display title and returns this for chaining. */
    public SessionMessage title(String title) {
        this.title = title;
        return this;
    }

    /** Returns the source label. */
    public String source() {
        return source;
    }

    /** Sets the source label and returns this for chaining. */
    public SessionMessage source(String source) {
        this.source = source;
        return this;
    }

    /** Returns the description text. */
    public String description() {
        return description;
    }

    /** Sets the description text and returns this for chaining. */
    public SessionMessage description(String description) {
        this.description = description;
        return this;
    }

    /** Returns the typed payload object. */
    public Object payload() {
        return payload;
    }

    /** Sets the payload object and returns this for chaining. */
    public SessionMessage payload(Object payload) {
        this.payload = payload;
        return this;
    }

    /** Returns the list of domain field schemas. */
    public List<DomainField> fields() {
        return fields;
    }

    /** Sets the field schema with defensive copy and returns this for chaining. */
    public SessionMessage fields(List<DomainField> fields) {
        this.fields = fields == null ? List.of() : List.copyOf(fields);
        return this;
    }

    /** Returns the message type. */
    public SessionMessageType messageType() {
        return messageType;
    }

    /** Sets the message type (defaults to TEXT if null) and returns this for chaining. */
    public SessionMessage messageType(SessionMessageType messageType) {
        this.messageType = messageType == null ? SessionMessageType.TEXT : messageType;
        return this;
    }

    /** Returns the download URL for file attachments. */
    public String downloadUrl() {
        return downloadUrl;
    }

    /** Sets the download URL and returns this for chaining. */
    public SessionMessage downloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }

    /** Returns the preview URL. */
    public String previewUrl() {
        return previewUrl;
    }

    /** Sets the preview URL and returns this for chaining. */
    public SessionMessage previewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
        return this;
    }

    /** Returns the creation timestamp. */
    public LocalDateTime createdTime() {
        return createdTime;
    }

    /** Sets the creation timestamp (defaults to now if null) and returns this for chaining. */
    public SessionMessage createdTime(LocalDateTime createdTime) {
        this.createdTime = createdTime == null ? LocalDateTime.now() : createdTime;
        return this;
    }

    /** Returns the streaming slice flag. */
    public boolean slice() {
        return slice;
    }

    /** Sets the streaming slice flag and returns this for chaining. */
    public SessionMessage slice(boolean slice) {
        this.slice = slice;
        return this;
    }

    /** Returns the free-form metadata map. */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /** Sets the free-form metadata map with defensive copy. */
    public SessionMessage metadata(Map<String, Object> metadata) {
        this.metadata = metadata == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(metadata));
        return this;
    }
}
