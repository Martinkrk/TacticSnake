package com.example.tacticsnake_client;

import android.util.Log;
import com.example.tacticsnake_client.events.HotseatEventManager;
import com.shared.events.*;
import com.shared.game.Game;
import com.shared.game.GameSettings;
import com.shared.player.BotLogic;
import com.shared.player.PlayerInfo;
import com.shared.player.Snake;

import java.util.*;

public class HotseatGame extends Game {
    private final int[][] snakeColors = new int[][] {{255, 0, 0}, {0, 255, 0}, {0, 0, 255}, {255, 255, 0}};
    private volatile int[][] playerBoosts = new int[][] {{1, 1}, {1, 1}, {1, 1}, {1, 1}};

    private HotseatEventManager eventManager;
    public HotseatGame(GameSettings currentSettings) {
        super(currentSettings);
    }

    @Override
    public void addPlayer(Snake player) {
        getPlayers().add(player);
    }

    private boolean hasAnyoneWon() {
        Snake alivePlayer = null;
        int alive = 0;
        for (Snake player : getPlayers()) {
            if (!player.isDead()) {
                alive++;
                alivePlayer = player;
            }
        }
        if (alive == 1) {
            handleVictory(alivePlayer);
            return true;
        }
        return false;
    }

    private void handleVictory(Snake alivePlayer) {
        setGameOver(true);
        eventManager.handlePlayerWonGameEvent(new PlayerWonGameEvent(alivePlayer.getPlayerNum(), alivePlayer.getNick()));
    }

    @Override
    public void handleNextTurn() {
        setCurrentTurn((getCurrentTurn()+1) % (getPlayers().size()));
        for (Snake player : getPlayers()) {
            if (player.getPlayerNum() == getCurrentTurn()) {
                if (player.isDead()) {
                    break;
                } else {
                    if (arePossibleMoves(player)) {
                        getEventManager().handlePlayerActiveGameEvent(new PlayerActiveGameEvent(player.getPlayerNum(), player.getNick()));
                        if (player instanceof BotLogic) {
                            handlePlayerMove(player.action());
                        }
                    } else {
                        handleDeath(player);
                        break;
                    }
                    return;
                }
            }
        }
        if (!isGameOver()) {
            handleNextTurn();
        }
    }

    @Override
    public void startGame() {
        int playerNum = 0;
        for (Snake player : getPlayers()) {
            player.setPlayerNum(playerNum);
            player.setSnakeDirection(playerNum+1);
            player.setSnakeHead(getStartingPos()[playerNum]);
            player.setSnakeBuried(1);
            player.addMoveToHistory(getStartingPos()[playerNum]);
            player.setSnakeColor(snakeColors[playerNum]);
            playerNum++;
        }

        for (int i = 0; i < getPlayers().size(); i++) {
            getTiles().get(getStartingPos()[i][1]).set(getStartingPos()[i][0], 1);
        }
        setCurrentTurn(getPlayers().size()-1);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        handleNextTurn();
    }

    public void handlePlayerMove(PlayerMovedGameEvent event) {
        Snake player = getPlayers().get(getCurrentTurn());
        if (isLegalMove(event, player)) {
            PlayerMoveBroadcastGameEvent pmbge = createSpriteDisplayInfo(event, player);
            player.addMoveHistory(event.getMove());
            player.removeBoosts();
            if (player.isUsedDiagonal()) playerBoosts[getCurrentTurn()][0] = 0;
            if (player.isUsedJump()) playerBoosts[getCurrentTurn()][1] = 0;
            player.setUsedDiagonal(false);
            player.setUsedJump(false);
            eventManager.handlePlayerMoveBroadcastGameEvent(pmbge);
            handleNextTurn();
        } else {
            handleDeath(player);
        }
    }

    public void handleDeath(Snake player) {
        player.setDead(true);
        movesRemove(player.getMoveHistory());
        eventManager.handlePlayerDiedGameEvent(new PlayerDiedGameEvent(player.getPlayerNum(), player.getNick(), player.getMoveHistory(), player.getSnakeDirection(), player.getSnakeColor(), player.getSnakeBuried()));
        hasAnyoneWon();
    }

    public List<PlayerInfo> generateHotseatSnakes() {
        List<PlayerInfo> playerInfos = new ArrayList<>();
        for (Snake player : getPlayers()) {
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

    public HotseatEventManager getEventManager() {
        return eventManager;
    }

    public void setEventManager(HotseatEventManager eventManager) {
        this.eventManager = eventManager;
    }

    public int[][] getPlayerBoosts() {
        return playerBoosts;
    }
}
