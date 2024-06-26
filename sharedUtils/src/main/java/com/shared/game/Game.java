package com.shared.game;

import com.shared.events.*;
import com.shared.player.BotLogic;
import com.shared.player.Snake;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Game implements Serializable {
    private int currentTurn;
    private GameSettings currentSettings;
    private List<Snake> players;
    private List<List<Integer>> tiles;
    private boolean gameOver;

    //starting positions and rotations
    private static int[][] startingPos;

    //Valid moves
    private static final int[][] movesCommon = new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static final int[][] movesDiagonal = new int[][] {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    private static final int[][] movesJump = new int[][] {{-2, 0}, {2, 0}, {0, -2}, {0, 2}};
    private static final int[][] movesKnight = new int[][] {{1, 2}, {2, 1}, {1, -2}, {2, -1}, {-1, 2}, {-2, 1}, {-1, -2}, {-2, -1}};

    public Game(GameSettings currentSettings) {
        this.currentSettings = currentSettings;
        this.players = new ArrayList<>();
        this.currentTurn = -1;
        this.tiles = new ArrayList<>();
        fillTiles();
        this.gameOver = false;
        startingPos = new int[][] {{0, 0, 90}, {currentSettings.fieldWidth -1, currentSettings.fieldHeight-1, 270},
                {currentSettings.fieldWidth -1, 0, 180}, {0, currentSettings.fieldHeight-1, 0}};
    }

    public boolean arePossibleMoves(Snake player) {
        int x = player.getSnakeHead()[0];
        int y = player.getSnakeHead()[1];

        if (checkLegalInBounds(x, y, movesCommon)) return true;

        if (player.getJumpBoost() > 0) {
            if (checkLegalInBounds(x, y, movesJump)) return true;
        }

        if (player.getDiagonalBoost() > 0) {
            if (checkLegalInBounds(x, y, movesDiagonal)) return true;
        }

        if (player.getJumpBoost() > 0 && player.getDiagonalBoost() > 0) {
            if (checkLegalInBounds(x, y, movesKnight)) return true;
        }

        return false;
    }

    private boolean checkLegalInBounds(int x, int y, int[][] moves) {
        for (int[] move : moves) {
            if (isMoveLegal(x, y, move)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<int[]> generateValidMoves(int x, int y, int[][] moves) {
        ArrayList<int[]> validMoves = new ArrayList<>();
        for (int[] move : moves) {
            if (isMoveLegal(x, y, move)) {
                validMoves.add(new int[] {x + move[0], y + move[1]});
            }
        }
        return validMoves;
    }

    private boolean isOutOfBounds(int[] moves) {
        int eX = moves[0];
        int eY = moves[1];
        if (!this.getCurrentSettings().isPortalWalls &&
                (eX > this.getCurrentSettings().fieldWidth -1 || eX < 0 ||
                        eY > this.getCurrentSettings().fieldHeight-1 || eY < 0)) {
            return true;
        }
        return false;
    }

    private boolean isMoveLegal(int x, int y, int[] move) {
        int newX = x + move[0];
        int newY = y + move[1];
        return newX >= 0 && newX < getCurrentSettings().fieldWidth &&
                newY >= 0 && newY < getCurrentSettings().fieldHeight &&
                getTiles().get(newY).get(newX) == 0;
    }

    public boolean isLegalMove(PlayerMovedGameEvent event, Snake player) {
        int eX = event.getMove()[0];
        int eY = event.getMove()[1];
        int dX = eX - player.getSnakeHead()[0];
        int dY = eY - player.getSnakeHead()[1];

        if (isOutOfBounds(event.getMove())) {
            return false;
        }

        // Occupation check
        if (getTiles().get(eY).get(eX) == 1) { // 1 = occupied space
            return false;
        }

        // Check if the move is a valid snake move
        // Check for common moves
        for (int[] move : movesCommon) {
            if (move[0] == dX && move[1] == dY) {
                return true;
            }
        }

        // Check for jump move
        if (player.getJumpBoost() > 0) {
            for (int[] move : movesJump) {
                if (move[0] == dX && move[1] == dY) {
                    player.setUsedJump(true);
                    return true;
                }
            }
        }

        // Check for diagonal move
        if (player.getDiagonalBoost() > 0) {
            for (int[] move : movesDiagonal) {
                if (move[0] == dX && move[1] == dY) {
                    player.setUsedDiagonal(true);
                    return true;
                }
            }
        }

        // Check for knight move
        if (player.getJumpBoost() > 0 && player.getDiagonalBoost() > 0) {
            for (int[] move : movesKnight) {
                if (move[0] == dX && move[1] == dY) {
                    player.setUsedDiagonal(true);
                    player.setUsedJump(true);
                    return true;
                }
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

        if (player.getSnakeBuried() == 1) {
            if (player.isUsedDiagonal() || player.isUsedJump()) {
                bodySprite = 5;
                headSprite = 0;
            } else {
                bodySprite = 4;
                bodyRotation = (headDirection + 2) % 4;
                headSprite = 1;
                player.setSnakeBuried(0);
            }
        } else if (player.getSnakeBuried() == 0) {
            if (player.isUsedDiagonal() || player.isUsedJump()) {
                player.setSnakeBuried(1);
                bodySprite = 4;
                headSprite = 0;
            } else {
                headSprite = 1;
                if (headDirection - bodyRotation == 0) {
                    bodySprite = 2;
                } else if (headDirection - bodyRotation == 1 || headDirection - bodyRotation == -3) {
                    bodySprite = 3;
                } else if (headDirection - bodyRotation == -1 || headDirection - bodyRotation == 3) {
                    bodySprite = 3;
                    bodyRotation+=1;
                } else {
                }
            }
        }

        //Update board and snake head
        acceptMove(event.getMove()[0], event.getMove()[1]);
        player.setSnakeHead(new int[] {event.getMove()[0], event.getMove()[1]});
        player.setSnakeDirection(headDirection);

        int[] sprites = new int[] {bodySprite, headSprite};
        int[] rotations = new int[] {bodyRotation*90, headDirection*90};

        PlayerMoveBroadcastGameEvent pmbge = new PlayerMoveBroadcastGameEvent(player.getPlayerNum(), player.getNick(), bodyCoords, event.getMove(), sprites, rotations, bodyMirror, player.getSnakeColor());
        return pmbge;
    }

    public void movesRemove(List<int[]> moves) {
        for (int[] move : moves) {
            tiles.get(move[1]).set(move[0], 0);
        }
    }

    public ArrayList<int[]> getValidMoves(int[] snakeHead, boolean commonMoves, boolean jumpMoves, boolean diagonalMoves, boolean knightMoves) {
        ArrayList<int[]> moves = new ArrayList<>();

        if (commonMoves) moves.addAll(generateValidMoves(snakeHead[0], snakeHead[1], movesCommon));
        if (jumpMoves) moves.addAll(generateValidMoves(snakeHead[0], snakeHead[1], movesJump));
        if (diagonalMoves) moves.addAll(generateValidMoves(snakeHead[0], snakeHead[1], movesDiagonal));
        if (knightMoves) moves.addAll(generateValidMoves(snakeHead[0], snakeHead[1], movesKnight));

        return moves;
    }

    public void acceptMove(int x, int y) {
        this.tiles.get(y).set(x, 1);
    }

    public void fillTiles() {
        for (int i = 0; i < getCurrentSettings().fieldWidth; i++) {
            List<Integer> innerList = new ArrayList<>();
            for (int j = 0; j < getCurrentSettings().fieldHeight; j++) {
                innerList.add(0);
            }
            tiles.add(innerList);
        }
        for (int i = 0; i < players.size(); i++) {
            tiles.get(startingPos[i][1]).set(startingPos[i][0], 1);
        }
    }

    abstract public void addPlayer(Snake player);

    abstract public void handleNextTurn();

    public List<Snake> getPlayers() {
        return players;
    }

    public void setPlayers(List<Snake> players) {
        this.players = players;
    }

    public void removePlayer(Snake player) {
        players.remove(player);
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

    public GameSettings getCurrentSettings() {
        return currentSettings;
    }

    public void setCurrentSettings(GameSettings currentSettings) {
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

    public static int[][] getStartingPos() {
        return startingPos;
    }

    public abstract void startGame();
}
