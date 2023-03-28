package com.shared.events;

public class PlayerMovedGameEvent implements Event {
    private final int[] move;

    public PlayerMovedGameEvent(int[] move) {
        this.move = move;
    }

    public int[] getMove() {
        return move;
    }
}
