package com.shared.game;

import java.io.Serializable;

public class GameSettings implements Serializable {
    public int gameMode;
    public int fieldWidth;
    public int fieldHeight;
    public boolean isPortalWalls;
    public boolean isCorpse;
    public int playersNum;
    public boolean isPrivate;
    public String gameRoom;
    public String nick;
    public int[] snakeColor;
}

