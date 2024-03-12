package com.example.tacticsnake_client;

import android.content.SharedPreferences;
import com.shared.game.GameSettings;

public class Preferences {
    //General settings
    public int volume_muted;
    //Game settings
    public int gameMode;
    public int fieldWidth;
    public int fieldHeight;
    public boolean isPortalWalls;
    public boolean isCorpse;
    public int playersNum;
    public boolean isPrivate;
    public String gameRoom;

    //Player info
    public String nick;
    public int[] snakeColor;

    public Preferences() {
         volume_muted = 0;
         gameMode = 0;
         fieldWidth = 8;
         fieldHeight = 8;
         isPortalWalls = false;
         isCorpse = false;
         playersNum = 4;
         isPrivate = false;
         gameRoom = "";
         nick = "Nameless";
         snakeColor = new int[] {255, 255, 255};
    }

    public void initialize(SharedPreferences sharedPreferences) {
        if (!sharedPreferences.getBoolean("arePreferences", false)) {
            initializeDefaultPreferences(sharedPreferences);
        } else {
            loadPreferencesFromStorage(sharedPreferences);
        }
    }

    private void initializeDefaultPreferences(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("arePreferences", true);
        editor.putInt("volume_muted", this.volume_muted);
        editor.putInt("fieldWidth", this.fieldWidth);
        editor.putInt("fieldHeight", this.fieldHeight);
        editor.putBoolean("isPortalWalls", this.isPortalWalls);
        editor.putBoolean("isCorpse", this.isCorpse);
        editor.putInt("playersNum", this.playersNum);
        editor.putString("nick", this.nick);
        editor.putInt("red", this.snakeColor[0]);
        editor.putInt("green", this.snakeColor[1]);
        editor.putInt("blue", this.snakeColor[2]);
        editor.apply();
    }

    private void loadPreferencesFromStorage(SharedPreferences sharedPreferences) {
        this.volume_muted = sharedPreferences.getInt("volume_muted", this.volume_muted);
        this.fieldWidth = sharedPreferences.getInt("fieldWidth", this.fieldWidth);
        this.fieldHeight = sharedPreferences.getInt("fieldHeight", this.fieldHeight);
        this.isPortalWalls = sharedPreferences.getBoolean("isPortalWalls", this.isPortalWalls);
        this.isCorpse = sharedPreferences.getBoolean("isCorpse", this.isCorpse);
        this.playersNum = sharedPreferences.getInt("playersNum", this.playersNum);
        this.nick = sharedPreferences.getString("nick", this.nick);
        this.snakeColor = new int[] {sharedPreferences.getInt("red", 255),
                sharedPreferences.getInt("green", 255), sharedPreferences.getInt("blue", 255)};
    }

    public GameSettings createGameSettings() {
        GameSettings gs = new GameSettings();
        gs.gameMode = this.gameMode;
        gs.fieldWidth = this.fieldWidth;
        gs.fieldHeight = this.fieldHeight;
        gs.isPortalWalls = this.isPortalWalls;
        gs.isCorpse = this.isCorpse;
        gs.playersNum = this.playersNum;
        gs.isPrivate = this.isPrivate;
        gs.gameRoom = this.gameRoom;
        gs.nick = this.nick;
        gs.snakeColor = this.snakeColor;
        return gs;
    }
}

