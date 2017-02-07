package com.gmail.smanis.konstantinos.qttt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class OptionsActivity extends AppCompatActivity {
    public final static String EXTRA_PLAYER = "com.gmail.smanis.konstantinos.qttt.PLAYER";
    public final static String EXTRA_DIFFICULTY = "com.gmail.smanis.konstantinos.qttt.DIFFICULTY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void start(View view) {
        RadioGroup radioGroupPlayer = (RadioGroup) findViewById(R.id.radioGroup_player);
        int radioButtonPlayerId = radioGroupPlayer.getCheckedRadioButtonId();
        RadioButton radioButtonPlayer = (RadioButton) findViewById(radioButtonPlayerId);
        int playerIndex = radioGroupPlayer.indexOfChild(radioButtonPlayer);
        RadioGroup radioGroupDifficulty = (RadioGroup) findViewById(R.id.radioGroup_difficulty);
        int radioButtonDifficultyId = radioGroupDifficulty.getCheckedRadioButtonId();
        RadioButton radioButtonDifficulty = (RadioButton) findViewById(radioButtonDifficultyId);
        int difficultyIndex = radioGroupDifficulty.indexOfChild(radioButtonDifficulty);

        Intent intent = new Intent(this, SingleActivity.class);
        intent.putExtra(EXTRA_PLAYER, playerIndex);
        intent.putExtra(EXTRA_DIFFICULTY, difficultyIndex);
        startActivity(intent);
    }
}
