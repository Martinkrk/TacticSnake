package com.example.tacticsnake_client;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.example.tacticsnake_client.events.OnlineEventManager;
import com.example.tacticsnake_client.singleton.AppSingleton;
import com.shared.events.GameInitiatedEvent;
import com.shared.game.GameSettings;

public class LoadingActivity extends AppCompatActivity {
    OnlineEventManager eventManager;
    ConstraintLayout loading_menu;
    TextView textView;
    TextView loading_roomCode_text;
    private long mLastClickTime = 0;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Preferences
        GameSettings preferences = (GameSettings) getIntent().getExtras().getSerializable("gameSettings");

        //Initial text
        textView = findViewById(R.id.loading_text_view);
        textView.setText("Connecting to server...");

        //Room code text
        loading_roomCode_text = findViewById(R.id.loading_roomCode_text);

        //Animation
        loading_menu = findViewById(R.id.loading_menu);

        //Event manager
        eventManager = new OnlineEventManager(this, preferences);
    }

    @Override
    public void onBackPressed() {
        cancelGame("", "");
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 250) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        if (v.getId() == R.id.loadingMenu_cancel_btn) {
            cancelGame("", "");
        }
    }

    public void editTextView(String text) {
        runOnUiThread(() -> textView.setText(text));
    }

    public void editRoomCodeTextView(String text) {
        runOnUiThread(() -> loading_roomCode_text.setText(text));
    }

    public void startGame(GameInitiatedEvent gameInitiatedEvent) {
        AppSingleton.getInstance().setOnlineEventManager(eventManager);

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("gameInitiatedEvent", gameInitiatedEvent);
        startActivity(intent);
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