package com.example.tacticsnake_client.events;

public interface EventManager {
    void sendEvent(Object event);
    void receiveEvent(Object event);
}
