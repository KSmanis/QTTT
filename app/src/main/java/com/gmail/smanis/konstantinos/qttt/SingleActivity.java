package com.gmail.smanis.konstantinos.qttt;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class SingleActivity extends AppCompatActivity {
    enum Difficulty {
        Random,
        Easy,
        Medium,
        Hard,
        Optimal
    }
    private ProgressBar progressBar;
    private GameView gameView;
    private State state;
    private Snackbar mSnackbar;
    private Player mHumanPlayer;
    private Difficulty mGameDifficulty;
    private Random mRng;

    class MinimaxTask extends AsyncTask<Void, Integer, List<Move>> implements State.OnProgressListener {
        @Override
        protected void onPreExecute() {
            gameView.pause();
            invalidateOptionsMenu();
            progressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected List<Move> doInBackground(Void... params) {
            int turn = state.currentTurn();
            if (turn < 5) {
                AssetManager am = getResources().getAssets();
                String path = (turn < 4 ? String.valueOf(turn) : "4/" + state.moveHistory().substring(0, 5));
                try {
                    List<Move> ret = state.lookupNextMove(am.open(path));
                    if (!ret.isEmpty()) {
                        return ret;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return state.minimaxMoves(this);
        }
        @Override
        protected void onPostExecute(List<Move> moves) {
            applyMove(moves);
            gameView.resume();
            gameView.refresh();
            invalidateOptionsMenu();
            progressBar.setVisibility(View.INVISIBLE);
            botPlay();
        }
        @Override
        public void onProgress(int current, int max) {
            publishProgress(current, max);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mHumanPlayer = Player.values()[intent.getIntExtra(OptionsActivity.EXTRA_PLAYER, 0)];
        mGameDifficulty = Difficulty.values()[intent.getIntExtra(OptionsActivity.EXTRA_DIFFICULTY, 2)];

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
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
                botPlay();
            }
        });
        state = gameView.state();
        mRng = new Random();

        botPlay();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!gameView.isPaused()) {
            getMenuInflater().inflate(R.menu.menu_single, menu);
            menu.findItem(R.id.action_undo).setVisible(state.hasIncompleteInput() || state.currentTurn() >= 2);
        }
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

    private void applyMove(List<Move> optimalMoves) {
        List<Move> allMoves = state.availableMoves(false), pool = null;
        switch (mGameDifficulty) {
        case Random:
            pool = allMoves;
            break;
        case Easy:
            if (mRng.nextFloat() < 0.5) {
                pool = optimalMoves;
            } else {
                pool = allMoves;
            }
            break;
        case Medium:
            if (mRng.nextFloat() < 0.75) {
                pool = optimalMoves;
            } else {
                pool = allMoves;
            }
            break;
        case Hard:
            if (mRng.nextFloat() < 0.9 || state.entangled()) {
                pool = optimalMoves;
            } else {
                pool = allMoves;
            }
            break;
        case Optimal:
            pool = optimalMoves;
            break;
        }
        state.applyMove(pool.get(mRng.nextInt(pool.size())));
    }
    private void botPlay() {
        if (state.currentPlayer() == mHumanPlayer || state.gameOver()) {
            return;
        }

        if (mGameDifficulty == Difficulty.Random) {
            applyMove(null);
            gameView.refresh();
            invalidateOptionsMenu();
            botPlay();
        } else {
            new MinimaxTask().execute();
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
        botPlay();
    }
    private void undoMove() {
        if (state.hasIncompleteInput()) {
            state.undoLastMove();
        } else {
            while (state.currentPlayer() == mHumanPlayer) {
                state.undoLastMove();
            }
            while (state.currentPlayer() != mHumanPlayer) {
                state.undoLastMove();
            }
        }
        gameView.refresh();
        invalidateOptionsMenu();
        if (mSnackbar != null) {
            mSnackbar.dismiss();
            mSnackbar = null;
        }
    }
}
