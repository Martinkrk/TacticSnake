package com.shared.events;

public class PlayerWonGameEvent implements Event {
    private final int who;
    private final String nick;

    public PlayerWonGameEvent(int who, String nick) {
        this.who = who;
        this.nick = nick;
    }

    public int getWho() {
        return who;
    }

    public String getNick() {
        return nick;
    }
}
