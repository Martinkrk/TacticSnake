package com.shared.player;

import com.shared.events.Event;
import com.shared.game.Game;

public class DumbBot extends Snake implements BotLogic {
    public DumbBot(Game assignedGame, int playerNum) {
        super(assignedGame, "Dumb Bot");
        setPlayerNum(playerNum);
    }

    @Override
    public void sendObject(Object object) {}

    @Override
    public int[] generateMove() {
        return new int[] {0, 1};
    }
}
