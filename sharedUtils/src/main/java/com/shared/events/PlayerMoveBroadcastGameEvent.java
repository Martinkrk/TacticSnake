package com.shared.events;

import java.util.List;

public class PlayerMoveBroadcastGameEvent implements Event {
    private final int who; //playerNum
    private final int[] bodyXY;
    private final int[] headXY;
    private final int[] sprites;
    private final int[] rotations;
    private final int bodyturnMirror;

    public PlayerMoveBroadcastGameEvent(int who, int[] bodyXY, int[] headXY, int[] sprites, int[] rotations, int bodyturnMirror) {
        this.who = who;
        this.bodyXY = bodyXY;
        this.headXY = headXY;
        this.sprites = sprites;
        this.rotations = rotations;
        this.bodyturnMirror = bodyturnMirror;
    }

    public int getWho() {
        return who;
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
}
