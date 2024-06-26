package com.shared.player;

import com.shared.events.PlayerMovedGameEvent;
import com.shared.game.Game;

import java.util.ArrayList;
import java.util.List;

public abstract class Snake {
    private final Game assignedGame;
    private int playerNum;
    private String nick;
    private int[] snakeHead;
    private Integer snakeDirection;
    private Integer snakeBuried;
    private boolean isDead;
    private boolean isDisconnected;
    private List<int[]> moveHistory;
    private int diagonalBoost;
    private int jumpBoost;
    private boolean usedDiagonal;
    private boolean usedJump;
    private int[] snakeColor = new int[3];

    public Snake(Game assignedGame, String nick) {
        this.assignedGame = assignedGame;
        this.nick = nick;
        this.isDead = false;
        this.isDisconnected = false;
        this.snakeBuried = 0;
        this.moveHistory = new ArrayList<>();
        this.diagonalBoost = 1;
        this.jumpBoost = 1;
        this.usedDiagonal = false;
        this.usedJump = false;
    }

    public void removeBoosts() {
        if (usedDiagonal) diagonalBoost -= 1;
        if (usedJump) jumpBoost -= 1;
    }

    public Game getAssignedGame() {
        return assignedGame;
    }

    public int[] evaluateSnakeHeadPos(int playerNum) {
        return new int[] {0, 0};
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public void setPlayerNum(int playerNum) {
        this.playerNum = playerNum;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int[] getSnakeHead() {
        return snakeHead;
    }

    public void setSnakeHead(int[] snakeHead) {
        this.snakeHead = snakeHead;
    }

    public Integer getSnakeDirection() {
        return snakeDirection;
    }

    public void setSnakeDirection(Integer snakeDirection) {
        this.snakeDirection = snakeDirection;
    }

    public Integer getSnakeBuried() {
        return snakeBuried;
    }

    public void setSnakeBuried(Integer snakeBuried) {
        this.snakeBuried = snakeBuried;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public boolean isDisconnected() {
        return isDisconnected;
    }

    public void setDisconnected(boolean disconnected) {
        isDisconnected = disconnected;
    }

    public List<int[]> getMoveHistory() {
        return moveHistory;
    }

    public void setMoveHistory(List<int[]> moveHistory) {
        this.moveHistory = moveHistory;
    }

    public void addMoveToHistory(int[] move) {
        moveHistory.add(move);
    }

    public void addMoveHistory(int[] move) {
        getMoveHistory().add(move);
    }

    public int getDiagonalBoost() {
        return diagonalBoost;
    }

    public void setDiagonalBoost(int diagonalBoost) {
        this.diagonalBoost = diagonalBoost;
    }

    public int getJumpBoost() {
        return jumpBoost;
    }

    public void setJumpBoost(int jumpBoost) {
        this.jumpBoost = jumpBoost;
    }

    public boolean isUsedDiagonal() {
        return usedDiagonal;
    }

    public void setUsedDiagonal(boolean usedDiagonal) {
        this.usedDiagonal = usedDiagonal;
    }

    public boolean isUsedJump() {
        return usedJump;
    }

    public void setUsedJump(boolean usedJump) {
        this.usedJump = usedJump;
    }

    public int[] getSnakeColor() {
        return snakeColor;
    }

    public void setSnakeColor(int[] snakeColor) {
        this.snakeColor = snakeColor;
    }

    public abstract void sendObject(Object object);
    public abstract PlayerMovedGameEvent action();
}
