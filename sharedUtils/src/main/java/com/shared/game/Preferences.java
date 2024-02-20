package com.shared.game;

import java.io.Serializable;

public class Preferences implements Serializable {
    //Game settings
    public int gameMode = 0;
    public int fieldWith = 8;
    public int fieldHeight = 8;
    public boolean isPortalWalls = false;
    public boolean isCorpse = false;
    public int playersNum = 4;
    public boolean isPrivate = false;
    public String gameRoom = "";

    //Player info
    public String nick = "Nameless";
    public int[] snakeColor = new int[] {255, 255, 255};
}

