package com.example.tacticsnake_client.events;

import com.example.tacticsnake_client.GameActivity;
import com.shared.events.*;
import com.shared.game.Preferences;

import java.io.Serializable;

public abstract class EventManager implements Serializable {
    protected GameActivity gameActivity;
    private Preferences preferences;
    private int playerNum;

    abstract public void sendEvent(Event event);
    public void handleObject(Object object) {
        if (object instanceof Preferences) {
            setPreferences((Preferences)object);
        } else if (object instanceof GameJoiningEvent) {
            handleGameJoiningEvent((GameJoiningEvent) object);
        } else if (object instanceof GameInvalidEvent) {
            handleGameInvalidEvent((GameInvalidEvent) object);
        } else if (object instanceof GameEnteredEvent) {
            handleGameEnteredEvent((GameEnteredEvent) object);
        } else if (object instanceof GameJoinedEvent) {
            handleGameJoinedEvent((GameJoinedEvent) object);
        } else if (object instanceof GameLeftEvent) {
            handleGameLeftEvent((GameLeftEvent) object);
        } else if (object instanceof PlayerNumEvent) {
            handlePlayerNumEvent((PlayerNumEvent) object);
        } else if (object instanceof GameInitiatedEvent) {
            handleGameInitiatedEvent((GameInitiatedEvent) object);
        } else if (object instanceof PlayerActiveGameEvent) {
            handlePlayerActiveGameEvent((PlayerActiveGameEvent) object);
        } else if (object instanceof PlayerMoveBroadcastGameEvent) {
            handlePlayerMoveBroadcastGameEvent((PlayerMoveBroadcastGameEvent) object);
        } else if (object instanceof PlayerWonGameEvent) {
            handlePlayerWonGameEvent((PlayerWonGameEvent) object);
        } else if (object instanceof PlayerDisconnectedGameEvent) {
            handlePlayerDisconnectedGameEvent((PlayerDisconnectedGameEvent) object);
        } else if (object instanceof PlayerDiedGameEvent) {
            handlePlayerDiedGameEvent((PlayerDiedGameEvent) object);

        }
    }

    abstract void handlePlayerDisconnectedGameEvent(PlayerDisconnectedGameEvent event);
    abstract void handlePlayerDiedGameEvent(PlayerDiedGameEvent event);
    abstract void handlePlayerWonGameEvent(PlayerWonGameEvent event);
    abstract void handlePlayerMoveBroadcastGameEvent(PlayerMoveBroadcastGameEvent event);
    abstract void handlePlayerActiveGameEvent(PlayerActiveGameEvent event);
    abstract void handleGameInitiatedEvent(GameInitiatedEvent event);
    abstract void handlePlayerNumEvent(PlayerNumEvent event);
    abstract void handleGameLeftEvent(GameLeftEvent event);
    abstract void handleGameJoinedEvent(GameJoinedEvent event);
    abstract void handleGameEnteredEvent(GameEnteredEvent event);
    abstract void handleGameInvalidEvent(GameInvalidEvent event);
    abstract void handleGameJoiningEvent(GameJoiningEvent event);

    abstract public void setup();
    abstract public void close();

    public GameActivity getGameActivity() {
        return gameActivity;
    }

    public void setGameActivity(GameActivity gameActivity) {
        this.gameActivity = gameActivity;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public void setPlayerNum(int playerNum) {
        this.playerNum = playerNum;
    }
}
