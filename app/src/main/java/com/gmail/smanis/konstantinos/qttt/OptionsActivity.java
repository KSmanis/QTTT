package com.gmail.smanis.konstantinos.qttt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;

public class OptionsActivity extends AppCompatActivity {
    public static final String EXTRA_PLAYER = "com.gmail.smanis.konstantinos.qttt.PLAYER";
    public static final String EXTRA_DIFFICULTY = "com.gmail.smanis.konstantinos.qttt.DIFFICULTY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.button_start).setOnClickListener(view -> start());
    }

    private void start() {
        RadioGroup radioGroupPlayer = findViewById(R.id.radioGroup_player);
        int radioButtonPlayerId = radioGroupPlayer.getCheckedRadioButtonId();
        RadioButton radioButtonPlayer = findViewById(radioButtonPlayerId);
        int playerIndex = radioGroupPlayer.indexOfChild(radioButtonPlayer);
        RadioGroup radioGroupDifficulty = findViewById(R.id.radioGroup_difficulty);
        int radioButtonDifficultyId = radioGroupDifficulty.getCheckedRadioButtonId();
        RadioButton radioButtonDifficulty = findViewById(radioButtonDifficultyId);
        int difficultyIndex = radioGroupDifficulty.indexOfChild(radioButtonDifficulty);

        Intent intent = new Intent(this, SingleActivity.class);
        intent.putExtra(EXTRA_PLAYER, playerIndex);
        intent.putExtra(EXTRA_DIFFICULTY, difficultyIndex);
        startActivity(intent);
    }
}
