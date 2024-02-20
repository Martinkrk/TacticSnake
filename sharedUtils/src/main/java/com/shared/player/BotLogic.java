package com.shared.player;

import java.util.Random;

public interface BotLogic {
    Random rnd = new Random();
    public int[] generateMove();

    default int[] generateSnakeColor(int[] snakeColor) {
        Random rnd = new Random();
        return new int[]{rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)};
    }
}
