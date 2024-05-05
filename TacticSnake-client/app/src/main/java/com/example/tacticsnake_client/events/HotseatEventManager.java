package com.example.tacticsnake_client.events;

import android.util.Log;
import com.example.tacticsnake_client.HotseatGame;
import com.example.tacticsnake_client.OfflinePlayer;
import com.shared.events.*;
import com.shared.game.GameSettings;
import com.shared.player.BotLogic;
import com.shared.player.DumbBot;
import com.shared.player.Snake;

import java.util.Locale;

public class HotseatEventManager extends EventManager {
    private HotseatGame game;

    public HotseatEventManager(GameSettings gameSettings, HotseatGame game) {
        this.setPreferences(gameSettings);
        this.game = game;
        this.game.setEventManager(this);
    }

    @Override
    public void sendEvent(Event event) {
        if (event instanceof PlayerMovedGameEvent) {
            handlePlayerMovedGameEvent((PlayerMovedGameEvent) event);
        }
    }

    @Override
    public void handlePlayerMovedGameEvent(PlayerMovedGameEvent event) {
        game.handlePlayerMove(event);
    }

    @Override
    public void handlePlayerDiedGameEvent(PlayerDiedGameEvent event) {
        gameActivity.removeSnake(event.getMoveHistory(), event.getHeadRotation(), event.getSnakeColor(), event.getSnakeBuried());
    }

    @Override
    public void handlePlayerDisconnectedGameEvent(PlayerDisconnectedGameEvent event) {}

    @Override
    public void handlePlayerWonGameEvent(PlayerWonGameEvent event) {
        gameActivity.toggleBoard(false);
        gameActivity.displayEventLog(String.format(Locale.ENGLISH, "Player %d has won!", event.getWho()+1));
        gameActivity.stopMoveTimer();
        gameActivity.alertBoxEvent(String.format(Locale.ENGLISH, "Player %d has won!", event.getWho()+1), "Game Over", "To main menu");
    }

    @Override
    public void handlePlayerMoveBroadcastGameEvent(PlayerMoveBroadcastGameEvent event) {
        gameActivity.updateSnake(event);
    }

    @Override
    public void handlePlayerActiveGameEvent(PlayerActiveGameEvent event) {
        if (game.getPlayers().get(event.getWho()) instanceof BotLogic) {
            gameActivity.displayEventLog(String.format(Locale.ENGLISH, "Bot %d's turn!.", event.getWho()));
            gameActivity.toggleBoard(false);
        } else {
            gameActivity.displayEventLog(String.format(Locale.ENGLISH, "Player %d's turn!.", event.getWho()+1));
            gameActivity.playSoundPing();
            gameActivity.setSnakePos(game.getPlayers().get(game.getCurrentTurn()).getSnakeHead());
            gameActivity.updateBoosterButtonsEvent(game.getPlayerBoosts()[event.getWho()][0], game.getPlayerBoosts()[event.getWho()][1]);
            gameActivity.toggleBoard(true);
        }
        gameActivity.updatePlayerTurnProgress(event.getWho());
        gameActivity.startMoveTimer(getPreferences().moveTimer, event.getWho());
    }

    @Override
    public void handleGameInitiatedEvent(GameInitiatedEvent event) {
    }
    @Override
    public void handlePlayerNumEvent(PlayerNumEvent event) {}
    @Override
    public void handleGameLeftEvent(GameLeftEvent event) {}
    @Override
    public void handleGameJoinedEvent(GameJoinedEvent event) {}
    @Override
    public void handleGameEnteredEvent(GameEnteredEvent event) {}
    @Override
    public void handleGameInvalidEvent(GameInvalidEvent event) {}
    @Override
    public void handleGameJoiningEvent(GameJoiningEvent event) {}
    @Override
    public void handleGameInfoEvent(GameInfoEvent event) {}

    @Override
    public void setup() {
        int[] players = gameActivity.getPlayers();
        Snake p = null;
        for (int i = 0; i < players.length; i++) {
            if (players[i] == 0) continue;
            if (players[i] == 1) {
                p = new OfflinePlayer(game, i);
            } else if (players[i] == 2) {
                p = new DumbBot(game, i);
            }
            game.addPlayer(p);
        }

        game.startGame();
    }

    @Override
    public void close() {}

    public HotseatGame getGame() {
        return game;
    }
}
