package com.example.tacticsnake_client;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
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
import java.util.Date;
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
        eventManager.setup();
    }

    @Override
    public void onBackPressed() {
        cancelGame("", "");
    }

    public int[] getPlayers() {
        int[] players = new int[] {1, 2, 0, 0};
        try {
            players = getIntent().getExtras().getIntArray("players");
        } catch (NullPointerException e) {
            System.err.println(e);
        }
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
                Log.d("DEBUG-pnum", String.valueOf(eventManager.getPlayerNum()));
                Log.d("DEBUG-shead", Arrays.toString(pi.headPos));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuitemexit) {
            cancelGame("", "");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.game_booster_diagonal) {
            diagonalState = Math.abs(diagonalState - 1);
            updateBoosterButton(diagonalBooster, diagonalState);
        } else if (id == R.id.game_booster_long) {
            jumpState = Math.abs(jumpState - 1);
            updateBoosterButton(jumpBooster, jumpState);
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
        Log.d("TIMERDEBUG", "I ENTERED STARTMOVETIMER!");
        Runnable countdownRunnable = () -> {
            int currentCount = countdown.getAndDecrement();
            if (currentCount >= 0) {
                Log.d("DEBUG", "currentCount = " + currentCount);
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

    private void updateBoosterButton(Button boosterButton, int state) {
        if (state == 0) {
            boosterButton.setTextColor(getResources().getColor(R.color.black));
        } else {
            boosterButton.setTextColor(getResources().getColor(R.color.dracula_fg));
        }
        boosterButton.setSelected(!boosterButton.isSelected());
        boardView.evaluateValidMoves(diagonalState, jumpState);
        boardView.changeMoveBtnStyle();
    }

    private void disableBoosterButton(Button boosterButton) {
        boosterButton.setEnabled(false);
        boosterButton.setBackground(getResources().getDrawable(R.drawable.game_booster_button_disabled_bg));
    }

    public void setMoveBtnState(boolean active) {
        makeamove.setEnabled(active);
        if (active) makeamove.setBackground(getResources().getDrawable(R.drawable.game_move_btn_bg));
        if (!active) makeamove.setBackground(getResources().getDrawable(R.drawable.game_booster_button_disabled_bg));
    }

    public void toggleBoard(boolean active) {
        boardView.setDiagonalState(diagonalState);
        boardView.setJumpState(jumpState);
        boardView.setMyTurn(active);
    }

    public void updateSnake(PlayerMoveBroadcastGameEvent event) {
        boardView.placeSnake(event.getBodyXY(), event.getRotations()[0], event.getSnakeColor(), event.getSprites()[0], event.getBodyturnMirror());
        boardView.placeSnake(event.getHeadXY(), event.getRotations()[1], event.getSnakeColor(), event.getSprites()[1], 0);
        if (event.getWho() == eventManager.getPlayerNum()) {
            boardView.setSnakeHead(new Point(event.getHeadXY()[0], event.getHeadXY()[1]));
        }
    }

    public void removeSnake(List<int[]> moves, int headRotation, int[] snakeColor, int snakeBuried) {
        boardView.placeDeadHead(moves.get(0), headRotation, snakeColor, snakeBuried);
        boardView.removeSnake(moves);
    }

    public void updatePlayerTurnTimer(int player, int moveTimer) {
        player_bars.get(player).setProgress(moveTimer);
        player_bars.get(player).setVisibility(View.VISIBLE);
    }

    public void updateTimerCount(int moveTimer) {
        game_top_text.setText(String.valueOf(moveTimer));
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