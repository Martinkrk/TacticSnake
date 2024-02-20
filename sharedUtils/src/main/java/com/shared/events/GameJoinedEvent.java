package com.shared.events;

public class GameJoinedEvent implements Event {
    private final int playersNum;
    private final int playersMax;

    public GameJoinedEvent(int playersNum, int playersMax) {
        this.playersNum = playersNum;
        this.playersMax = playersMax;
    }

    public int getPlayersNum() {
        return playersNum;
    }

    public int getPlayersMax() {
        return playersMax;
    }
}
