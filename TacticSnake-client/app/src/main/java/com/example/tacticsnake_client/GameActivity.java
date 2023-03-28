package com.example.tacticsnake_client;

import android.annotation.SuppressLint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class GameActivity extends AppCompatActivity {

    private Button diagonalBooster;
    private Button jumpBooster;
    private Button makeamove;
    private int diagonalBoosterLeft = 1;
    private int jumpBoosterLeft = 1;
    private int diagonalState;
    private int jumpState;
    private BoardView boardView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        try
        {
            this.getSupportActionBar().show();
        }
        catch (NullPointerException e){}

        boardView = findViewById(R.id.board_view);

        diagonalBooster = findViewById(R.id.diagonalbooster);
        jumpBooster = findViewById(R.id.jumpbooster);
        makeamove = findViewById(R.id.makeamove);

        // Get the screen width
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        // Change the height and width of the BoardView to match the screen width
        ViewGroup.LayoutParams layoutParams = boardView.getLayoutParams();
        layoutParams.width = screenWidth;
        layoutParams.height = screenWidth;
        boardView.setLayoutParams(layoutParams);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitemexit:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.diagonalbooster:
                diagonalState = Math.abs(diagonalState - 1);
                if (diagonalState == 0) {
                    diagonalBooster.setTextColor(getResources().getColor(R.color.black));
                }
                else {
                    diagonalBooster.setTextColor(getResources().getColor(R.color.dracula_fg));
                }

                v.setSelected(!v.isSelected());
                boardView.evaluateValidMoves(diagonalState, jumpState);
                boardView.changeMoveBtnStyle();
                break;
            case R.id.jumpbooster:
                jumpState = Math.abs(jumpState - 1);
                if (jumpState == 0) {
                    jumpBooster.setTextColor(getResources().getColor(R.color.black));
                }
                else {
                    jumpBooster.setTextColor(getResources().getColor(R.color.dracula_fg));
                }

                v.setSelected(!v.isSelected());
                boardView.evaluateValidMoves(diagonalState, jumpState);
                boardView.changeMoveBtnStyle();
                break;
            case R.id.makeamove:
                boardView.placeSnake();
                if (diagonalState == 1) diagonalBoosterLeft -= 1;
                if (diagonalBoosterLeft == 0) {
                    diagonalBooster.setEnabled(false);
                    diagonalBooster.setBackground(getResources().getDrawable(R.drawable.game_booster_button_disabled_bg));
                    diagonalState = 0;
                }
                if (jumpState == 1) jumpBoosterLeft -= 1;
                if (jumpBoosterLeft == 0) {
                    jumpBooster.setEnabled(false);
                    jumpBooster.setBackground(getResources().getDrawable(R.drawable.game_booster_button_disabled_bg));
                    jumpState = 0;
                }

                boardView.evaluateValidMoves(diagonalState, jumpState);
                boardView.changeMoveBtnStyle();

                break;
            default:
                break;
        }
    }

    public void setMoveBtnState(boolean active) {
        makeamove.setEnabled(active);
        if (active) makeamove.setBackground(getResources().getDrawable(R.drawable.game_move_btn_bg));
        if (!active) makeamove.setBackground(getResources().getDrawable(R.drawable.game_booster_button_disabled_bg));
    }
}