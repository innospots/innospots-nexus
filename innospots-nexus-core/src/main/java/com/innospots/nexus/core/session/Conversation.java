package com.innospots.nexus.core.session;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model for a conversation (chat session). Carries metadata about
 * the conversation including user identity, API/extension keys, tags, and
 * a share-memory map for cross-message state.
 * <p>Uses a fluent setter pattern — all mutation methods return {@code this}
 * and call {@link #touch()} to record the update time.</p>
 */
@Getter
public class Conversation {

    private final String conversationId;
    private final String title;
    private final String appKey;
    private String apiKey;
    private String extensionKey;
    private String userId;
    private String userName;
    private String description;
    private List<String> tags = List.of();
    private Map<String, Object> shareMemory = Map.of();
    private final LocalDateTime createdTime = LocalDateTime.now();
    private LocalDateTime updatedTime = createdTime;

    private Conversation(String conversationId, String title, String appKey) {
        this.conversationId = conversationId;
        this.title = title;
        this.appKey = appKey;
    }

    /**
     * Creates a conversation with the required identifiers.
     *
     * @param conversationId unique conversation ID
     * @param title          display title
     * @param appKey         application/tenant key
     */
    public static Conversation named(String conversationId, String title, String appKey) {
        return new Conversation(conversationId, title, appKey);
    }

    /** Returns the unique conversation identifier. */
    public String conversationId() {
        return conversationId;
    }

    /** Returns the display title. */
    public String title() {
        return title;
    }

    /** Returns the application / tenant key. */
    public String appKey() {
        return appKey;
    }

    /** Returns the API key for external integrations. */
    public String apiKey() {
        return apiKey;
    }

    /** Sets the API key and returns this for chaining. */
    public Conversation apiKey(String apiKey) {
        this.apiKey = apiKey;
        touch();
        return this;
    }

    /** Returns the extension key for plugin routing. */
    public String extensionKey() {
        return extensionKey;
    }

    /** Sets the extension key and returns this for chaining. */
    public Conversation extensionKey(String extensionKey) {
        this.extensionKey = extensionKey;
        touch();
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

    /** Sets both user ID and name and returns this for chaining. */
    public Conversation user(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        touch();
        return this;
    }

    /** Returns the conversation description. */
    public String description() {
        return description;
    }

    /** Sets the conversation description and returns this for chaining. */
    public Conversation description(String description) {
        this.description = description;
        touch();
        return this;
    }

    /** Returns the tag list. */
    public List<String> tags() {
        return tags;
    }

    /** Sets tags with a defensive copy and returns this for chaining. */
    public Conversation tags(List<String> tags) {
        this.tags = tags == null ? List.of() : List.copyOf(tags);
        touch();
        return this;
    }

    /** Returns the share-memory map for cross-message state. */
    public Map<String, Object> shareMemory() {
        return shareMemory;
    }

    /** Sets share memory with a defensive copy. */
    public Conversation shareMemory(Map<String, Object> shareMemory) {
        this.shareMemory = shareMemory == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(shareMemory));
        touch();
        return this;
    }

    /** Returns the creation timestamp. */
    public LocalDateTime createdTime() {
        return createdTime;
    }

    /** Returns the last update timestamp. */
    public LocalDateTime updatedTime() {
        return updatedTime;
    }

    /** Updates the {@code updatedTime} to now. Called by all fluent setters. */
    public void touch() {
        updatedTime = LocalDateTime.now();
    }
}
