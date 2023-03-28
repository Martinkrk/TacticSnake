package server;

import com.shared.events.*;
import game.OnlineGame;
import com.shared.game.Preferences;
import game.OnlinePlayer;
import gui.ServerGUI;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Server {
    private final int port = 8080;
    private Socket socket;
    private ServerSocket serverSocket;
    private List<OnlineGame> games;
    private int gameIdCounter;
    private final ServerGUI serverGUI;

    public Server() {
        this.games = new LinkedList<>();
        this.gameIdCounter = 0;

        this.serverGUI = new ServerGUI();
        startServer();
    }

    public void startServer() {
        serverGUI.appendLog("hello!");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());

                Preferences preferences = (Preferences) input.readObject();
                System.out.println("Received game settings from client: " + preferences);

                //Tell client server is trying to find or create a game for client
                sendEvent(output, new GameJoiningEvent());

                OnlineGame game = findOrCreateGame(preferences); //TODO Change code when finding a private game
                System.out.printf("Game found or created. Games: %d%n", games.size());
                if (game == null) {

                    //Tell client there is no private game with such game room code
                    sendEvent(output, new GameInvalidEvent());

                    System.out.println("No such private game - null returned");
                    clientSocket.close();
                    continue;
                }

                OnlinePlayer player = new OnlinePlayer(game, preferences.nick, output, clientSocket);
                game.addPlayer(player);

                ClientHandler clientHandler = new ClientHandler(clientSocket, input, output, player);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();

            }
        } catch (IOException e) {
            System.out.println("IOException in main server loop");
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException");
        }
    }

    public void stopServer() {
        // TODO stop when GUI is closed
    }

    private void sendEvent(ObjectOutputStream out, Event event) {
        try {
            out.writeObject(event);
            out.flush();
        } catch (IOException e) {
            System.out.println("Couldn't send event to client " + this);
        }
    }

    private OnlineGame findOrCreateGame(Preferences preferences) {
        for (OnlineGame g : games) {
            if (canJoin(g, preferences)) {
                return g;
            }
        }

        System.out.println("Game created");
        OnlineGame game = new OnlineGame(preferences, this);
        games.add(game);
        return game;
    }

    private boolean canJoin(OnlineGame g, Preferences gs) {
        if (g.isInSession()) {
            System.out.println("Already in session");
            return false;
        }

        //TODO CREATE A SEPARATE MESSAGE WHEN A GAME IS FULL
        if (g.getCurrentSettings().fieldWith != gs.fieldWith ||
                g.getCurrentSettings().fieldHeight != gs.fieldHeight ||
                g.getCurrentSettings().isPortalWalls != gs.isPortalWalls ||
                g.getCurrentSettings().isCorpse != gs.isCorpse ||
                g.getCurrentSettings().playersNum != gs.playersNum ||
                g.getPlayers().size() >= g.getCurrentSettings().playersNum ||
                g.getCurrentSettings().isPrivate != gs.isPrivate) {
            System.out.println("settings don't match");
            return false;
        }

        if (gs.isPrivate && !gs.gameRoom.equals(g.getCurrentSettings().gameRoom)) {
            return false;
        }

        System.out.println("game found");
        return true;
    }

    public void removeGame(OnlineGame game) {
        this.games.remove(game);
        System.out.printf("Game removed. Games: %d%n", games.size());
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

        public ClientHandler(Socket socket, ObjectInputStream inputStream, ObjectOutputStream outputStream, OnlinePlayer player) {
            this.socket = socket;
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            this.player = player;
            this.game = (OnlineGame) player.getAssignedGame();
        }
        @Override
        public void run() {
            System.out.println("Thread started successfully for " + player.getName());
            while (true) {
                try {
                    Object receivedObject = null;
                    if (player.getPlayerNum() == game.getCurrentTurn() && game.isInSession()) {
                        socket.setSoTimeout(60000);

                        receivedObject = inputStream.readObject();

                        if (receivedObject == null) {
                            System.out.println("No object sent from active player");
                            break;
                        } else if (receivedObject instanceof PlayerMovedGameEvent) {
                            System.out.println("Received PlayerMovedGameEvent");
                            PlayerMovedGameEvent event = (PlayerMovedGameEvent) receivedObject;
                            if (game.isLegalMove(event, player)) {
                                PlayerMoveBroadcastGameEvent pmbge = game.createSpriteDisplayInfo(event, player);
                                player.addMoveHistory(event.getMove());
                                player.removeBoosts();
                                game.broadcast(pmbge);
                                game.nextPlayer();
                            } else {
                                System.out.println("Illegal move! Player: " + player.getName());
                                System.out.println("Player head: " + Arrays.toString(player.getSnakeHead()));
                                System.out.println("Attempted move: " + Arrays.toString(event.getMove()));
                                break;
                            }
                        } else if (receivedObject instanceof PlayerTimeoutGameEvent) {
                            System.out.println("Client told us the player didn't make a move in time");
                            break;
                        } else {
                            System.out.println("Wrong object sent");
                            //TODO do something if client provided a wrong object
                        }
                    } else if (player.getPlayerNum() != game.getCurrentTurn()) {
                        socket.setSoTimeout(10000);

                        receivedObject = inputStream.readObject();

                        if (receivedObject == null) {
                            System.out.println("No object sent from inactive player");
                            break;
                        } else if (receivedObject instanceof PlayerResponseEvent) {
                            System.out.println("Received PlayerResponseEvent from " + player.getName());
                        } else {
                            System.out.println("Wrong object from inactive client");
                        }
                    }
                } catch (SocketTimeoutException e) {
                    System.err.println("Timeout: No data received from client.");
                    break;
                } catch (IOException e) {
                    System.err.println("IOException");
                    if (e instanceof EOFException) {
                        System.err.println("EOF: Client closed the connection.");
                    } else if (e instanceof SocketException) {
                        System.err.println("SocketException: Connection reset");
                    } else {
                        System.err.println(e);
                    }
                    break;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Broke from while loop");
            try {
                socket.close();
                outputStream.close();
                inputStream.close();
                System.out.println("socket and streams closed");
                game.processDisconnect(player);
            } catch (IOException e) {
                System.out.println("couldn't close socket or streams");
                // Handle exception
            } finally {
                System.out.println("done");
            }
        }
    }
}