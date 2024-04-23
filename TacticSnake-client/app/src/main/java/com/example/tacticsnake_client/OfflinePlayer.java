package com.example.tacticsnake_client;

import com.shared.events.PlayerMovedGameEvent;
import com.shared.game.Game;
import com.shared.player.Snake;

import java.io.IOException;

public class OfflinePlayer extends Snake {

    public OfflinePlayer(Game assignedGame, int playerNum) {
        super(assignedGame, "Player" + playerNum+1);
        setPlayerNum(playerNum);
    }

    @Override
    public void sendObject(Object object) {}

    @Override
    public PlayerMovedGameEvent action() {
        return null;
    }
}
