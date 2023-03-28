package com.shared.game;

import com.shared.events.*;
import com.shared.player.BotLogic;
import com.shared.player.Snake;

import java.util.ArrayList;
import java.util.List;

public abstract class Game {
    private int currentTurn;
    private Preferences currentSettings;
    private List<Snake> players;
    private List<List<Integer>> tiles;
    private boolean gameOver;

    private static final int[][] movesCommon = new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static final int[][] movesDiagonal = new int[][] {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    private static final int[][] movesJump = new int[][] {{-2, 0}, {2, 0}, {0, -2}, {0, 2}};
    private static final int[][] movesKnight = new int[][] {{1, 2}, {2, 1}, {1, -2}, {2, -1}, {-1, 2}, {-2, 1}, {-1, -2}, {-2, -1}};

    public Game(Preferences currentSettings) {
        this.currentSettings = currentSettings;
        this.players = new ArrayList<>();
        this.currentTurn = -1;
        this.tiles = new ArrayList<>();
        fillTiles();
        this.gameOver = false;
    }

    public boolean arePossibleMoves(Snake player) {
        int x = player.getSnakeHead()[0];
        int y = player.getSnakeHead()[1];

        if (checkBounds(x, y, movesCommon)) return true;

        if (player.getJumpBoost() > 0) {
            if (checkBounds(x, y, movesJump)) return true;
        }

        if (player.getDiagonalBoost() > 0) {
            if (checkBounds(x, y, movesDiagonal)) return true;
        }

        if (player.getJumpBoost() > 0 && player.getDiagonalBoost() > 0) {
            if (checkBounds(x, y, movesKnight)) return true;
        }

        return false;
    }

    private boolean checkBounds(int x, int y, int[][] moves) {
        for (int[] move : moves) {
            if (x + move[0] >= 0 && x + move[0] < getCurrentSettings().fieldWith &&
                    y + move[1] >= 0 && y + move[1] < getCurrentSettings().fieldHeight) {
                if (getTiles().get(x + move[0]).get(y + move[1]) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOutOfBounds(int[] moves) {
        int eX = moves[0];
        int eY = moves[1];
        if (!this.getCurrentSettings().isPortalWalls &&
                (eX > this.getCurrentSettings().fieldWith-1 || eX < 0 ||
                        eY > this.getCurrentSettings().fieldHeight-1 || eY < 0)) {
            return true;
        }
        return false;
    }

    public boolean isLegalMove(PlayerMovedGameEvent event, Snake player) {
        int eX = event.getMove()[0];
        int eY = event.getMove()[1];
        int dX = eX - player.getSnakeHead()[0];
        int dY = eY - player.getSnakeHead()[1];

        if (isOutOfBounds(event.getMove())) {
            return false;
        }

        //Occupied check
        if (getTiles().get(eX).get(eY) == 1) {
            return false;
        }

        //Type of move
        for (int[] move : movesCommon) {
            if (move[0] == dX && move[1] == dY) {
                return true;
            }
        }

        for (int[] move : movesJump) {
            if (move[0] == dX && move[1] == dY) {
                if (player.getJumpBoost() > 0) {
                    player.setUsedJump(true);
                    return true;
                } else break;
            }
        }

        for (int[] move : movesDiagonal) {
            if (move[0] == dX && move[1] == dY) {
                if (player.getDiagonalBoost() > 0) {
                    player.setUsedDiagonal(true);
                    return true;
                } else break;
            }
        }

        for (int[] move : movesKnight) {
            if (move[0] == dX && move[1] == dY) {
                if (player.getDiagonalBoost() > 0 && player.getJumpBoost() > 0) {
                    player.setUsedDiagonal(true);
                    player.setUsedJump(true);
                    return true;
                } else break;
            }
        }
        return false;
    }

    public PlayerMoveBroadcastGameEvent createSpriteDisplayInfo(PlayerMovedGameEvent event, Snake player) {
        int bodySprite = -1;
        int headSprite = -1;
        int bodyRotation = player.getSnakeDirection();
        int headDirection = -1;
        int bodyMirror = 0;
        int[] bodyCoords = player.getSnakeHead();
        int diffX = event.getMove()[0] - bodyCoords[0];
        int diffY = event.getMove()[1] - bodyCoords[1];

        //Head
        if (player.isUsedJump() && player.isUsedDiagonal()) {
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (diffY > 0) {
                    headDirection = 2;
                } else if (diffY < 0) {
                    headDirection = 0;
                }
            } else if (Math.abs(diffY) > Math.abs(diffX)) {
                if (diffX > 0) {
                    headDirection = 1;
                } else if (diffX < 0) {
                    headDirection = 3;
                }
            }
        } else if (player.isUsedDiagonal()) {
            System.out.println("DIAGONAL USED");
            int newRotation = -1;
            if (diffX == -1 && diffY == -1) {
                if (bodyRotation == 0) {
                    newRotation = 3;
                } else if (bodyRotation == 1) {
                    newRotation = 3;
                } else if (bodyRotation == 2) {
                    newRotation = 0;
                } else if (bodyRotation == 3) {
                    newRotation = 0;
                }
            } else if (diffX == 1 && diffY == -1) {
                if (bodyRotation == 0) {
                    newRotation = 1;
                } else if (bodyRotation == 1) {
                    newRotation = 0;
                } else if (bodyRotation == 2) {
                    newRotation = 0;
                } else if (bodyRotation == 3) {
                    newRotation = 1;
                }
            } else if (diffX == -1 && diffY == 1) {
                if (bodyRotation == 0) {
                    newRotation = 2;
                } else if (bodyRotation == 1) {
                    newRotation = 3;
                } else if (bodyRotation == 2) {
                    newRotation = 3;
                } else if (bodyRotation == 3) {
                    newRotation = 2;
                }
            } else if (diffX == 1 && diffY == 1) {
                if (bodyRotation == 0) {
                    newRotation = 2;
                } else if (bodyRotation == 1) {
                    newRotation = 2;
                } else if (bodyRotation == 2) {
                    newRotation = 1;
                } else if (bodyRotation == 3) {
                    newRotation = 1;
                }
            }
            headDirection = newRotation;
        } else {
            if (diffX > 0) {
                headDirection = 1;
            } else if (diffX < 0) {
                headDirection = 3;
            } else if (diffY > 0) {
                headDirection = 2;
            } else if (diffY < 0) {
                headDirection = 0;
            }
        }

        //TODO check all possible moves including consecutive

        if (player.getSnakeBuried() == 1) {
            if (player.isUsedDiagonal() || player.isUsedJump()) {
                bodySprite = 3;
                headSprite = 1;
            } else {
                bodySprite = 2;
                bodyRotation = (headDirection + 2) % 4;
                headSprite = 0;
                player.setSnakeBuried(0);
            }
        } else if (player.getSnakeBuried() == 0) {
            if (player.isUsedDiagonal() || player.isUsedJump()) {
                player.setSnakeBuried(1);
                bodySprite = 2;
                headSprite = 1;
            } else {
                headSprite = 0;
                if (headDirection - bodyRotation == 0) {
                    bodySprite = 0;
                } else if (headDirection - bodyRotation == 1 || headDirection - bodyRotation == -3) {
                    bodySprite = 1;
                } else if (headDirection - bodyRotation == -1 || headDirection - bodyRotation == 3) {
                    bodySprite = 1;
                    bodyMirror = 1;
                } else {
                    System.out.println("headD " + headDirection + " bodyR " + bodyRotation);
                }
            }
        }

        //Update board and snake head
        acceptMove(event.getMove()[0], event.getMove()[1]);
        player.setSnakeHead(new int[] {event.getMove()[0], event.getMove()[1]});
        player.setSnakeDirection(headDirection);

        int[] sprites = new int[] {bodySprite, headSprite};
        int[] rotations = new int[] {bodyRotation, headDirection};

        PlayerMoveBroadcastGameEvent pmbge = new PlayerMoveBroadcastGameEvent(player.getPlayerNum(), bodyCoords, event.getMove(), sprites, rotations, bodyMirror);
        return pmbge;
    }

    public void acceptMove(int x, int y) {
        this.tiles.get(x).set(y, 1);
    }

    public void fillTiles() {
        for (int i = 0; i < getCurrentSettings().fieldWith; i++) {
            List<Integer> innerList = new ArrayList<Integer>();
            for (int j = 0; j < getCurrentSettings().fieldHeight; j++) {
                innerList.add(0); // add a default value of 0
            }
            tiles.add(innerList); // add the inner list to the outer ArrayList
        }

    }

    public void broadcast(Event event) {
        for (Snake player : getPlayers()) {
            if (player instanceof BotLogic) continue;
            player.sendEvent(event);
        }
    }

    public void addPlayer(Snake player) {
        getPlayers().add(player);
        //tell all clients a new client has joined and how many are in the game already
        player.sendEvent(new GameEnteredEvent(this.getPlayers().size(), this.getCurrentSettings().playersNum));
        broadcast(new GameJoinedEvent());
        if (getCurrentSettings().playersNum == getPlayers().size()) {
            startGame();
        }
    }

    public void nextPlayer() {

    }

    public List<Snake> getPlayers() {
        return players;
    }

    public void setPlayers(List<Snake> players) {
        this.players = players;
    }

    public void removePlayer(Snake player) {
        this.players.remove(player);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }

    public Preferences getCurrentSettings() {
        return currentSettings;
    }

    public void setCurrentSettings(Preferences currentSettings) {
        this.currentSettings = currentSettings;
    }

    public List<List<Integer>> getTiles() {
        return tiles;
    }

    public void setTiles(List<List<Integer>> tiles) {
        this.tiles = tiles;
    }

    public boolean isValidMove(PlayerMovedGameEvent pmge) {
        return true;
    }

    public abstract void startGame();
}
