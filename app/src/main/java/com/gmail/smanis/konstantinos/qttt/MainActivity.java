package com.gmail.smanis.konstantinos.qttt;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        OpeningBook.initialize(this);
        findViewById(R.id.button_single_play)
                .setOnClickListener(view -> startActivity(new Intent(this, OptionsActivity.class)));
        findViewById(R.id.button_multi_play)
                .setOnClickListener(view -> startActivity(new Intent(this, MultiActivity.class)));
        findViewById(R.id.button_tutorial)
                .setOnClickListener(
                        view -> startActivity(new Intent(this, TutorialActivity.class)));
    }
}
