package com.gmail.smanis.konstantinos.qttt;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

public class SingleActivity extends AppCompatActivity {
    private static final String STATE_HISTORY = "single.state.history";

    enum Difficulty {
        Random(0),
        Easy(1),
        Medium(2),
        Hard(3),
        Optimal(4);

        private final int id;

        Difficulty(int id) {
            this.id = id;
        }

        int id() {
            return id;
        }

        static Difficulty fromId(int id) {
            for (Difficulty difficulty : values()) {
                if (difficulty.id == id) {
                    return difficulty;
                }
            }
            throw new IllegalArgumentException("Unknown difficulty id: " + id);
        }
    }

    private ProgressBar progressBar;
    private GameView gameView;
    private State state;
    private Snackbar mSnackbar;
    private Player mHumanPlayer;
    private Difficulty mGameDifficulty;
    private Random mRng;
    private Thread mMinimaxThread;
    private long mMinimaxGeneration;
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private static class MinimaxTask implements Runnable {
        private final WeakReference<SingleActivity> mActivity;
        private final State mState;
        private final AssetManager mAssets;
        private final long mGeneration;

        MinimaxTask(SingleActivity activity, long generation) {
            mActivity = new WeakReference<>(activity);
            mState = new State(activity.state);
            mAssets = activity.getApplicationContext().getAssets();
            mGeneration = generation;
        }

        @Override
        public void run() {
            List<Move> moves = findMoves();
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            MAIN_HANDLER.post(
                    () -> {
                        SingleActivity activity = mActivity.get();
                        if (activity != null
                                && !activity.isFinishing()
                                && !activity.isDestroyed()) {
                            activity.finishBotMove(mGeneration, moves);
                        }
                    });
        }

        private List<Move> findMoves() {
            int turn = mState.currentTurn();
            if (turn < 5) {
                String path =
                        (turn < 4
                                ? String.valueOf(turn)
                                : "4/" + mState.moveHistory().substring(0, 5));
                try {
                    List<Move> ret = mState.lookupNextMove(mAssets.open(path));
                    if (!ret.isEmpty()) {
                        return ret;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return mState.minimaxMoves();
        }
    }

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

        progressBar = findViewById(R.id.progressBar);
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
        gameView.setOnInputListener(
                () -> {
                    invalidateOptionsMenu();
                    botPlay();
                });
        state = gameView.state();
        if (savedInstanceState != null) {
            state.restore(savedInstanceState.getString(STATE_HISTORY));
            gameView.refresh();
        }
        mRng = new Random();

        botPlay();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_HISTORY, state.moveHistory());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!gameView.isPaused()) {
            getMenuInflater().inflate(R.menu.menu_single, menu);
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

    @Override
    protected void onDestroy() {
        cancelMinimax();
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

    void botPlay() {
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
            long generation = ++mMinimaxGeneration;
            mMinimaxThread = new Thread(new MinimaxTask(this, generation));
            mMinimaxThread.start();
        }
    }

    private void finishBotMove(long generation, List<Move> moves) {
        if (generation != mMinimaxGeneration || isFinishing() || isDestroyed()) {
            return;
        }

        mMinimaxThread = null;
        applyMove(moves);
        gameView.resume();
        gameView.refresh();
        invalidateOptionsMenu();
        progressBar.setVisibility(View.INVISIBLE);
        botPlay();
    }

    void resetBoard() {
        cancelMinimax();
        state.reset();
        gameView.resume();
        gameView.refresh();
        invalidateOptionsMenu();
        if (mSnackbar != null) {
            mSnackbar.dismiss();
            mSnackbar = null;
        }
        botPlay();
    }

    void undoMove() {
        cancelMinimax();
        gameView.resume();
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

    private void cancelMinimax() {
        ++mMinimaxGeneration;
        if (mMinimaxThread != null) {
            mMinimaxThread.interrupt();
            mMinimaxThread = null;
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
