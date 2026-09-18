package com.gmail.smanis.konstantinos.qttt;

import android.content.Intent;
import android.os.Bundle;
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
        RadioGroup radioGroupDifficulty = findViewById(R.id.radioGroup_difficulty);

        Intent intent = new Intent(this, SingleActivity.class);
        intent.putExtra(
                EXTRA_PLAYER,
                playerForRadioButton(radioGroupPlayer.getCheckedRadioButtonId()).id());
        intent.putExtra(
                EXTRA_DIFFICULTY,
                difficultyForRadioButton(radioGroupDifficulty.getCheckedRadioButtonId()).id());
        startActivity(intent);
    }

    static Player playerForRadioButton(int id) {
        if (id == R.id.radioButton_x) {
            return Player.X;
        } else if (id == R.id.radioButton_o) {
            return Player.O;
        }
        throw new IllegalArgumentException("Unknown player radio button id: " + id);
    }

    static Difficulty difficultyForRadioButton(int id) {
        if (id == R.id.radioButton_random) {
            return Difficulty.RANDOM;
        } else if (id == R.id.radioButton_easy) {
            return Difficulty.EASY;
        } else if (id == R.id.radioButton_medium) {
            return Difficulty.MEDIUM;
        } else if (id == R.id.radioButton_hard) {
            return Difficulty.HARD;
        } else if (id == R.id.radioButton_optimal) {
            return Difficulty.OPTIMAL;
        }
        throw new IllegalArgumentException("Unknown difficulty radio button id: " + id);
    }
}
