package com.gmail.smanis.konstantinos.qttt;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

public class MultiActivity extends AppCompatActivity {
    private GameView gameView;
    private State state;
    private Snackbar mSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gameView = findViewById(R.id.gameView);
        gameView.setOnGameOverListener(
                res -> {
                    String message;
                    if (res.draw()) {
                        message = getString(R.string.result_draw);
                    } else {
                        message = getString(R.string.result_winner, res.winner());
                    }
                    mSnackbar = Snackbar.make(gameView, message, Snackbar.LENGTH_INDEFINITE);
                    mSnackbar.setAction(R.string.action_reset, view -> resetBoard());
                    mSnackbar.show();
                });
        gameView.setOnInputListener(state -> invalidateOptionsMenu());
        state = gameView.state();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_single, menu);
        menu.findItem(R.id.action_undo).setVisible(state.isUndoAvailable());
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
        state.reset();
        gameView.refresh();
        invalidateOptionsMenu();
        if (mSnackbar != null) {
            mSnackbar.dismiss();
            mSnackbar = null;
        }
    }

    private void undoMove() {
        state.undoLastMove();
        gameView.refresh();
        invalidateOptionsMenu();
        if (mSnackbar != null) {
            mSnackbar.dismiss();
            mSnackbar = null;
        }
    }
}
