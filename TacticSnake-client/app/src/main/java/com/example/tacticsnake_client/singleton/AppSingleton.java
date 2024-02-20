package com.example.tacticsnake_client.singleton;

import com.example.tacticsnake_client.events.OnlineEventManager;

public class AppSingleton {
    private static AppSingleton instance;
    private OnlineEventManager onlineEventManager;

    private AppSingleton() {}

    public static AppSingleton getInstance() {
        if (instance == null) {
            instance = new AppSingleton();
        }
        return instance;
    }

    public void setOnlineEventManager(OnlineEventManager onlineEventManager) {
        this.onlineEventManager = onlineEventManager;
    }

    public OnlineEventManager getOnlineEventManager() {
        return onlineEventManager;
    }
}
