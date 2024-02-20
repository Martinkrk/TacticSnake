package com.shared.events;

public class PlayerMoveBroadcastGameEvent implements Event {
    private final int who; //playerNum
    private final String nick;
    private final int[] bodyXY;
    private final int[] headXY;
    private final int[] sprites;
    private final int[] rotations;
    private final int bodyturnMirror;
    private final int[] snakeColor;

    public PlayerMoveBroadcastGameEvent(int who, String nick, int[] bodyXY, int[] headXY, int[] sprites, int[] rotations, int bodyturnMirror, int[] snakeColor) {
        this.who = who;
        this.nick = nick;
        this.bodyXY = bodyXY;
        this.headXY = headXY;
        this.sprites = sprites;
        this.rotations = rotations;
        this.bodyturnMirror = bodyturnMirror;
        this.snakeColor = snakeColor;
    }

    public int getWho() {
        return who;
    }

    public String getNick() {
        return nick;
    }

    public int[] getBodyXY() {
        return bodyXY;
    }

    public int[] getHeadXY() {
        return headXY;
    }

    public int[] getSprites() {
        return sprites;
    }

    public int[] getRotations() {
        return rotations;
    }

    public int getBodyturnMirror() {
        return bodyturnMirror;
    }

    public int[] getSnakeColor() {
        return snakeColor;
    }
}
