package dev.joseluisgs.notification;

public interface Notifications<T> {
    void send(Notification<T> notification);
}