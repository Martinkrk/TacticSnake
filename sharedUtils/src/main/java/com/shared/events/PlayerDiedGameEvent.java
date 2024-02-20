package com.shared.events;

import java.util.List;

public class PlayerDiedGameEvent implements Event {
    private final int who;
    private final String nick;
    private final List<int[]> moveHistory;
    private final int headRotation;
    private final int[] snakeColor;
    private final int snakeBuried;

    public PlayerDiedGameEvent(int who, String nick, List<int[]> moveHistory, int headRotation, int[] snakeColor, int snakeBuried) {
        this.who = who;
        this.nick = nick;
        this.moveHistory = moveHistory;
        this.headRotation = headRotation;
        this.snakeColor = snakeColor;
        this.snakeBuried = snakeBuried;
    }

    public int getWho() {
        return who;
    }

    public List<int[]> getMoveHistory() {
        return moveHistory;
    }

    public String getNick() {
        return nick;
    }

    public int getHeadRotation() {
        return headRotation;
    }

    public int[] getSnakeColor() {
        return snakeColor;
    }

    public int getSnakeBuried() {
        return snakeBuried;
    }
}
