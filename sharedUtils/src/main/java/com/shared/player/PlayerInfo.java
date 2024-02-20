package com.shared.player;

import java.io.Serializable;

public class PlayerInfo implements Serializable {
    public int playerNum;
    public int[] headPos;
    public int headRotation;
    public int[] snakeColor;

    public PlayerInfo(int playerNum, int[] headPos, int headRotation, int[] snakeColor) {
        this.playerNum = playerNum;
        this.headPos = headPos;
        this.headRotation = headRotation;
        this.snakeColor = snakeColor;
    }
}
