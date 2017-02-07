package com.gmail.smanis.konstantinos.qttt;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MultiActivity extends AppCompatActivity {
    private GameView gameView;
    private State state;
    private Snackbar mSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gameView = (GameView) findViewById(R.id.gameView);
        gameView.setOnGameOverListener(new GameView.OnGameOverListener() {
            @Override
            public void onGameOver(GameResult res) {
                String message;
                if (res.gameOver()) {
                    if (res.draw()) {
                        message = getString(R.string.result_draw);
                    } else {
                        message = getString(R.string.result_winner, res.winner());
                    }
                } else {
                    message = getString(R.string.result_in_progress);
                }
                mSnackbar = Snackbar.make(gameView, message, Snackbar.LENGTH_INDEFINITE);
                mSnackbar.setAction(R.string.action_reset, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resetBoard();
                    }
                });
                mSnackbar.show();
            }
        });
        gameView.setOnInputListener(new GameView.OnInputListener() {
            @Override
            public void onInput(State s) {
                invalidateOptionsMenu();
            }
        });
        state = gameView.state();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_multi, menu);
        menu.findItem(R.id.action_undo).setVisible(state.isUndoAvailable());
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_undo:
            undoMove();
            return true;
        case R.id.action_reset:
            resetBoard();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
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
