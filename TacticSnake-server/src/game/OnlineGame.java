package game;

import com.shared.events.*;
import com.shared.game.Game;
import com.shared.game.Preferences;
import com.shared.player.PlayerInfo;
import com.shared.player.Snake;

import server.Server;
import tools.GameRoomCodeGenerator;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;;

public class OnlineGame extends Game {
    private final Server server;
    private final String gameRoom; //By invitation: random HEX string, random game: empty string
    private boolean inSession;

    public OnlineGame(Preferences currentSettings, Server server) {
        super(currentSettings);
        this.server = server;
        this.gameRoom = setGameRoom(currentSettings.isPrivate);
        this.inSession = false;
    }

    public void removeGame() {
        server.removeGame(this);
    }

    @Override
    public void startGame() {
        List<PlayerInfo> playerInfoList = new ArrayList<>();
        int i = 0;
        for (Snake player : getPlayers()) {
            playerInfoList.add(new PlayerInfo(i));
            player.sendEvent(new PlayerNumEvent(i));
            player.setPlayerNum(i);
            player.setSnakeDirection(i+1);
            player.setSnakeHead(new int[] {1, 1});
            player.setSnakeBuried(1);
            i++;
        }
        broadcast(new GameInitiatedEvent(playerInfoList));
        setCurrentTurn(getPlayers().size()-1);
        nextPlayer();
        setInSession(true);
        //TODO change other game states?
    }

    public Server getServer() {
        return server;
    }

    public void nextPlayer() {
        setCurrentTurn((getCurrentTurn()+1) % getCurrentSettings().playersNum);
        System.out.println("current move is: " + getCurrentTurn());
        for (Snake player : getPlayers()) {
            if (player.getPlayerNum() == this.getCurrentTurn() && !player.isDead()) {
                if (arePossibleMoves(player)) {
                    broadcast(new PlayerActiveGameEvent(player.getPlayerNum()));
                    try {
                        ((OnlinePlayer)player).getSocket().setSoTimeout(10000);
                    } catch (SocketException e) {
                        System.out.println(e);;
                    }
                } else {
                    processDeath(player);
                }
                return;
            }
        }
        nextPlayer();
    }

    public void processDisconnect(OnlinePlayer player) {
        getPlayers().remove(player);

        Event disconnectEvent;
        System.out.println("disconnecting");
        if (isInSession()) {
        System.out.println("disconnecting in session");
            if(!isGameOver() && !player.isDead()) {
        System.out.println("disconnecting not over not dead");
                disconnectEvent = new PlayerDisconnectedGameEvent(player.getPlayerNum(), player.getMoveHistory());
                broadcast(disconnectEvent);
                processDeath(player);
            } else if (isGameOver() && getPlayers().size() < 1) {
                System.out.println("disconnecting over and no players");
                removeGame();
            }
        } else {
            System.out.println("disconnecting not in session");
            disconnectEvent = new GameLeftEvent();
            broadcast(disconnectEvent);

            if (getPlayers().size() < 1) {
                removeGame();
            }
        }
    }

    public void checkAlivePlayers() {
        int alive = 0;
        int index = 0;
        for (Snake playerItem : getPlayers()) {
            if (!playerItem.isDead()) {
                index = getPlayers().indexOf(playerItem);
                alive++;
            }
        }
        if (alive == 1) {
            System.out.println("Last man standing");
            broadcast(new PlayerWonGameEvent(getPlayers().get(index).getPlayerNum()));
            setGameOver(true);
            //TODO close game after 5 seconds
        } else if (alive > 1) {
            nextPlayer();
        } else if (alive == 0) {
            //TODO close all client's sockets
            removeGame();
        }
    }

    public void processDeath(Snake player) {
        player.setDead(true);
        broadcast(new PlayerDiedGameEvent(player.getPlayerNum(), player.getMoveHistory()));
        checkAlivePlayers();
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
}

