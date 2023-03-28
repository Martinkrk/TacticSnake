package com.example.tacticsnake_client;

import com.shared.events.Event;
import com.shared.game.Game;
import com.shared.player.Snake;

public class OfflinePlayer extends Snake {

    public OfflinePlayer(Game assignedGame, int playerNum) {
        super(assignedGame);
    }

    @Override
    public void sendEvent(Event event) {
        //TODO use the fitting event manager to reflect changes in the game
    }
}
