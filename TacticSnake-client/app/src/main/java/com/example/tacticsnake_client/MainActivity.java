package com.example.tacticsnake_client;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {
    private long mLastClickTime = 0;

    ConstraintLayout cl_main_menu;
    ConstraintLayout cl_play_menu;
    ConstraintLayout cl_settings_menu;
    ConstraintLayout cl_preferences_menu;
    ConstraintLayout cl_hotseat_play_menu;


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

    TextView selMenuText;

    SeekBar playerNum;TextView playerNumText;
    SeekBar playerNumOff;TextView playerNumTextOff;
    SeekBar botNum;TextView botNumText;


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

        selMenuText = findViewById(R.id.selmenutext);

        cl_main_menu = findViewById(R.id.main_menu);
        cl_play_menu = findViewById(R.id.play_menu);
        cl_settings_menu = findViewById(R.id.settings_menu);
        cl_preferences_menu = findViewById(R.id.preferences_menu);
        cl_hotseat_play_menu = findViewById(R.id.hotseat_play_menu);

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

//        //ONLINE SEEKBAR
//        playerNumText = findViewById(R.id.playernumtext);
//        playerNum = findViewById(R.id.playernum);
//        playerNum.setOnSeekBarChangeListener(
//                new SeekBar.OnSeekBarChangeListener()
//                {
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {}
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {}
//
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress,
//                                                  boolean fromUser)
//                    {
//                        String string = "Number of players: " + (progress + 2);
//                        playerNumText.setText(string);
//                    }
//                }
//        );
//
//        //OFFLINE SEEKBARS
//        playerNumTextOff = findViewById(R.id.playernumtextoff);
//        playerNumOff = findViewById(R.id.playernumoff);
//        playerNumOff.setOnSeekBarChangeListener(
//                new SeekBar.OnSeekBarChangeListener()
//                {
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {}
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {}
//
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress,
//                                                  boolean fromUser)
//                    {
//                        while((progress + 1) + (botNum.getProgress()) > 4) {
//                            botNum.setProgress(botNum.getProgress() - 1);
//                        }
//                        String string = "Number of players: " + (progress + 1);
//                        playerNumTextOff.setText(string);
//                    }
//                }
//        );
//
//        botNumText = findViewById(R.id.botnumtext);
//        botNum = findViewById(R.id.botsnum);
//        botNum.setOnSeekBarChangeListener(
//                new SeekBar.OnSeekBarChangeListener()
//                {
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {}
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {}
//
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress,
//                                                  boolean fromUser)
//                    {
//                        while(progress + (playerNumOff.getProgress() + 1) > 4) {
//                            playerNumOff.setProgress(playerNumOff.getProgress() - 1);
//                        }
//                        String string = "Number of bots: " + progress;
//                        botNumText.setText(string);
//                    }
//                }
//        );
    }

    //NAVIGATION
    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 250){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()) {
            case R.id.mainMenu_quickGame:
                //TODO
                break;
            case R.id.mainMenu_play:
                cl_main_menu.setVisibility(View.INVISIBLE);
                cl_play_menu.setVisibility(View.VISIBLE);
                selMenuText.setText(R.string.menucategory2);
                break;
            case R.id.mainMenu_instructions:
                break;
            case R.id.mainMenu_statistics:
                break;
            case R.id.mainMenu_gameSettings:
                cl_main_menu.setVisibility(View.INVISIBLE);
                cl_settings_menu.setVisibility(View.VISIBLE);
                selMenuText.setText(R.string.menucategory3);
                break;
            case R.id.mainMenu_playerSettings:
                cl_main_menu.setVisibility(View.INVISIBLE);
                cl_preferences_menu.setVisibility(View.VISIBLE);
                selMenuText.setText(R.string.menucategory4);
                break;
            case R.id.find_button:
                //TODO
                break;
            case R.id.playMenu_online_game:
                //TODO
                break;
            case R.id.playMenu_private_online_game:
                //TODO
                break;
            case R.id.playMenu_hotseat_game:
                cl_play_menu.setVisibility(View.INVISIBLE);
                cl_hotseat_play_menu.setVisibility(View.VISIBLE);
                selMenuText.setText("Hotseat lobby");
                break;

            case R.id.back_btn:
                cl_play_menu.setVisibility(View.INVISIBLE);
                cl_main_menu.setVisibility(View.VISIBLE);
                selMenuText.setText("");
                break;
            case R.id.settings_back_btn:
                cl_settings_menu.setVisibility(View.INVISIBLE);
                cl_main_menu.setVisibility(View.VISIBLE);
                selMenuText.setText("");
                break;
            case R.id.preferences_back_btn:
                cl_preferences_menu.setVisibility(View.INVISIBLE);
                cl_main_menu.setVisibility(View.VISIBLE);
                selMenuText.setText("");
                break;

            //Hotseat
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

            case R.id.hotseat_play:
                Intent intent = new Intent(this, GameActivity.class);
                startActivity(intent);
                //TODO
                break;
            case R.id.hotseat_back_btn:
                cl_hotseat_play_menu.setVisibility(View.INVISIBLE);
                cl_play_menu.setVisibility(View.VISIBLE);
                selMenuText.setText("");
                break;


//            case R.id.play_online:
//                clmain.setVisibility(View.INVISIBLE);
//                clpon.setVisibility(View.VISIBLE);
//                selMenuText.setText(R.string.menucategory1);
//                break;
//            case R.id.play_offline:
//                clmain.setVisibility(View.INVISIBLE);
//                clpoff.setVisibility(View.VISIBLE);
//                selMenuText.setText(R.string.menucategory2);
//                break;
//            case R.id.returnimage:
//                clpon.setVisibility(View.INVISIBLE);
//                clpoff.setVisibility(View.INVISIBLE);
//                clmain.setVisibility(View.VISIBLE);
//                selMenuText.setText("");
//                break;
//            case R.id.findOfflineGameBtn:
//                Intent intent = new Intent(this, GameActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.instructions:
//                break;
        }
    }

    //HELPERS
    public void onWhatIsCorpse(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Corpse mode does not remove dead snakes but leaves them as obstacles");
        builder.setTitle("Information");
        builder.setNegativeButton("CLOSE", (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void onWhatIsPortalwalls(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Portal Walls lets snakes to go through walls and appear on the other side");
        builder.setTitle("Information");
        builder.setNegativeButton("CLOSE", (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}