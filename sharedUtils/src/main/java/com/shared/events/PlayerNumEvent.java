package com.shared.events;

public class PlayerNumEvent implements Event{
    private final int playerNum;

    public PlayerNumEvent(int playerNum) {
        this.playerNum = playerNum;
    }

    public int getPlayerNum() {
        return playerNum;
    }
}
