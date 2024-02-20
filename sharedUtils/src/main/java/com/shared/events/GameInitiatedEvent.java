package com.shared.events;

import com.shared.player.PlayerInfo;

import java.util.List;

public class GameInitiatedEvent implements Event {
    private final List<PlayerInfo> players;

    public GameInitiatedEvent(List<PlayerInfo> players) {
        this.players = players;
    }

    public List<PlayerInfo> getPlayers() {
        return players;
    }
}
