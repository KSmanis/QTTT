package com.gmail.smanis.konstantinos.qttt;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;

public class SingleActivity extends AppCompatActivity {
    private GameView gameView;
    private GameViewModel game;
    private Snackbar mSnackbar;
    private Player mHumanPlayer;
    private Difficulty mGameDifficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (!intent.hasExtra(OptionsActivity.EXTRA_PLAYER)
                || !intent.hasExtra(OptionsActivity.EXTRA_DIFFICULTY)) {
            throw new IllegalArgumentException("SingleActivity options are missing");
        }
        mHumanPlayer = Player.fromId(intent.getIntExtra(OptionsActivity.EXTRA_PLAYER, -1));
        mGameDifficulty =
                Difficulty.fromId(intent.getIntExtra(OptionsActivity.EXTRA_DIFFICULTY, -1));

        game = new ViewModelProvider(this).get(GameViewModel.class);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        gameView = findViewById(R.id.gameView);
        gameView.setOnGameOverListener(
                res -> {
                    String message = GameResultFormatter.format(this, res);
                    mSnackbar = Snackbar.make(gameView, message, Snackbar.LENGTH_INDEFINITE);
                    mSnackbar.setAction(R.string.action_reset, view -> resetBoard());
                    mSnackbar.show();
                });
        gameView.setOnInputListener(
                cellIndex -> {
                    if (!game.applyInput(cellIndex)) {
                        return false;
                    }
                    game.playBot(mHumanPlayer, mGameDifficulty);
                    return true;
                });
        game.snapshots()
                .observe(
                        this,
                        state -> {
                            gameView.render(state);
                            invalidateOptionsMenu();
                        });
        game.thinking()
                .observe(
                        this,
                        thinking -> {
                            boolean isThinking = Boolean.TRUE.equals(thinking);
                            gameView.setPaused(isThinking);
                            progressBar.setVisibility(isThinking ? View.VISIBLE : View.INVISIBLE);
                            invalidateOptionsMenu();
                        });

        game.playBot(mHumanPlayer, mGameDifficulty);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!gameView.isPaused()) {
            getMenuInflater().inflate(R.menu.menu_single, menu);
            State state = game.snapshot();
            menu.findItem(R.id.action_undo)
                    .setVisible(state.hasIncompleteInput() || state.currentTurn() >= 2);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_undo) {
            undoMove();
            return true;
        } else if (itemId == R.id.action_reset) {
            resetBoard();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void resetBoard() {
        game.reset();
        dismissResult();
        game.playBot(mHumanPlayer, mGameDifficulty);
    }

    void undoMove() {
        game.undoSinglePlayer(mHumanPlayer);
        dismissResult();
    }

    private void dismissResult() {
        if (mSnackbar != null) {
            mSnackbar.dismiss();
            mSnackbar = null;
        }
    }
}
