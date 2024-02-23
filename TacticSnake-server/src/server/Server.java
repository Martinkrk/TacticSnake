package server;

import com.shared.events.*;
import com.shared.events.Event;
import game.OnlineGame;
import com.shared.game.GameSettings;
import game.OnlinePlayer;
import gui.ServerGUI;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class Server {
    //TODO client exit won't close connection with server -Update fixed?
    //TODO move is always relative to first player -Update no?
    private final int port = 8080;
    private Socket socket;
    private ServerSocket serverSocket;
    private List<OnlineGame> games;
    private final int gameIdCounter;
    private boolean serverRunning;
    private final ServerGUI serverGUI;

    public Server() {
        this.games = new ArrayList<>();
        this.gameIdCounter = 0;
        this.serverRunning = true;
        this.serverGUI = new ServerGUI(this);
        startServer();
    }

    public void startServer() {
        sendLog("INFO", "Server is starting...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            sendLog("INFO", String.format("Server has started on port %d", port));

            while (serverRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                sendLog("INFO", String.format("(%s) has connected to the server", clientSocket.getInetAddress()));

                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());

                GameSettings gameSettings = (GameSettings) input.readObject();
                System.out.println("Received game settings from client: " + gameSettings);

                //Tell client server is trying to find or create a game for client
                sendEvent(output, new GameJoiningEvent());

                OnlineGame game = findOrCreateGame(gameSettings);
                if (game == null) {

                    //Tell client there is no private game with such game room code
                    sendEvent(output, new GameInvalidEvent());

                    System.out.println("No such private game - null returned");
                    sendLog("INFO", String.format("(%s) requested an invalid game, abort.", clientSocket.getInetAddress()));
                    clientSocket.close();
                    continue;
                }
                System.out.printf("Game found or created. Games: %d%n", games.size());

                OnlinePlayer player = new OnlinePlayer(game, gameSettings.nick, output, input, clientSocket);
                player.setSnakeColor(gameSettings.snakeColor);
                game.addPlayer(player);
                serverGUI.updateGames(games);
                System.out.println("Game " + getGames().size() + ". Players: " + game.getPlayers().size());
                sendEvent(output, new GameInfoEvent(game.getGameRoom()));

                ClientHandler clientHandler = new ClientHandler(clientSocket, input, output, player);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();

                sendLog("INFO", String.format("(%s) %s has joined the game %d.",
                        player.getSocket().getInetAddress(),
                        player.getNick(),
                        game.getGameId()));
            }
        } catch (IOException e) {
            System.out.println("IOException in main server loop");
            sendLog("ERROR", "IOException in main server loop");
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException");
            sendLog("ERROR", "ClassNotFoundException in main server loop");
        }
    }

    public void stopServer() {
        serverRunning = false;
        System.exit(0);
    }

    private void sendEvent(ObjectOutputStream out, Event event) {
        try {
            out.writeObject(event);
            out.flush();
        } catch (IOException e) {
            System.out.println("Couldn't send event to client " + this);
        }
    }

    private OnlineGame findOrCreateGame(GameSettings gameSettings) {
        if (gameSettings.gameMode != 3) {
            for (OnlineGame game : games) {
                if (game.isInSession()) continue;

                if (gameSettings.gameMode == 0) { //Find any game
                    if (!game.getCurrentSettings().isPrivate) {
                        return game;
                    }
                } else if (gameSettings.gameMode == 1) { //Find game matching client's gameSettings
                    if (!game.getCurrentSettings().isPrivate && gameSettingsMatch(game, gameSettings)) {
                        return game;
                    }
                } else if (gameSettings.gameMode == 2) { //Find private game
                    if (game.getCurrentSettings().isPrivate && game.getGameRoom().equals(gameSettings.gameRoom)) {
                        return game;
                    }
                }
            }
            //If private game is not found
            if (gameSettings.gameMode == 2) return null;
        }

        OnlineGame game = new OnlineGame(gameSettings, this);
        games.add(game);
        System.out.println("Game created");
        sendLog("INFO", String.format("Game %d has been created.", game.getGameId()));

        return game;
    }

    private boolean gameSettingsMatch(OnlineGame g, GameSettings gs) {
            if (g.getCurrentSettings().fieldWidth != gs.fieldWidth ||
                    g.getCurrentSettings().fieldHeight != gs.fieldHeight ||
                    g.getCurrentSettings().isPortalWalls != gs.isPortalWalls ||
                    g.getCurrentSettings().isCorpse != gs.isCorpse ||
                    g.getCurrentSettings().playersNum != gs.playersNum ||
                    g.getPlayers().size() >= g.getCurrentSettings().playersNum ||
                    g.getCurrentSettings().isPrivate != gs.isPrivate) {
                System.out.println("settings don't match");
                return false;
            }

        System.out.println("game found");
        return true;
    }

    public void removeGame(OnlineGame game) {
        this.games.remove(game);
        System.out.printf("Game removed. Games: %d%n", games.size());
        sendLog("INFO", String.format("Game %d has been destroyed.", game.getGameId()));
        serverGUI.updateGames(games);
    }

    public void sendLog(String logType, String logMessage) {
        serverGUI.appendLog(String.format(" [%s] %s", logType, logMessage));
    }

    public List<OnlineGame> getGames() {
        return games;
    }

    public void setGames(List<OnlineGame> games) {
        this.games = games;
    }

    public static void main(String[] args) {
        Server server = new Server();
    }


    public static class ClientHandler implements Runnable {
        private final Socket socket;
        private final ObjectInputStream inputStream;
        private final ObjectOutputStream outputStream;
        private final OnlinePlayer player;
        private final OnlineGame game;
        private Timer responseTimer;
        private Timer moveTimer;
        private volatile boolean running;
        private volatile boolean moveTimerCanceled;

        public ClientHandler(Socket socket, ObjectInputStream inputStream, ObjectOutputStream outputStream, OnlinePlayer player) {
            this.socket = socket;
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            this.player = player;
            this.game = (OnlineGame) player.getAssignedGame();
            this.responseTimer = new Timer();
            this.moveTimer = new Timer();
            this.running = true;
            this.moveTimerCanceled = true;
        }

        @Override
        public void run() {
            System.out.println("Thread started successfully for " + player.getNick());

            while (running) {
                if (player.getPlayerNum() == game.getCurrentTurn() && game.isInSession() && moveTimerCanceled) {
                    try {
                        moveTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                running = false;
                                game.handleDisconnect(player);
                            }
                        }, 60000);
                        moveTimerCanceled = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    responseTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            running = false;
                            game.handleDisconnect(player);
                        }
                    }, 11000);

                } catch (Exception e) {}

                if (!running) break;

                try {
                    java.lang.Object receivedObject = inputStream.readObject();

                    if (receivedObject == null) {
                        System.out.println("No object sent from active player");
                        game.handleDisconnect(player);
                        break;
                    } else if (receivedObject instanceof PlayerMovedGameEvent) {
                        System.out.println("Received PlayerMovedGameEvent from " + player.getNick());
                        moveTimer.cancel();
                        PlayerMovedGameEvent event = (PlayerMovedGameEvent) receivedObject;
                        if (game.isLegalMove(event, player)) {
                            PlayerMoveBroadcastGameEvent pmbge = game.createSpriteDisplayInfo(event, player);
                            player.addMoveHistory(event.getMove());
                            player.removeBoosts();
                            game.broadcast(pmbge);
                            game.handleNextTurn();
                        } else {
                            System.out.println("Illegal move! Player: " + player.getNick());
                            System.out.println("Player head: " + Arrays.toString(player.getSnakeHead()));
                            System.out.println("Attempted move: " + Arrays.toString(event.getMove()));
                            game.handleDisconnect(player);
                            break;
                        }
                    } else if (receivedObject instanceof PlayerResponseEvent) {
                        System.out.println("Received PlayerResponseEvent from " + player.getNick());
                        try {
                            responseTimer.cancel();
                        } catch (Exception e) {}
                    } else {
                        System.out.println("Wrong object from client");
                        game.handleDisconnect(player);
                        break;
                    }
                } catch (IOException e) {
                    moveTimer.cancel();
                    responseTimer.cancel();
                    System.err.println("IOException");
                    game.sendLog("WARNING", String.format("IOException for (%s) %s.", player.getSocket().getInetAddress(), player.getNick()));
                    if (e instanceof EOFException) {
                        System.err.println("EOF: Client closed the connection.");
                        game.sendLog("WARNING", String.format("EOFException for (%s) %s. Client closed the connection.", player.getSocket().getInetAddress(), player.getNick()));
                    } else if (e instanceof SocketException) {
                        System.err.println("SocketException: Connection reset");
                        game.sendLog("WARNING", String.format("SocketException for (%s) %s. Connection reset.", player.getSocket().getInetAddress(), player.getNick()));
                    } else {
                        game.sendLog("ERROR", String.format("%s for (%s) %s. Connection reset.", e.getMessage(), player.getSocket().getInetAddress(), player.getNick()));
                    }
                    game.handleDisconnect(player);
                    break;
                } catch (ClassNotFoundException e) {
                    game.sendLog("ERROR", String.format("ClassNotFoundException for (%s) %s. Class was not found", player.getSocket().getInetAddress(), player.getNick()));
                }
            }
            running = false;
            System.out.println("exited while loop");
        }
    }
}