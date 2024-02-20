package com.example.tacticsnake_client;

import android.util.Log;
import com.shared.game.Game;
import com.shared.game.Preferences;
import com.shared.player.PlayerInfo;
import com.shared.player.Snake;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HotseatGame extends Game {
    private final int[][] snakeColors = new int[][] {{255, 0, 0}, {0, 255, 0}, {0, 0, 255}, {255, 255, 0}};
    public HotseatGame(Preferences currentSettings) {
        super(currentSettings);
    }

    @Override
    public void addPlayer(Snake player) {
        getPlayers().add(player);
    }

    @Override
    public void handleNextTurn() {

    }

    @Override
    public void startGame() {

    }

    public List<PlayerInfo> generateHotseatSnakes() {
        List<PlayerInfo> playerInfos = new ArrayList<>();
        for (Snake player : getPlayers()) {
            Log.d("DEBUG hotseatgame", "player: " + String.valueOf(player.getPlayerNum()));
            if (player != null) {
                playerInfos.add(new PlayerInfo(player.getPlayerNum(),
                        Arrays.copyOfRange(getStartingPos()[player.getPlayerNum()], 0, 2),
                        getStartingPos()[player.getPlayerNum()][2], getSnakeColors()[player.getPlayerNum()]));
            }
        }
        return playerInfos;
    }

    public int[][] getSnakeColors() {
        return snakeColors;
    }
}
