package com.gmail.smanis.konstantinos.qttt;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CancellationException;

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

public class GameViewModel extends AndroidViewModel {
    private static final String STATE_HISTORY = "game.state.history";
    private static final String STATE_PENDING_INPUT = "game.state.pendingInput";

    private final SavedStateHandle savedState;
    private final State state = new State();
    private final MutableLiveData<State> snapshots = new MutableLiveData<>();
    private final MutableLiveData<Boolean> thinking = new MutableLiveData<>(false);
    private final Random rng = new Random();
    private Thread aiThread;
    private long aiGeneration;

    public GameViewModel(@NonNull Application application, SavedStateHandle savedState) {
        super(application);
        this.savedState = savedState;
        state.restore(savedState.get(STATE_HISTORY));
        Integer pendingInput = savedState.get(STATE_PENDING_INPUT);
        if (pendingInput != null) {
            state.applyInput(pendingInput);
        }
        publish();
    }

    LiveData<State> snapshots() {
        return snapshots;
    }

    LiveData<Boolean> thinking() {
        return thinking;
    }

    boolean applyInput(int cellIndex) {
        if (!state.applyInput(cellIndex)) {
            return false;
        }
        publish();
        return true;
    }

    void applyMove(Move move) {
        state.applyMove(move);
        publish();
    }

    State snapshot() {
        return new State(state);
    }

    void reset() {
        cancelAi();
        state.reset();
        publish();
    }

    void undo() {
        cancelAi();
        state.undoLastMove();
        publish();
    }

    void undoSinglePlayer(Player humanPlayer) {
        cancelAi();
        if (state.hasIncompleteInput()) {
            state.undoLastMove();
        } else {
            while (state.currentPlayer() == humanPlayer) {
                state.undoLastMove();
            }
            while (state.currentPlayer() != humanPlayer) {
                state.undoLastMove();
            }
        }
        publish();
    }

    void playBot(Player humanPlayer, Difficulty difficulty) {
        if (aiThread != null || state.currentPlayer() == humanPlayer || state.gameOver()) {
            return;
        }

        if (difficulty == Difficulty.Random) {
            applyBotMove(null, difficulty);
            publish();
            playBot(humanPlayer, difficulty);
            return;
        }

        thinking.setValue(true);
        long generation = ++aiGeneration;
        State stateSnapshot = new State(state);
        aiThread =
                new Thread(
                        () -> {
                            try {
                                List<Move> moves = findMoves(stateSnapshot);
                                ContextCompat.getMainExecutor(getApplication())
                                        .execute(
                                                () ->
                                                        finishBotMove(
                                                                generation,
                                                                humanPlayer,
                                                                difficulty,
                                                                moves));
                            } catch (CancellationException ignored) {
                                // Cancellation is expected after reset, undo, or final destruction.
                            }
                        });
        aiThread.start();
    }

    private List<Move> findMoves(State stateSnapshot) {
        int turn = stateSnapshot.currentTurn();
        if (turn < 5) {
            String path =
                    turn < 4
                            ? String.valueOf(turn)
                            : "4/" + stateSnapshot.moveHistory().substring(0, 5);
            try {
                List<Move> moves =
                        stateSnapshot.lookupNextMove(getApplication().getAssets().open(path));
                if (!moves.isEmpty()) {
                    return moves;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stateSnapshot.minimaxMoves();
    }

    private void finishBotMove(
            long generation, Player humanPlayer, Difficulty difficulty, List<Move> optimalMoves) {
        if (generation != aiGeneration || aiThread == null) {
            return;
        }

        aiThread = null;
        applyBotMove(optimalMoves, difficulty);
        publish();
        thinking.setValue(false);
        playBot(humanPlayer, difficulty);
    }

    private void applyBotMove(List<Move> optimalMoves, Difficulty difficulty) {
        List<Move> allMoves = state.availableMoves(false);
        List<Move> pool = optimalMoves;
        switch (difficulty) {
            case Random:
                pool = allMoves;
                break;
            case Easy:
                if (rng.nextFloat() >= 0.5) {
                    pool = allMoves;
                }
                break;
            case Medium:
                if (rng.nextFloat() >= 0.75) {
                    pool = allMoves;
                }
                break;
            case Hard:
                if (rng.nextFloat() >= 0.9 && !state.entangled()) {
                    pool = allMoves;
                }
                break;
            case Optimal:
                break;
        }
        state.applyMove(pool.get(rng.nextInt(pool.size())));
    }

    private void publish() {
        savedState.set(STATE_HISTORY, state.moveHistory());
        savedState.set(STATE_PENDING_INPUT, state.pendingInputCell());
        snapshots.setValue(new State(state));
    }

    private void cancelAi() {
        ++aiGeneration;
        if (aiThread != null) {
            aiThread.interrupt();
            aiThread = null;
        }
        thinking.setValue(false);
    }

    @Override
    protected void onCleared() {
        cancelAi();
    }
}
