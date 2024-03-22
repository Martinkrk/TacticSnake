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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

    @Override
    public void addPlayer(Snake player) {
        getPlayers().add(player);
        //tell all clients a new client has joined and how many are in the game already
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
            setGameOver(true);
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
        System.out.println("current move is: " + getCurrentTurn());
        for (Snake player : getPlayers()) {
            if (player.getPlayerNum() == getCurrentTurn()) {
                if (player.isDead()) {
                    setCurrentTurn((getCurrentTurn() + 1) % getCurrentSettings().playersNum);
                    break;
                } else if (player.isDisconnected()) {
                    handleDeath(player);
                    setCurrentTurn((getCurrentTurn() + 1) % getCurrentSettings().playersNum);
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
        System.out.println("Disconnection process for " + player.getNick());
        disconnectPlayer(player);
        player.setDisconnected(true);

        Event disconnectEvent;
        if (isInSession()) {
            System.out.println("Disconnecting, in session");
            disconnectEvent = new PlayerDisconnectedGameEvent(player.getPlayerNum(), player.getNick());
            broadcast(disconnectEvent);
            if (!hasAnyoneWon() && player.getPlayerNum() == getCurrentTurn()) {
                handleDeath(player);
            }
        } else {
            System.out.println("Disconnecting, not in session");
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
            System.out.println("socket and streams closed");
        } catch (IOException e) {
            System.out.println("couldn't close socket or streams");
        }
    }

    private void closeGameTimer() {
        Timer closeGameTimer = new Timer();;
        try {
            closeGameTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    removeGame();
                }
            }, 5000);

        } catch (Exception e) {

        }
    }

    public void handleDeath(Snake player) {
        System.out.println("Handling death for: " +  player.getNick());
        player.setDead(true);
        movesRemove(player.getMoveHistory());
        broadcast(new PlayerDiedGameEvent(player.getPlayerNum(), player.getNick(), player.getMoveHistory(), player.getSnakeDirection(), player.getSnakeColor(), player.getSnakeBuried()));
        if (!hasAnyoneWon()) {
            handleNextTurn();
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

