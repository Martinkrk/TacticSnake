package com.example.tacticsnake_client;

import com.shared.game.Game;
import com.shared.player.Snake;

public class OfflinePlayer extends Snake {

    public OfflinePlayer(Game assignedGame, int playerNum) {
        super(assignedGame, "Player" + playerNum+1);
        setPlayerNum(playerNum);
    }

    @Override
    public void sendObject(Object object) {
        //TODO use the fitting event manager to reflect changes in the game
    }
}
