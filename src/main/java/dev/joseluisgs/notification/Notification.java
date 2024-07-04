package dev.joseluisgs.notification;

import java.time.LocalDateTime;

public record Notification<T>(Type type, T item, String message, LocalDateTime createdAt) {

    public Notification(Type type, T item, String message) {
        this(type, item, message, LocalDateTime.now());
    }

    public Notification(Type type, T item) {
        this(type, item, null, LocalDateTime.now());
    }

    public Notification(Type type) {
        this(type, null, null, LocalDateTime.now());
    }

    public enum Type {
        CREATE, UPDATE, DELETE, REFRESH
    }
}