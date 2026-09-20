package com.gmail.smanis.konstantinos.qttt;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TutorialActivity extends AppCompatActivity {
    private static final String SAVED_STEP = "tutorial.step";
    private static final String SAVED_HISTORY = "tutorial.history";
    private static final String SAVED_PENDING_INPUTS = "tutorial.pendingInputs";

    private static final int[] TITLES = {
        R.string.tutorial_superposition_title,
        R.string.tutorial_entanglement_title,
        R.string.tutorial_cycle_title,
        R.string.tutorial_measurement_title,
        R.string.tutorial_collapse_title,
        R.string.tutorial_complete_win_title,
        R.string.tutorial_loss_title,
        R.string.tutorial_draw_title,
        R.string.tutorial_narrow_first_title,
        R.string.tutorial_double_win_title
    };

    private static final int[] BODIES = {
        R.string.tutorial_superposition_body,
        R.string.tutorial_entanglement_body,
        R.string.tutorial_cycle_body,
        R.string.tutorial_measurement_body,
        R.string.tutorial_collapse_body,
        R.string.tutorial_complete_win_body,
        R.string.tutorial_loss_body,
        R.string.tutorial_draw_body,
        R.string.tutorial_narrow_first_body,
        R.string.tutorial_double_win_body
    };

    private static final GameResult[] RESULTS = {
        GameResult.COMPLETE_WIN,
        GameResult.LOSS,
        GameResult.DRAW,
        GameResult.NARROW_WIN_FIRST,
        GameResult.DOUBLE_COMPLETE_WIN
    };

    private static final String[] RESULT_HISTORIES = {
        "(0,1)(1,4)(4,5)(2,4)(0,4)(4)(6,7)(6,7)(7)(3,8)(3,8)(3)",
        "(0,1)(1,4)(3,7)(2,4)(1,7)(1,5)(7,8)(4,8)(8)",
        "(0,1)(1,4)(6,7)(4,7)(2,7)(6,7)(6)(3,8)(5,8)(3,8)(8)",
        "(0,1)(1,4)(1,3)(3,7)(6,7)(2,6)(3,5)(4,7)(4)",
        "(0,1)(1,4)(0,8)(3,7)(4,5)(2,6)(0,2)(7,8)(4,8)(4)"
    };

    private GameView gameView;
    private TextView progress;
    private TextView title;
    private TextView body;
    private Button back;
    private Button next;
    private final State[] states = new State[TITLES.length];
    private int step;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gameView = findViewById(R.id.gameView);
        progress = findViewById(R.id.tutorial_progress);
        title = findViewById(R.id.tutorial_title);
        body = findViewById(R.id.tutorial_body);
        body.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        back = findViewById(R.id.tutorial_back);
        next = findViewById(R.id.tutorial_next);

        if (savedInstanceState == null) {
            states[0] = new State();
        } else {
            step = savedInstanceState.getInt(SAVED_STEP);
            String[] histories = savedInstanceState.getStringArray(SAVED_HISTORY);
            int[] pendingInputs = savedInstanceState.getIntArray(SAVED_PENDING_INPUTS);
            for (int i = 0; i < histories.length; ++i) {
                if (histories[i] != null) {
                    states[i] = new State();
                    states[i].restore(histories[i]);
                    if (pendingInputs != null && pendingInputs[i] > 0) {
                        states[i].applyInput(pendingInputs[i] - 1);
                    }
                }
            }
        }

        gameView.setOnInputListener(this::applyInput);
        gameView.setInputPredicate(this::isInputAllowed);
        back.setOnClickListener(view -> showStep(step - 1));
        next.setOnClickListener(
                view -> {
                    if (step == TITLES.length - 1) {
                        finish();
                    } else {
                        showStep(step + 1, true);
                    }
                });
        render();
    }

    private boolean applyInput(int cellIndex) {
        if (!isInputAllowed(cellIndex) || !state().applyInput(cellIndex)) {
            return false;
        }
        render();
        return true;
    }

    private boolean isInputAllowed(int cellIndex) {
        return interactionAllowed()
                && (step != 1 || isEntanglementCell(cellIndex))
                && (step != 2 || isCycleCell(cellIndex));
    }

    private boolean interactionAllowed() {
        return interactionPending() || (step == 4 && !state().gameOver());
    }

    private boolean interactionPending() {
        if (step == 0) {
            return state().currentTurn() == 0;
        }
        if (step == 1) {
            return state().currentTurn() == 1;
        }
        if (step == 2) {
            return !state().entangled();
        }
        return step == 3 && state().entangled();
    }

    private boolean isEntanglementCell(int cellIndex) {
        Move x1 = state().lastMove();
        Integer pending = state().pendingInputCell();
        if (pending == null) {
            return cellIndex == x1.firstCellIndex() || cellIndex == x1.secondCellIndex();
        }
        int otherX = pending == x1.firstCellIndex() ? x1.secondCellIndex() : x1.firstCellIndex();
        return cellIndex != otherX;
    }

    private boolean isCycleCell(int cellIndex) {
        Move o2 = state().lastMove();
        Move x1 = o2.previousMove();
        int sharedCell =
                o2.firstCellIndex() == x1.firstCellIndex()
                                || o2.firstCellIndex() == x1.secondCellIndex()
                        ? o2.firstCellIndex()
                        : o2.secondCellIndex();
        int xOnly = x1.firstCellIndex() == sharedCell ? x1.secondCellIndex() : x1.firstCellIndex();
        int oOnly = o2.firstCellIndex() == sharedCell ? o2.secondCellIndex() : o2.firstCellIndex();
        return cellIndex == xOnly || cellIndex == oOnly;
    }

    private void showStep(int newStep) {
        showStep(newStep, false);
    }

    private void showStep(int newStep, boolean forward) {
        if (!forward) {
            for (int i = newStep + 1; i < states.length; ++i) {
                states[i] = null;
            }
            if (newStep == 0) {
                states[0] = new State();
            } else if (newStep == 1 || newStep == 2 || newStep == 3) {
                states[newStep] = advance(newStep, states[newStep - 1]);
            }
        }
        if (forward && states[newStep] == null) {
            states[newStep] =
                    newStep < 5 ? advance(newStep, state()) : resultState(RESULTS[newStep - 5]);
        }
        step = newStep;
        if (step >= TITLES.length - RESULTS.length) {
            gameView.hideHistory();
        }
        render();
    }

    private void render() {
        progress.setText(getString(R.string.tutorial_progress, step + 1, TITLES.length));
        title.setText(TITLES[step]);
        body.setText(BODIES[step]);
        back.setVisibility(step == 0 ? View.INVISIBLE : View.VISIBLE);
        next.setText(step == TITLES.length - 1 ? R.string.tutorial_done : R.string.tutorial_next);
        next.setEnabled(!interactionPending());
        gameView.setPaused(!interactionAllowed());
        gameView.render(state());
    }

    static State advance(int nextStep, State current) {
        return new State(current);
    }

    static State resultState(GameResult target) {
        for (int i = 0; i < RESULTS.length; ++i) {
            if (RESULTS[i] == target) {
                State state = new State();
                state.restore(RESULT_HISTORIES[i]);
                return state;
            }
        }
        throw new IllegalArgumentException("Unsupported tutorial result: " + target);
    }

    private State state() {
        return states[step];
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_STEP, step);
        String[] histories = new String[states.length];
        int[] pendingInputs = new int[states.length];
        for (int i = 0; i < states.length; ++i) {
            if (states[i] != null) {
                histories[i] = states[i].moveHistory();
                Integer pendingInput = states[i].pendingInputCell();
                if (pendingInput != null) {
                    pendingInputs[i] = pendingInput + 1;
                }
            }
        }
        outState.putStringArray(SAVED_HISTORY, histories);
        outState.putIntArray(SAVED_PENDING_INPUTS, pendingInputs);
    }
}
