package com.example.tacticsnake_client.events;

import android.util.Log;
import com.example.tacticsnake_client.LoadingActivity;
import com.example.tacticsnake_client.network.Network;
import com.shared.events.*;
import com.shared.game.Preferences;

import java.util.Locale;

public class OnlineEventManager extends EventManager{
    private Network network;
    private LoadingActivity loadingActivity;

    public OnlineEventManager(LoadingActivity loadingActivity, Preferences preferences) {
        this.setPreferences(preferences);
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
    public void handlePlayerDiedGameEvent(PlayerDiedGameEvent event) {
        gameActivity.displayEventLog(String.format(Locale.ENGLISH, "%s(%d)'s snake has died."));
        gameActivity.removeSnake(event.getMoveHistory(), event.getHeadRotation(), event.getSnakeColor(), event.getSnakeBuried());
    }

    @Override
    public void handlePlayerDisconnectedGameEvent(PlayerDisconnectedGameEvent event) {
        gameActivity.displayEventLog(String.format(Locale.ENGLISH, "%s(%d) has disconnected.", event.getNick(), event.getWho()));
    }

    @Override
    public void handlePlayerWonGameEvent(PlayerWonGameEvent event) {
        gameActivity.toggleBoard(false);
        gameActivity.displayEventLog(String.format(Locale.ENGLISH, "%s(%d) has won!.", event.getNick(), event.getWho()));
    }

    @Override
    public void handlePlayerMoveBroadcastGameEvent(PlayerMoveBroadcastGameEvent event) {
        gameActivity.displayEventLog(String.format(Locale.ENGLISH, "%s(%d) made a move!. [%d, %d]", event.getNick(), event.getWho(), event.getHeadXY()[0], event.getHeadXY()[1]));
        gameActivity.updateSnake(event);
    }

    @Override
    public void handlePlayerActiveGameEvent(PlayerActiveGameEvent event) {
        if (event.getWho() == getPlayerNum()) {
            gameActivity.displayEventLog("Your Turn!");
            gameActivity.toggleBoard(true);
            //TODO sound??
        } else {
            gameActivity.displayEventLog(String.format(Locale.ENGLISH, "%s(%d)'s turn!.", event.getNick(), event.getWho()));
            gameActivity.toggleBoard(false);
        }
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
        loadingActivity.cancelGame("Game Search Error", "Couldn't find the requested private game");
    }

    @Override
    public void handleGameJoiningEvent(GameJoiningEvent event) {
        loadingActivity.editTextView("Joining a game...");
    }

    @Override
    public void setup() {

    }

    public void handleConnectionError() {
        close();
        if (getGameActivity() != null) {
            getGameActivity().cancelGame("Connection Error", "Disconnected.");
        } else if (getLoadingActivity() != null) {
            getLoadingActivity().cancelGame("Connection Error", "Disconnected.");
        } else {
            throw new NullPointerException("LoadingActivity can not be null!");
        }
    }

    @Override
    public void close() {
        network.close();
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