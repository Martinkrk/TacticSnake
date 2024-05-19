package com.example.tacticsnake_client.events;

import com.example.tacticsnake_client.LoadingActivity;
import com.example.tacticsnake_client.network.Network;
import com.shared.events.*;
import com.shared.game.GameSettings;

import java.util.Locale;

public class OnlineEventManager extends EventManager{
    private Network network;
    private LoadingActivity loadingActivity;

    public OnlineEventManager(LoadingActivity loadingActivity, GameSettings gameSettings) {
        this.setPreferences(gameSettings);
        this.network = new Network(this);
        this.loadingActivity = loadingActivity;
    }

    private void startResponseThread() {
        network.setSendResponse(true);
    }

    @Override
    public void sendEvent(Event event) {
        network.sendObject(event);
    }

    @Override
    public void handlePlayerMovedGameEvent(PlayerMovedGameEvent event) {

    }

    @Override
    public void handlePlayerDiedGameEvent(PlayerDiedGameEvent event) {
        gameActivity.removeSnake(event.getMoveHistory(), event.getHeadRotation(), event.getSnakeColor(), event.getSnakeBuried());
    }

    @Override
    public void handlePlayerDisconnectedGameEvent(PlayerDisconnectedGameEvent event) {
    }

    @Override
    public void handlePlayerWonGameEvent(PlayerWonGameEvent event) {
        gameActivity.toggleBoard(false);
        gameActivity.displayEventLog(String.format(Locale.ENGLISH, "%s's won!", event.getNick()));
        if (event.getWho() == getPlayerNum()) {
            gameActivity.alertBoxEvent(String.format(Locale.ENGLISH, "You've won!"), "Game Over", "To main menu");
        } else {
            gameActivity.alertBoxEvent(String.format(Locale.ENGLISH, "%s has won!", event.getNick()), "Game Over", "To main menu");
        }
    }

    @Override
    public void handlePlayerMoveBroadcastGameEvent(PlayerMoveBroadcastGameEvent event) {
        gameActivity.updateSnake(event);
        if (event.getWho() == this.getPlayerNum()) {
            gameActivity.setSnakePos(event.getHeadXY());
        }
    }

    @Override
    public void handlePlayerActiveGameEvent(PlayerActiveGameEvent event) {
        if (event.getWho() == getPlayerNum()) {
            gameActivity.displayEventLog("Your Turn!");
            gameActivity.playSoundPing();
            gameActivity.toggleBoard(true);
        } else {
            gameActivity.displayEventLog(String.format(Locale.ENGLISH, "%s's turn!.", event.getNick()));
            gameActivity.toggleBoard(false);
        }
        gameActivity.updatePlayerTurnProgress(event.getWho());
        gameActivity.startMoveTimer(getPreferences().moveTimer, event.getWho());
    }

    @Override
    public void handlePlayerNumEvent(PlayerNumEvent event) {
        setPlayerNum(event.getPlayerNum());
    }

    @Override
    public void handleGameInitiatedEvent(GameInitiatedEvent event) {
        loadingActivity.startGame(event);
    }

    @Override
    public void handleGameLeftEvent(GameLeftEvent event) {
        String text = String.format(Locale.ENGLISH,"In lobby. %d out of %d.", event.getPlayersNum(), event.getPlayersMax());
        loadingActivity.editTextView(text);
    }

    @Override
    public void handleGameJoinedEvent(GameJoinedEvent event) {
        String text = String.format(Locale.ENGLISH, "In lobby. %d out of %d.%n", event.getPlayersNum(), event.getPlayersMax());
        loadingActivity.editTextView(text);
    }

    @Override
    public void handleGameEnteredEvent(GameEnteredEvent event) {
        loadingActivity.editTextView("Game joined");
        startResponseThread();
    }

    @Override
    public void handleGameInvalidEvent(GameInvalidEvent event) {
        loadingActivity.cancelGame("No game found", "Couldn't find the requested private game");
    }

    @Override
    public void handleGameJoiningEvent(GameJoiningEvent event) {
        loadingActivity.editTextView("Joining a game...");
    }

    @Override
    public void handleGameInfoEvent(GameInfoEvent event) {
        loadingActivity.editRoomCodeTextView(event.getGameRoom());
    }

    @Override
    public void setup() {

    }

    public void handleConnectionError() {
        close();
        if (getGameActivity() != null) {
//            getGameActivity().cancelGame("Connection Error", "Disconnected.");
            getGameActivity().cancelGame("", "");
        } else if (getLoadingActivity() != null) {
//            getLoadingActivity().cancelGame("Connection Error", "Disconnected.");
            getLoadingActivity().cancelGame("", "");
        } else {
            throw new NullPointerException("LoadingActivity can not be null!");
        }
    }

    @Override
    public void close() {
        if (network.getStopThread().getState() == Thread.State.NEW) {
            network.getStopThread().start();
        }
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public LoadingActivity getLoadingActivity() {
        return loadingActivity;
    }

    public void setLoadingActivity(LoadingActivity loadingActivity) {
        this.loadingActivity = loadingActivity;
    }
}