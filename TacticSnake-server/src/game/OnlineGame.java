package game;

import com.shared.events.*;
import com.shared.events.Event;
import com.shared.game.Game;
import com.shared.game.GameSettings;
import com.shared.player.BotLogic;
import com.shared.player.PlayerInfo;
import com.shared.player.Snake;

import server.Server;
import tools.GameRoomCodeGenerator;
import tools.GameIdGenerator;

import java.io.IOException;
import java.util.*;

public class OnlineGame extends Game {
    private final Server server;
    private int gameId;
    private final String gameRoom; //By invitation: random HEX string, random game: empty string
    private boolean inSession;

    public OnlineGame(GameSettings currentSettings, Server server) {
        super(currentSettings);
        this.server = server;
        this.gameId = GameIdGenerator.getNextId();
        this.gameRoom = setGameRoom(currentSettings.isPrivate);
        this.inSession = false;
    }

    public void removeGame() {
        server.removeGame(this);
    }

    @Override
    public void startGame() {
        List<PlayerInfo> playerInfoList = new ArrayList<>();
        int playerNum = 0;
        for (Snake player : getPlayers()) {
            if (!(player instanceof BotLogic)) {
                player.sendObject(new PlayerNumEvent(playerNum));
            }
            playerInfoList.add(new PlayerInfo(playerNum, getStartingPos()[playerNum], getStartingPos()[playerNum][2], player.getSnakeColor()));
            player.setPlayerNum(playerNum);
            player.setSnakeDirection(playerNum+1);
            player.setSnakeHead(getStartingPos()[playerNum]);
            player.setSnakeBuried(1);
            player.addMoveToHistory(getStartingPos()[playerNum]);
            playerNum++;
        }
        for (int i = 0; i < getPlayers().size(); i++) {
            getTiles().get(getStartingPos()[i][1]).set(getStartingPos()[i][0], 1);
        }

        broadcast(getCurrentSettings());
        broadcast(new GameInitiatedEvent(playerInfoList));
        setCurrentTurn(getPlayers().size()-1);
        setInSession(true);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
            handleNextTurn();
    }

    public Server getServer() {
        return server;
    }

    public boolean handlePlayerMove(PlayerMovedGameEvent event) {
        OnlinePlayer player = (OnlinePlayer) getPlayers().get(getCurrentTurn());
        if (isLegalMove(event, player)) {
            PlayerMoveBroadcastGameEvent pmbge = createSpriteDisplayInfo(event, player);
            player.addMoveHistory(event.getMove());
            player.removeBoosts();
            player.setUsedDiagonal(false);
            player.setUsedJump(false);
            broadcast(pmbge);
            handleNextTurn();
        } else {
            sendLog("ERROR", "Player %s, moved wrong! Disconnecting...");
            handleDisconnect(player);
            return false;
        }
        return true;
    }

    @Override
    public void addPlayer(Snake player) {
        getPlayers().add(player);
        if (!(player instanceof BotLogic)) {
            player.sendObject(new GameEnteredEvent(this.getPlayers().size(), this.getCurrentSettings().playersNum));
        }
        broadcast(new GameJoinedEvent(getPlayers().size(), getCurrentSettings().playersNum));
        if (getCurrentSettings().playersNum == getPlayers().size()) {
            startGame();
        }
    }

    private boolean hasAnyoneWon() {
        Snake alivePlayer = null;
        int alive = 0;
        for (Snake player : getPlayers()) {
            if (!player.isDisconnected() && !player.isDead()) {
                alive++;
                alivePlayer = player;
            }
        }
        if (alive == 1) {
            handleVictory(alivePlayer);
            closeGameTimer();
            return true;
        }
        return false;
    }

    private void handleVictory(Snake alivePlayer) {
        setGameOver(true);
        broadcast(new PlayerWonGameEvent(alivePlayer.getPlayerNum(), alivePlayer.getNick()));
    }

    @Override
    public void handleNextTurn() {
        setCurrentTurn((getCurrentTurn()+1) % getCurrentSettings().playersNum);
        for (Snake player : getPlayers()) {
            if (player.getPlayerNum() == getCurrentTurn()) {
                if (player.isDead()) {
                    break;
                } else if (player.isDisconnected()) {
                    handleDeath(player);
                    break;
                } else {
                    if (arePossibleMoves(player)) {
                        broadcast(new PlayerActiveGameEvent(player.getPlayerNum(), player.getNick()));
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

    public void handleDisconnect(OnlinePlayer player) {
        disconnectPlayer(player);
        player.setDisconnected(true);

        Event disconnectEvent;
        if (isInSession()) {
            disconnectEvent = new PlayerDisconnectedGameEvent(player.getPlayerNum(), player.getNick());
            broadcast(disconnectEvent);
            if (!hasAnyoneWon() && player.getPlayerNum() == getCurrentTurn()) {
                handleDeath(player);
            }
        } else {
            removePlayer(player);
            disconnectEvent = new GameLeftEvent(getPlayers().size(), getCurrentSettings().playersNum);
            broadcast(disconnectEvent);
            if (getPlayers().size() < 1) removeGame();
        }
        server.sendLog("INFO", String.format("(%s) %s has disconnected.", player.getSocket().getInetAddress(), player.getNick()));
    }

    private void disconnectPlayer(OnlinePlayer player) {
        try {
            player.getSocket().close();
            player.getOut().close();
            player.getIn().close();
        } catch (IOException e) {
            sendLog("ERROR", String.format("Couldn't disconnect player %s", player.getNick()));
        }
    }

    private void disconnectAll() {
        for (Snake player : getPlayers()) {
            if (player instanceof BotLogic) continue;
            disconnectPlayer((OnlinePlayer) player);
        }
    }

    private void closeGameTimer() {
        Timer closeGameTimer = new Timer();;
        try {
            closeGameTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    disconnectAll();
                    removeGame();
                }
            }, 10000);

        } catch (Exception e) {

        }
    }

    public void handleDeath(Snake player) {
        player.setDead(true);
        broadcast(new PlayerDiedGameEvent(player.getPlayerNum(), player.getNick(), player.getMoveHistory(), player.getSnakeDirection(), player.getSnakeColor(), player.getSnakeBuried()));
        movesRemove(player.getMoveHistory());
        if (!hasAnyoneWon()) {
            handleNextTurn();
        }
    }

    public void broadcast(Object object) {
        for (Snake player : getPlayers()) {
            if (player instanceof BotLogic) continue;
            if (!player.isDisconnected()) player.sendObject(object);
        }
    }

    public void sendLog(String logType, String logMessage) {
        server.sendLog(logType, logMessage);
    }

    public String getGameRoom() {
        return gameRoom;
    }

    public String setGameRoom(boolean privateGame) {
        if (privateGame) return GameRoomCodeGenerator.generateHexString(4);
        return "";
    }

    public boolean isInSession() {
        return inSession;
    }

    public void setInSession(boolean inSession) {
        this.inSession = inSession;
    }

    public int getGameId() {
        return gameId;
    }
}

