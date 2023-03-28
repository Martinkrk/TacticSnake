package com.shared.game;

import java.io.Serializable;

public class Preferences implements Serializable {
    //Game settings
    public int fieldWith;
    public int fieldHeight;
    public boolean isPortalWalls;
    public boolean isCorpse;
    public int playersNum;
    public boolean isPrivate;
    public String gameRoom = "";

    //Player info
    public String nick;
    //snake color?
}

