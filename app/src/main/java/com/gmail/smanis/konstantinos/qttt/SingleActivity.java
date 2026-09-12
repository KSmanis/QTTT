package com.gmail.smanis.konstantinos.qttt;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.io.IOException;
import java.lang.ref.WeakReference;
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
    private Thread mMinimaxThread;
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private static class MinimaxTask implements Runnable {
        private final WeakReference<SingleActivity> mActivity;
        private final State mState;
        private final AssetManager mAssets;

        MinimaxTask(SingleActivity activity) {
            mActivity = new WeakReference<>(activity);
            mState = activity.state;
            mAssets = activity.getApplicationContext().getAssets();
        }

        @Override
        public void run() {
            List<Move> moves = findMoves();
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            MAIN_HANDLER.post(() -> {
                SingleActivity activity = mActivity.get();
                if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                    activity.finishBotMove(moves);
                }
            });
        }

        private List<Move> findMoves() {
            int turn = mState.currentTurn();
            if (turn < 5) {
                String path = (turn < 4 ? String.valueOf(turn) : "4/" + mState.moveHistory().substring(0, 5));
                try {
                    List<Move> ret = mState.lookupNextMove(mAssets.open(path));
                    if (!ret.isEmpty()) {
                        return ret;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return mState.minimaxMoves(null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mHumanPlayer = Player.values()[intent.getIntExtra(OptionsActivity.EXTRA_PLAYER, 0)];
        mGameDifficulty = Difficulty.values()[intent.getIntExtra(OptionsActivity.EXTRA_DIFFICULTY, 2)];

        progressBar = findViewById(R.id.progressBar);
        gameView = findViewById(R.id.gameView);
        gameView.setOnGameOverListener(res -> {
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
            mSnackbar.setAction(R.string.action_reset, view -> resetBoard());
            mSnackbar.show();
        });
        gameView.setOnInputListener(state -> {
            invalidateOptionsMenu();
            botPlay();
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

    @Override
    protected void onDestroy() {
        if (mMinimaxThread != null) {
            mMinimaxThread.interrupt();
        }
        super.onDestroy();
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
            gameView.pause();
            invalidateOptionsMenu();
            progressBar.setVisibility(View.VISIBLE);
            mMinimaxThread = new Thread(new MinimaxTask(this));
            mMinimaxThread.start();
        }
    }
    private void finishBotMove(List<Move> moves) {
        mMinimaxThread = null;
        applyMove(moves);
        gameView.resume();
        gameView.refresh();
        invalidateOptionsMenu();
        progressBar.setVisibility(View.INVISIBLE);
        botPlay();
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
