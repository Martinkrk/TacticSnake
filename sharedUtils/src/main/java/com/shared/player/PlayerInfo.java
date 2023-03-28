package com.shared.player;

import java.io.Serializable;

public class PlayerInfo implements Serializable {
    public int playerNum;
    public int[] headPos;
    public int headRotation;

    public PlayerInfo(int playerNum) {
        this.playerNum = playerNum;
    }
}
