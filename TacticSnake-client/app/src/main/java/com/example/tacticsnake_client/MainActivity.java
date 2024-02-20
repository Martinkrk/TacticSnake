package com.example.tacticsnake_client;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.example.tacticsnake_client.events.EventManager;
import com.example.tacticsnake_client.events.HotseatEventManager;
import com.shared.game.Preferences;

public class MainActivity extends AppCompatActivity {
    private long mLastClickTime = 0;
    private Preferences preferences;
    private SharedPreferences sharedPreferences;

    ConstraintLayout cl_main_menu;
    ConstraintLayout cl_play_menu;
    ConstraintLayout cl_play_find_menu;
    ConstraintLayout cl_play_online_menu;
    ConstraintLayout cl_settings_menu;
    ConstraintLayout cl_preferences_menu;
    ConstraintLayout cl_play_hotseat_menu;


    //Hotseat
    int[] hotseatPlayers = new int[] {1, 0, 0, 0};
    Button hs_row1_state1;
    LinearLayout hs_row1_state2;
    LinearLayout hs_row1_state3;
    Button hs_row1_playerOrBot;
    Button hs_row2_state1;
    LinearLayout hs_row2_state2;
    LinearLayout hs_row2_state3;
    Button hs_row2_playerOrBot;
    Button hs_row3_state1;
    LinearLayout hs_row3_state2;
    LinearLayout hs_row3_state3;
    Button hs_row3_playerOrBot;

    //Main menu category text
    TextView selMenuText;

    //Settings
    SeekBar settings_boardSize_seekbar;
    TextView settings_boardSize_text;
    SeekBar settings_onlinePlayersNumber_seekbar;
    TextView settings_onlinePlayersNumber_text;
    private final int boardSizeOffset = 6;
    private final int onlinePlayersNumberOffset = 2;

    SwitchCompat settings_private_switch;
    SwitchCompat settings_corpseMode_switch;
    SwitchCompat settings_portalWallsMode_switch;

    //Preferences
    EditText preferences_playerName_editText;
    SeekBar preferences_snakeColor_redSeekbar;
    SeekBar preferences_snakeColor_greenSeekbar;
    SeekBar preferences_snakeColor_blueSeekbar;
    View preferences_snakeColor_colorPreview;

