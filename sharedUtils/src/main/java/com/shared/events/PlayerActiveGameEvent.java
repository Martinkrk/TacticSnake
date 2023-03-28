package com.shared.events;

import com.shared.player.PlayerInfo;

public class PlayerActiveGameEvent implements Event {
    private final int playerNum;

    public PlayerActiveGameEvent(int playerNum) {
        this.playerNum = playerNum;
    }

    public int getPlayerNum() {
        return playerNum;
    }
}
