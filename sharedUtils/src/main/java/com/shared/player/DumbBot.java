package com.shared.player;

import com.shared.events.PlayerMovedGameEvent;
import com.shared.game.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DumbBot extends Snake implements BotLogic {
    Random random;
    public DumbBot(Game assignedGame, int playerNum) {
        super(assignedGame, "Dumb Bot");
        setPlayerNum(playerNum);
        random = new Random();
    }

    @Override
    public void sendObject(Object object) {}

    @Override
    public PlayerMovedGameEvent action() {
        return new PlayerMovedGameEvent(generateMove());
    }

    @Override
    public int[] generateMove() {
        ArrayList<int[]> availableMoves;
        availableMoves = getAssignedGame().getValidMoves(getSnakeHead(), true, false, false, false);
        if (!availableMoves.isEmpty()) {
            return availableMoves.get(random.nextInt(availableMoves.size()));
        }

        if (getJumpBoost() > 0) availableMoves = getAssignedGame().getValidMoves(getSnakeHead(), false, true, false, false);
        if (!availableMoves.isEmpty()) {
            setUsedJump(true);
            return availableMoves.get(random.nextInt(availableMoves.size()));
        }

        if (getDiagonalBoost() > 0) availableMoves = getAssignedGame().getValidMoves(getSnakeHead(), false, false, true, false);
        if (!availableMoves.isEmpty()) {
            setUsedDiagonal(true);
            return availableMoves.get(random.nextInt(availableMoves.size()));
        }

        if (getJumpBoost() > 0 && getDiagonalBoost() > 0) availableMoves = getAssignedGame().getValidMoves(getSnakeHead(), false, false, false, true);
        if (!availableMoves.isEmpty()) {
            setUsedJump(true);
            setUsedDiagonal(true);
            return availableMoves.get(random.nextInt(availableMoves.size()));
        }
        return null;
    }
}
