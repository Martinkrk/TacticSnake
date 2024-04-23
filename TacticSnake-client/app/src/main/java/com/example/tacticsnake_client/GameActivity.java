package com.example.tacticsnake_client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.tacticsnake_client.events.EventManager;
import com.example.tacticsnake_client.events.HotseatEventManager;
import com.example.tacticsnake_client.managers.CustomAudioManager;
import com.example.tacticsnake_client.singleton.AppSingleton;
import com.shared.events.GameInitiatedEvent;
import com.shared.events.PlayerMoveBroadcastGameEvent;
import com.shared.events.PlayerMovedGameEvent;
import com.shared.game.GameSettings;
import com.shared.player.PlayerInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GameActivity extends AppCompatActivity {
    private EventManager eventManager;
    private Preferences preferences;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor preferencesEditor;

    //AUDIO
    private CustomAudioManager cam;
    private Button game_soundToggle;
    private SeekBar game_volume_seekbar;

    private TextView game_event_text;
    private TextView game_top_text;
    private Button diagonalBooster;
    private Button jumpBooster;
    private Button makeamove;
    private int diagonalBoosterLeft = 1;
    private int jumpBoosterLeft = 1;
    private int diagonalState;
    private int jumpState;
    private BoardView boardView;
    private int[] players = new int[] {};
    private long mLastClickTime = 0;
    // Player elements
    public HashMap<Integer, LinearLayout> player_layouts = new HashMap<>();
    public HashMap<Integer, ProgressBar> player_bars = new HashMap<>();
    public ScheduledExecutorService executor;
    public AtomicInteger countdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        //EventManager
        try {
            eventManager = (EventManager) getIntent().getExtras().getSerializable("eventManager");
        } catch (NullPointerException e) {
        }

        if (eventManager == null) {
            eventManager = AppSingleton.getInstance().getOnlineEventManager();
        }

        if (eventManager == null) {
            eventManager = new HotseatEventManager(new GameSettings(), new HotseatGame(new GameSettings()));
        }

        eventManager.setGameActivity(this);

        //Boardview
        boardView = findViewById(R.id.board_view);
        boardView.setBoardSize(eventManager.getPreferences().fieldHeight, eventManager.getPreferences().fieldWidth);

        jumpBooster = findViewById(R.id.game_booster_long);
        makeamove = findViewById(R.id.game_play_move);
        diagonalBooster = findViewById(R.id.game_booster_diagonal);

        // Get the screen width
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // Change the height and width of the BoardView to match the screen width
        ViewGroup.LayoutParams layoutParams = boardView.getLayoutParams();
        int boardSize = Math.min(screenWidth, screenHeight);
        layoutParams.width = boardSize;
        layoutParams.height = boardSize;

        boardView.setLayoutParams(layoutParams);

        //Preferences
        preferences = new Preferences();
        sharedPreferences = getSharedPreferences("preferences", MODE_PRIVATE);
        preferences.initialize(sharedPreferences);

        // AUDIO
        cam = new CustomAudioManager(this, (AudioManager) getSystemService(AUDIO_SERVICE));

        game_soundToggle = findViewById(R.id.mainMenu_soundToggle);
        game_volume_seekbar = findViewById(R.id.game_volume_seekbar);
        game_volume_seekbar.setMax(cam.getMaxVolume());

        //Mute or set volume based on preferences
        cam.setCurrentVolume(cam.getAm().getStreamVolume(AudioManager.STREAM_MUSIC));
        if (preferences.volume_muted == 1) {
            muteSounds();
        } else {
            game_volume_seekbar.setProgress(cam.getCurrentVolume());
        }

        //Displays
        game_event_text = findViewById(R.id.game_event_text);
        game_top_text = findViewById(R.id.game_top_text);

        //Player bars
        player_layouts.put(0, (LinearLayout) findViewById(R.id.player1_bar));
        player_layouts.put(1, (LinearLayout) findViewById(R.id.player2_bar));
        player_layouts.put(2, (LinearLayout) findViewById(R.id.player3_bar));
        player_layouts.put(3, (LinearLayout) findViewById(R.id.player4_bar));
        for (int i=0; i<4; i++) {
            if (i+1 <= preferences.playersNum) {
                player_layouts.get(i).setVisibility(View.VISIBLE);
            } else {
                player_layouts.get(i).setVisibility(View.INVISIBLE);
            }
        }

        //Turn Timers
        player_bars.put(0, (ProgressBar) findViewById(R.id.avatar1_progressBar));
        player_bars.put(1, (ProgressBar) findViewById(R.id.avatar2_progressBar));
        player_bars.put(2, (ProgressBar) findViewById(R.id.avatar3_progressBar));
        player_bars.put(3, (ProgressBar) findViewById(R.id.avatar4_progressBar));

        for (int i=0; i<4; i++) {
            player_bars.get(i).setVisibility(View.INVISIBLE);
            player_bars.get(i).setMax(preferences.moveTimer);
        }

        //setup game, board, players
        try {
            players = getIntent().getExtras().getIntArray("players");
        } catch (Exception e) {
            Log.d("EXCEPTION", String.valueOf(e));
        }
        eventManager.setup();
        cam.getSp().play(cam.getSounds().get("start"), 1, 1, 0, 0, 1);
    }

    @Override
    public void onBackPressed() {
        cancelGameAlertBox("Are you sure you want to leave? You will automatically lose the match.", "Forfeit the game", "Leave", "Cancel");
    }

    public void updatePlayerTurnProgress(int who) {
        runOnUiThread(() -> {
            for (int i = 0; i < 4; i++) {
                player_bars.get(i).setVisibility(View.INVISIBLE);
                player_bars.get(i).setMax(preferences.moveTimer);
            }
            player_bars.get(who).setVisibility(View.VISIBLE);
        });
    }

    public int[] getPlayers() {
        return players;
    }

    public void setStartingSnakes() {
        GameInitiatedEvent gie = null;
        try {
            gie = (GameInitiatedEvent) getIntent().getExtras().getSerializable("gameInitiatedEvent");
        } catch (NullPointerException e) {
            System.err.println(e);
        }

        if (gie == null) {
            gie = new GameInitiatedEvent(((HotseatEventManager)eventManager).getGame().generateHotseatSnakes());
        }

        for (PlayerInfo pi : gie.getPlayers()) {
            boardView.placeSnake(pi.headPos, pi.headRotation, pi.snakeColor, 0, 0);
            if (pi.playerNum == eventManager.getPlayerNum()) {
                boardView.setSnakeHead(new Point(pi.headPos[0], pi.headPos[1]));
            }
        }
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.game_booster_diagonal) {
            diagonalState = Math.abs(diagonalState - 1);
            updateBoosterButton(id, diagonalState);
        } else if (id == R.id.game_booster_long) {
            jumpState = Math.abs(jumpState - 1);
            updateBoosterButton(id, jumpState);
        } else if (id == R.id.game_play_move) {
            toggleBoard(false);
            boardView.setMyTurn(false);

            // Create a new thread to send the game event over the network
            new Thread(() -> eventManager.sendEvent(new PlayerMovedGameEvent(new int[] {boardView.getmSelectedColumn(), boardView.getmSelectedRow()}))).start();

            if (diagonalState == 1) diagonalBoosterLeft -= 1;
            if (diagonalBoosterLeft == 0) {
                disableBoosterButton(diagonalBooster);
                diagonalState = 0;
            }
            if (jumpState == 1) jumpBoosterLeft -= 1;
            if (jumpBoosterLeft == 0) {
                disableBoosterButton(jumpBooster);
                jumpState = 0;
            }

            boardView.evaluateValidMoves(diagonalState, jumpState);
            boardView.changeMoveBtnStyle();
        }
        if (SystemClock.elapsedRealtime() - mLastClickTime < 250){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        cam.getSp().play(cam.getSounds().get("click"), 1, 1, 0, 0, 1);

        switch (v.getId()) {
            // AUDIO TOGGLE
            case R.id.game_soundToggle:
                preferences.volume_muted ^= 1;
                if (preferences.volume_muted == 1) {
                    muteSounds();
                } else {
                    unmuteSounds();
                }
                updateVolumeMute(preferences.volume_muted);
                break;
            case R.id.game_forfeit:
                cancelGameAlertBox("Are you sure you want to leave? You will automatically lose the match.", "Forfeit the game", "Leave", "Cancel");
                break;
        }
    }

    private void updateVolumeMute(int m) {
        preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putInt("volume_muted", m);
        preferencesEditor.apply();
    }

    public void muteSounds() {
        game_volume_seekbar.setProgress(0);
        cam.muteSounds();
        game_soundToggle.setBackgroundResource(R.mipmap.menu_btn_small_gray);
        game_soundToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sound_off_black, 0, 0, 0);
    }

    public void unmuteSounds() {
        game_volume_seekbar.setProgress(cam.getSavedVolume());
        cam.unmuteSounds();
        game_soundToggle.setBackgroundResource(R.mipmap.menu_btn_small_yellow);
        game_soundToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sound_on, 0, 0, 0);
    }

    public void playSoundPing() {
        cam.getSp().play(cam.getSounds().get("ping"), 1, 1, 0, 0, 1);
    }

    public void displayEventLog(String text) {
        runOnUiThread(() -> game_event_text.setText(text));
    }

    public void startMoveTimer(int moveTimer, int player) {
        try {
            executor.shutdown();
        } catch (Exception e) {}
        executor = Executors.newScheduledThreadPool(1);
        countdown = new AtomicInteger(moveTimer);
        Runnable countdownRunnable = () -> {
            int currentCount = countdown.getAndDecrement();
            if (currentCount >= 0) {
                runOnUiThread(() -> {
                    updateTimerCount(currentCount);
                    updatePlayerTurnTimer(player, currentCount);
                });
            } else {
                executor.shutdown();
            }
        };

        // Schedule the countdown task to execute every second
        executor.scheduleAtFixedRate(countdownRunnable, 0, 1, TimeUnit.SECONDS);
    }

    public void stopMoveTimer() {
        executor.shutdown();
    }

    private void updateBoosterButton(int id , int state) {
        if (id == R.id.game_booster_long) {
            if (state == 0) updateBtn(jumpBooster, R.mipmap.menu_btn_smedium_blue_hollow, R.color.black);
            else updateBtn(jumpBooster, R.mipmap.menu_btn_smedium_blue, R.color.white);
        } else if (id == R.id.game_booster_diagonal) {
            if (state == 0) updateBtn(diagonalBooster, R.mipmap.menu_btn_smedium_magenta_hollow, R.color.black);
            else updateBtn(diagonalBooster, R.mipmap.menu_btn_smedium_magenta, R.color.white);
        }
        boardView.evaluateValidMoves(diagonalState, jumpState);
        boardView.changeMoveBtnStyle();
    }

    private void disableBoosterButton(Button boosterButton) {
        boosterButton.setEnabled(false);
        updateBtn(boosterButton, R.mipmap.menu_btn_white, R.color.black);
    }

    public void updateBoosterButtonsEvent(int diagonal, int jump) {
        runOnUiThread(() -> {
            setDiagonalBoosterLeft(diagonal);
            if (diagonalBoosterLeft > 0) {
                diagonalBooster.setEnabled(true);
                diagonalState = 0;
                updateBoosterButton(R.id.game_booster_diagonal, diagonalState);
            } else {
                disableBoosterButton(diagonalBooster);
            }

            setJumpBoosterLeft(jump);
            if (jumpBoosterLeft > 0) {
                jumpBooster.setEnabled(true);
                jumpState = 0;
                updateBoosterButton(R.id.game_booster_long, jumpState);
            } else {
                disableBoosterButton(jumpBooster);
            }
        });
    }

    public void setMoveBtnState(boolean active) {
        makeamove.setEnabled(active);
        if (active) updateBtn(makeamove, R.mipmap.menu_btn_yellow, R.color.white);
        if (!active) updateBtn(makeamove, R.mipmap.menu_btn_white, R.color.black);
    }

    public void updateBtn(Button b, int background_image, int text_color) {
        b.setBackground(getDrawable(background_image));
        b.setTextColor(getResources().getColor(text_color));
    }

    public void toggleBoard(boolean active) {
        boardView.setDiagonalState(diagonalState);
        boardView.setJumpState(jumpState);
        boardView.setMyTurn(active);
    }

    public void updateSnake(PlayerMoveBroadcastGameEvent event) {
        boardView.placeSnake(event.getBodyXY(), event.getRotations()[0], event.getSnakeColor(), event.getSprites()[0], event.getBodyturnMirror());
        boardView.placeSnake(event.getHeadXY(), event.getRotations()[1], event.getSnakeColor(), event.getSprites()[1], 0);
    }

    public void setSnakePos(int[] pos) {
        boardView.setSnakeHead(new Point(pos[0], pos[1]));
    }

    public void removeSnake(List<int[]> moves, int headRotation, int[] snakeColor, int snakeBuried) {
        boardView.placeDeadHead(moves.get(moves.size()-1), headRotation, snakeColor, snakeBuried);
        boardView.removeSnake(moves);
    }

    public void updatePlayerTurnTimer(int player, int moveTimer) {
        player_bars.get(player).setProgress(moveTimer);
        player_bars.get(player).setVisibility(View.VISIBLE);
    }

    public void updateTimerCount(int moveTimer) {
        game_top_text.setText(String.valueOf(moveTimer));
    }

    public void setDiagonalBoosterLeft(int diagonalBoosterLeft) {
        this.diagonalBoosterLeft = diagonalBoosterLeft;
    }

    public void setJumpBoosterLeft(int jumpBoosterLeft) {
        this.jumpBoosterLeft = jumpBoosterLeft;
    }

    public void setDiagonalState(int diagonalState) {
        this.diagonalState = diagonalState;
    }

    public void setJumpState(int jumpState) {
        this.jumpState = jumpState;
    }

    public void alertBoxEvent(String message, String title, String button) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
            builder.setMessage(message);
            builder.setTitle(title);
            builder.setNegativeButton(button, (dialog, which) -> {
                cancelGame("", "");
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    public void alertBox(String message, String title, String button) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setNegativeButton(button, (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void cancelGameAlertBox(String message, String title, String confirmBtn, String cancelBtn) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton(confirmBtn, (dialog, which) -> {
           cancelGame("", "");
        });
        builder.setNeutralButton(cancelBtn, (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void cancelGame(String errorTitle, String errorDesc) {
        eventManager.close();
        Intent intent = new Intent(this, MainActivity.class);
        if (!TextUtils.isEmpty(errorTitle)) {
            intent.putExtra("errorTitle", errorTitle);
            intent.putExtra("errorDesc", errorDesc);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}