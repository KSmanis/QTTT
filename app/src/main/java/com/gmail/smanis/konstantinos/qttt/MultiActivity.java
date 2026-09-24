package com.gmail.smanis.konstantinos.qttt;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;

public class MultiActivity extends AppCompatActivity {
    private GameViewModel game;
    private Snackbar mSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        game = new ViewModelProvider(this).get(GameViewModel.class);
        GameView gameView = findViewById(R.id.gameView);
        gameView.setOnGameOverListener(
                res -> {
                    String message = GameResultFormatter.format(this, res);
                    mSnackbar = Snackbar.make(gameView, message, Snackbar.LENGTH_INDEFINITE);
                    mSnackbar.setAction(R.string.action_reset, view -> resetBoard());
                    mSnackbar.show();
                });
        gameView.setOnInputListener(game::applyInput);
        game.snapshots()
                .observe(
                        this,
                        state -> {
                            gameView.render(state);
                            invalidateOptionsMenu();
                        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_single, menu);
        menu.findItem(R.id.action_undo).setVisible(game.snapshot().isUndoAvailable());
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

    private void resetBoard() {
        game.reset();
        if (mSnackbar != null) {
            mSnackbar.dismiss();
            mSnackbar = null;
        }
    }

    private void undoMove() {
        game.undo();
        if (mSnackbar != null) {
            mSnackbar.dismiss();
            mSnackbar = null;
        }
    }
}
