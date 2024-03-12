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
import java.util.List;

public class GameActivity extends AppCompatActivity {
    private EventManager eventManager;
    private Preferences preferences;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor preferencesEditor;

    //AUDIO
    private CustomAudioManager cam;
    private Button game_soundToggle;
    private SeekBar game_volume_seekbar;

    private LinearLayout game_eventLog;
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
    private LinearLayout player1_bar, player2_bar, player3_bar, player4_bar;
    private ProgressBar avatar1_progressBar, avatar2_progressBar, avatar3_progressBar, avatar4_progressBar;


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

        //Player bars
        player1_bar = findViewById(R.id.player1_bar);
        player2_bar = findViewById(R.id.player2_bar);
        player3_bar = findViewById(R.id.player3_bar);
        player4_bar = findViewById(R.id.player4_bar);

        //Turn Timers
        avatar1_progressBar = findViewById(R.id.avatar1_progressBar);
        avatar2_progressBar = findViewById(R.id.avatar2_progressBar);
        avatar3_progressBar = findViewById(R.id.avatar3_progressBar);
        avatar4_progressBar = findViewById(R.id.avatar4_progressBar);
        avatar1_progressBar.setVisibility(View.VISIBLE);
        avatar2_progressBar.setVisibility(View.INVISIBLE);
        avatar3_progressBar.setVisibility(View.INVISIBLE);
        avatar4_progressBar.setVisibility(View.INVISIBLE);

        //setup game, board, players
        eventManager.setup();
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    eventManager.sendEvent(new PlayerMovedGameEvent(new int[] {boardView.getmSelectedColumn(), boardView.getmSelectedRow()})); //TODO WORTH INVESTIGATING
                }
            }).start();

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

    public void displayEventLog(String text) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                TextView textView = new TextView(getApplicationContext());
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                );
//                textView.setLayoutParams(params);
//                textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
//                textView.setGravity(Gravity.CENTER);
//                textView.setText(text);
//
//                game_eventLog.addView(textView);
//
//                int maxLines = 10;
//                if (game_eventLog.getChildCount() > maxLines) {
//                    View viewToRemove = game_eventLog.getChildAt(0);
//                    AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
//                    anim.setDuration(500);
//                    anim.setAnimationListener(new Animation.AnimationListener() {
//                        @Override
//                        public void onAnimationStart(Animation animation) {}
//                        @Override
//                        public void onAnimationEnd(Animation animation) {
//                            game_eventLog.removeView(viewToRemove);
//                        }
//                        @Override
//                        public void onAnimationRepeat(Animation animation) {}
//                    });
//                    viewToRemove.startAnimation(anim);
//                }
//            }
//        });
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

    public void updatePlayerTurnTimer(int player) {

    }

    public void cancelGame(String errorTitle, String errorDesc) {
        Intent intent = new Intent(this, MainActivity.class);
        if (!TextUtils.isEmpty(errorTitle)) {
            intent.putExtra("errorTitle", errorTitle);
            intent.putExtra("errorDesc", errorDesc);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}