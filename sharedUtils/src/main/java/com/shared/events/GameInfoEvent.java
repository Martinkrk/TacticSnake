package com.shared.events;

public class GameInfoEvent implements Event {
    private final int gameNum;
    private final String gameRoom;

    public GameInfoEvent(String gameRoom) {
        this.gameNum = 0;
        this.gameRoom = gameRoom;
    }

    public int getGameNum() {
        return gameNum;
    }

    public String getGameRoom() {
        return gameRoom;
    }
}
