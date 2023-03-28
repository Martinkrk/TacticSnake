package com.shared.events;

import java.util.List;

public class PlayerDiedGameEvent implements Event {
    private final int who;
    private final List<Integer[]> moveHistory;

    public PlayerDiedGameEvent(int who, List<Integer[]> moveHistory) {
        this.who = who;
        this.moveHistory = moveHistory;
    }

    public int getWho() {
        return who;
    }

    public List<Integer[]> getMoveHistory() {
        return moveHistory;
    }
}
