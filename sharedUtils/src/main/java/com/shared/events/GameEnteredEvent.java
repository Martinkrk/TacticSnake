package com.shared.events;

public class GameEnteredEvent implements Event {
    private final int playersNum;
    private final int maxPlayers;

    public GameEnteredEvent(int playersNum, int maxPlayers) {
        this.playersNum = playersNum;
        this.maxPlayers = maxPlayers;
    }

    public int getPlayersNum() {
        return playersNum;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
}
