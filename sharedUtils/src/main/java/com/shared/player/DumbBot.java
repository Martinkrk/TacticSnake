package com.shared.player;

import com.shared.events.Event;
import com.shared.game.Game;

public class DumbBot extends Snake implements BotLogic {
    public DumbBot(Game assignedGame, int playerNum) {
        super(assignedGame);
    }

    @Override
    public void sendEvent(Event event) {}

    @Override
    public int[] generateMove() {
        return new int[] {0, 1};
    }
}
