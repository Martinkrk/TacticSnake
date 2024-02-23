package com.example.tacticsnake_client.events;

import android.util.Log;
import com.example.tacticsnake_client.HotseatGame;
import com.example.tacticsnake_client.OfflinePlayer;
import com.shared.events.*;
import com.shared.game.GameSettings;
import com.shared.player.DumbBot;
import com.shared.player.Snake;

public class HotseatEventManager extends EventManager {
    private HotseatGame game;

    public HotseatEventManager(GameSettings gameSettings, HotseatGame game) {
        this.setPreferences(gameSettings);
        this.game = game;
    }

    @Override
    public void sendEvent(Event event) {

    }

    @Override
    public void handlePlayerDiedGameEvent(PlayerDiedGameEvent event) {

    }

    @Override
    public void handlePlayerDisconnectedGameEvent(PlayerDisconnectedGameEvent event) {

    }

    @Override
    public void handlePlayerWonGameEvent(PlayerWonGameEvent event) {

    }

    @Override
    public void handlePlayerMoveBroadcastGameEvent(PlayerMoveBroadcastGameEvent event) {

    }

    @Override
    public void handlePlayerActiveGameEvent(PlayerActiveGameEvent event) {

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
    public void setup() {
        int[] players = gameActivity.getPlayers();
        Snake p = null;
        Log.d("DEBUG hotseateventm", String.valueOf(players.length));
        for (int i = 0; i < players.length; i++) {
            if (players[i] == 0) continue;
            if (players[i] == 1) {
                p = new OfflinePlayer(game, i);
            } else if (players[i] == 2) {
                p = new DumbBot(game, i);
            }
            game.addPlayer(p);
        }
    }

    @Override
    public void close() {

    }

    public HotseatGame getGame() {
        return game;
    }
}
