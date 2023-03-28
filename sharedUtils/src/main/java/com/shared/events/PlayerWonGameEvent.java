package com.shared.events;

public class PlayerWonGameEvent implements Event {
    private final int who;

    public PlayerWonGameEvent(int who) {
        this.who = who;
    }

    public int getWho() {
        return who;
    }
}