    //Play menu
    EditText mainMenu_find_game_input;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}


        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Preferences
        preferences = new Preferences();
        sharedPreferences = getSharedPreferences("preferences", MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("arePreferences", false)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("arePreferences", true);
            editor.putInt("fieldWith", 8);
            editor.putInt("fieldHeight", 8);
            editor.putBoolean("isPortalWalls", false);
            editor.putBoolean("isCorpse", false);
            editor.putInt("playersNum", 4);
            editor.putString("nick", "Nameless");
            editor.putInt("red", 255);
            editor.putInt("green", 255);
            editor.putInt("blue", 255);
            editor.apply();
        } else {
            preferences.fieldWith = sharedPreferences.getInt("fieldWith", preferences.fieldWith);
            preferences.fieldHeight = sharedPreferences.getInt("fieldHeight", preferences.fieldHeight);
            preferences.isPortalWalls = sharedPreferences.getBoolean("isPortalWalls", preferences.isPortalWalls);
            preferences.isCorpse = sharedPreferences.getBoolean("isCorpse", preferences.isCorpse);
            preferences.playersNum = sharedPreferences.getInt("playersNum", preferences.playersNum);
            preferences.nick = sharedPreferences.getString("nick", preferences.nick);
            preferences.snakeColor = new int[] {sharedPreferences.getInt("red", 255),
                    sharedPreferences.getInt("green", 255), sharedPreferences.getInt("blue", 255)};
        }

        //Main menu category text
        selMenuText = findViewById(R.id.selmenutext);

        //Main layouts
        cl_main_menu = findViewById(R.id.main_menu);
        cl_play_menu = findViewById(R.id.play_menu);
        cl_play_find_menu = findViewById(R.id.play_find_menu);
        cl_play_online_menu = findViewById(R.id.play_online_menu);
        cl_settings_menu = findViewById(R.id.settings_menu);
        cl_preferences_menu = findViewById(R.id.preferences_menu);
        cl_play_hotseat_menu = findViewById(R.id.play_hotseat_menu);

        //Play menu
        mainMenu_find_game_input = findViewById(R.id.findMenu_input);

        //Hotseat menu
        hs_row1_state1 = findViewById(R.id.hotseat_row1_state1);
        hs_row1_state2 = findViewById(R.id.hotseat_row1_state2);
        hs_row1_state3 = findViewById(R.id.hotseat_row1_state3);
        hs_row1_playerOrBot = findViewById(R.id.hotseat_row1_state3_btn1);

        hs_row2_state1 = findViewById(R.id.hotseat_row2_state1);
        hs_row2_state2 = findViewById(R.id.hotseat_row2_state2);
        hs_row2_state3 = findViewById(R.id.hotseat_row2_state3);
        hs_row2_playerOrBot = findViewById(R.id.hotseat_row2_state3_btn1);

        hs_row3_state1 = findViewById(R.id.hotseat_row3_state1);
        hs_row3_state2 = findViewById(R.id.hotseat_row3_state2);
        hs_row3_state3 = findViewById(R.id.hotseat_row3_state3);
        hs_row3_playerOrBot = findViewById(R.id.hotseat_row3_state3_btn1);

        //Board size seekbar
        settings_boardSize_text = findViewById(R.id.settings_boardSize_text);
        settings_boardSize_seekbar = findViewById(R.id.settings_boardSize_seekbar);
        settings_boardSize_seekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {
                        String string = "Board Size: " + (progress + boardSizeOffset);
                        settings_boardSize_text.setText(string);
                    }
                }
        );

        //Online players count seekbar
        settings_onlinePlayersNumber_text = findViewById(R.id.settings_onlinePlayersNumber_text);
        settings_onlinePlayersNumber_seekbar = findViewById(R.id.settings_onlinePlayersNumber_seekbar);
        settings_onlinePlayersNumber_seekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {
                        String string = "Players: " + (progress + onlinePlayersNumberOffset);
                        settings_onlinePlayersNumber_text.setText(string);
                    }
                }
        );

        //Switches
        settings_private_switch = findViewById(R.id.settings_private_switch);
        settings_corpseMode_switch = findViewById(R.id.settings_corpseMode_switch);
        settings_portalWallsMode_switch = findViewById(R.id.settings_portalWallsMode_switch);

        //Preferences
        preferences_playerName_editText = findViewById(R.id.preferences_playerName_editText);
        preferences_snakeColor_redSeekbar = findViewById(R.id.preferences_snakeColor_redSeekbar);
        preferences_snakeColor_greenSeekbar = findViewById(R.id.preferences_snakeColor_greenSeekbar);
        preferences_snakeColor_blueSeekbar = findViewById(R.id.preferences_snakeColor_blueSeekbar);
        preferences_snakeColor_colorPreview = findViewById(R.id.preferences_snakeColor_colorPreview);

        preferences_snakeColor_redSeekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {
                        int backgroundColor = ((ColorDrawable) preferences_snakeColor_colorPreview.getBackground()).getColor();
                        int green = Color.green(backgroundColor);
                        int blue = Color.blue(backgroundColor);
                        int newColor = Color.rgb(progress, green, blue);
                        preferences_snakeColor_colorPreview.setBackgroundColor(newColor);

                    }
                }
        );

        preferences_snakeColor_greenSeekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {
                        int backgroundColor = ((ColorDrawable) preferences_snakeColor_colorPreview.getBackground()).getColor();
                        int red = Color.red(backgroundColor);
                        int blue = Color.blue(backgroundColor);
                        int newColor = Color.rgb(red, progress, blue);
                        preferences_snakeColor_colorPreview.setBackgroundColor(newColor);

                    }
                }
        );

        preferences_snakeColor_blueSeekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {
                        int backgroundColor = ((ColorDrawable) preferences_snakeColor_colorPreview.getBackground()).getColor();
                        int red = Color.red(backgroundColor);
                        int green = Color.green(backgroundColor);
                        int newColor = Color.rgb(red, green, progress);
                        preferences_snakeColor_colorPreview.setBackgroundColor(newColor);

                    }
                }
        );

        //Display error (if any)
        String errorTitle = "";
        String errorDesc = "";
        try {
            errorTitle = getIntent().getExtras().getString("errorTitle");
            errorDesc = getIntent().getExtras().getString("errorDesc");
        } catch (NullPointerException e) {}

        if (!TextUtils.isEmpty(errorTitle) && !TextUtils.isEmpty(errorDesc)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(errorDesc);
            builder.setTitle(errorTitle);
            builder.setNegativeButton("OK", (dialog, which) -> {
                dialog.cancel();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        //Use preferences to set values of setting's elements
        preferences_playerName_editText.setText(preferences.nick);
        preferences_snakeColor_redSeekbar.setProgress(1);
        preferences_snakeColor_redSeekbar.setProgress(preferences.snakeColor[0]-1);
        preferences_snakeColor_greenSeekbar.setProgress(1);
        preferences_snakeColor_greenSeekbar.setProgress(preferences.snakeColor[1]-1);
        preferences_snakeColor_blueSeekbar.setProgress(1);
        preferences_snakeColor_blueSeekbar.setProgress(preferences.snakeColor[2]-1);
        preferences_snakeColor_colorPreview.setBackgroundColor(Color.rgb(preferences.snakeColor[0], preferences.snakeColor[1], preferences.snakeColor[2]));
        settings_boardSize_seekbar.setProgress(1);
        settings_boardSize_seekbar.setProgress(preferences.fieldHeight-boardSizeOffset);
        settings_onlinePlayersNumber_seekbar.setProgress(1);
        settings_onlinePlayersNumber_seekbar.setProgress(preferences.playersNum - onlinePlayersNumberOffset);
        settings_corpseMode_switch.setChecked(preferences.isCorpse);
        settings_portalWallsMode_switch.setChecked(preferences.isPortalWalls);
    }

    //NAVIGATION
    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 250){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        Intent intent;

        switch (v.getId()) {
                // MAIN MENU
            case R.id.mainMenu_quickGame:
                intent = new Intent(this, LoadingActivity.class);
                preferences.gameMode = 0;
                intent.putExtra("preferences", preferences);
                startActivity(intent);
                break;

            case R.id.mainMenu_play:
                cl_main_menu.setVisibility(View.INVISIBLE);
                cl_play_menu.setVisibility(View.VISIBLE);
                selMenuText.setText(R.string.menu_category2);
                break;

            case R.id.mainMenu_instructions:
                Toast toast = Toast.makeText(getApplicationContext(), "It's your turn!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                break;

            case R.id.mainMenu_statistics:
                break;

            case R.id.mainMenu_gameSettings:
                cl_main_menu.setVisibility(View.INVISIBLE);
                cl_settings_menu.setVisibility(View.VISIBLE);
                selMenuText.setText(R.string.menu_category3);
                break;

            case R.id.mainMenu_playerSettings:
                cl_main_menu.setVisibility(View.INVISIBLE);
                cl_preferences_menu.setVisibility(View.VISIBLE);
                selMenuText.setText(R.string.menu_category4);
                break;

                //PLAY MENU
            case R.id.playMenu_find_game:
                //preferences.gameMode = 3;
                cl_play_menu.setVisibility(View.INVISIBLE);
                cl_play_find_menu.setVisibility(View.VISIBLE);
                selMenuText.setText(R.string.menu_play_category1);
                break;

            case R.id.playMenu_online_game:
                cl_play_menu.setVisibility(View.INVISIBLE);
                cl_play_online_menu.setVisibility(View.VISIBLE);
                selMenuText.setText(R.string.menu_play_category2);
                break;

            case R.id.playMenu_hotseat_game:
                cl_play_menu.setVisibility(View.INVISIBLE);
                cl_play_hotseat_menu.setVisibility(View.VISIBLE);
                selMenuText.setText(R.string.menu_play_category3);
                break;

            case R.id.back_btn:
                cl_play_menu.setVisibility(View.INVISIBLE);
                cl_main_menu.setVisibility(View.VISIBLE);
                selMenuText.setText("");
                break;

                //FIND GAME MENU
            case R.id.findMenu_find_btn:
                intent = new Intent(this, LoadingActivity.class);
                preferences.gameMode = 2;
                preferences.gameRoom = String.valueOf(mainMenu_find_game_input.getText());
                intent.putExtra("preferences", preferences);
                startActivity(intent);
                break;

            case R.id.findMenu_back_btn:
                cl_play_find_menu.setVisibility(View.INVISIBLE);
                cl_play_menu.setVisibility(View.VISIBLE);
                selMenuText.setText(R.string.menu_category2);
                break;

                //ONLINE GAME MENU
            case R.id.onlineMenu_play_btn:
                // Preferences class
                preferences.gameMode = settings_private_switch.isChecked() ? 3 : 1; // TODO place correct values
                preferences.fieldWith = settings_boardSize_seekbar.getProgress() + boardSizeOffset;
                preferences.fieldHeight = settings_boardSize_seekbar.getProgress() + boardSizeOffset;
                preferences.playersNum = settings_onlinePlayersNumber_seekbar.getProgress() + onlinePlayersNumberOffset;
                preferences.isCorpse = settings_corpseMode_switch.isChecked();
                preferences.isPortalWalls = settings_portalWallsMode_switch.isChecked();

                // Shared Preferences
                SharedPreferences.Editor settingsEditor = sharedPreferences.edit();
                settingsEditor.putInt("fieldWith", preferences.fieldWith);
                settingsEditor.putInt("fieldHeight", preferences.fieldHeight);
                settingsEditor.putBoolean("isPortalWalls", preferences.isPortalWalls);
                settingsEditor.putBoolean("isCorpse", preferences.isCorpse);
                settingsEditor.putInt("playersNum", preferences.playersNum);
                settingsEditor.apply();

                intent = new Intent(this, LoadingActivity.class);
                intent.putExtra("preferences", preferences);
                startActivity(intent);
                break;

            case R.id.onlineMenu_back_btn:
                cl_play_online_menu.setVisibility(View.INVISIBLE);
                cl_play_menu.setVisibility(View.VISIBLE);
                selMenuText.setText(R.string.menu_category2);
                break;

                // PREFERENCES MENU
            case R.id.preferences_done_btn:
                // Preferences class
                preferences.nick = String.valueOf(preferences_playerName_editText.getText());
                int red = preferences_snakeColor_redSeekbar.getProgress();
                int green = preferences_snakeColor_greenSeekbar.getProgress();
                int blue = preferences_snakeColor_blueSeekbar.getProgress();
                preferences.snakeColor = new int[] {red, green, blue};

                // Shared Preferences
                SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                preferencesEditor.putString("nick", preferences.nick);
                preferencesEditor.putInt("red", red);
                preferencesEditor.putInt("green", green);
                preferencesEditor.putInt("blue", blue);
                preferencesEditor.apply();

                cl_preferences_menu.setVisibility(View.INVISIBLE);
                cl_main_menu.setVisibility(View.VISIBLE);
                selMenuText.setText("");
                break;

                // SETTINGS MENU
            case R.id.settings_done_btn:
                cl_settings_menu.setVisibility(View.INVISIBLE);
                cl_main_menu.setVisibility(View.VISIBLE);
                selMenuText.setText("");
                break;

                //HOTSEAT GAME MENU
            case R.id.mainMenu_hotseat_game_btn:
                intent = new Intent(this, GameActivity.class);
                intent.putExtra("eventManager", new HotseatEventManager(preferences, new HotseatGame(preferences)));
                intent.putExtra("players", hotseatPlayers);
                startActivity(intent);
                break;
            case R.id.mainMenu_hotseat_back_btn:
                cl_play_hotseat_menu.setVisibility(View.INVISIBLE);
                cl_play_menu.setVisibility(View.VISIBLE);
                selMenuText.setText(R.string.menu_category2);
                break;

            //HOTSEAT players menu
            case R.id.hotseat_row1_state1:
                hs_row1_state1.setVisibility(View.GONE);
                hs_row1_state2.setVisibility(View.VISIBLE);
                break;
            case R.id.hotseat_row1_state2_btn1:
                hs_row1_state2.setVisibility(View.GONE);
                hs_row1_state3.setVisibility(View.VISIBLE);
                hotseatPlayers[1] = 1;
                hs_row1_playerOrBot.setText("Player 2");
                break;
            case R.id.hotseat_row1_state2_btn2:
                hs_row1_state2.setVisibility(View.GONE);
                hs_row1_state3.setVisibility(View.VISIBLE);
                hotseatPlayers[1] = 2;
                hs_row1_playerOrBot.setText("Bot");
                break;
            case R.id.hotseat_row1_state3_btn2:
                hs_row1_state3.setVisibility(View.GONE);
                hs_row1_state1.setVisibility(View.VISIBLE);
                hotseatPlayers[1] = 0;
                break;

            case R.id.hotseat_row2_state1:
                hs_row2_state1.setVisibility(View.GONE);
                hs_row2_state2.setVisibility(View.VISIBLE);
                break;
            case R.id.hotseat_row2_state2_btn1:
                hs_row2_state2.setVisibility(View.GONE);
                hs_row2_state3.setVisibility(View.VISIBLE);
                hotseatPlayers[2] = 1;
                hs_row2_playerOrBot.setText("Player 3");
                break;
            case R.id.hotseat_row2_state2_btn2:
                hs_row2_state2.setVisibility(View.GONE);
                hs_row2_state3.setVisibility(View.VISIBLE);
                hotseatPlayers[2] = 2;
                hs_row2_playerOrBot.setText("Bot");
                break;
            case R.id.hotseat_row2_state3_btn2:
                hs_row2_state3.setVisibility(View.GONE);
                hs_row2_state1.setVisibility(View.VISIBLE);
                hotseatPlayers[2] = 0;
                break;

            case R.id.hotseat_row3_state1:
                hs_row3_state1.setVisibility(View.GONE);
                hs_row3_state2.setVisibility(View.VISIBLE);
                break;
            case R.id.hotseat_row3_state2_btn1:
                hs_row3_state2.setVisibility(View.GONE);
                hs_row3_state3.setVisibility(View.VISIBLE);
                hotseatPlayers[3] = 1;
                hs_row3_playerOrBot.setText("Player 4");
                break;
            case R.id.hotseat_row3_state2_btn2:
                hs_row3_state2.setVisibility(View.GONE);
                hs_row3_state3.setVisibility(View.VISIBLE);
                hotseatPlayers[3] = 2;
                hs_row3_playerOrBot.setText("Bot");
                break;
            case R.id.hotseat_row3_state3_btn2:
                hs_row3_state3.setVisibility(View.GONE);
                hs_row3_state1.setVisibility(View.VISIBLE);
                hotseatPlayers[3] = 0;
                break;
        }
    }

    //HELPERS
    public void onWhatIsCorpse(View v) {
        alertBox("Corpse mode does not remove dead snakes but leaves them as obstacles.");
    }

    public void onWhatIsPortalWalls(View v) {
        alertBox("Portal Walls mode lets snakes go through walls and appear on the other side.");
    }

    public void onWhatIsPrivate(View v) {
        alertBox("Players can only join your game via game room code provided to you.");
    }

    private void alertBox(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message);
        builder.setTitle("Information");
        builder.setNegativeButton("CLOSE", (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}