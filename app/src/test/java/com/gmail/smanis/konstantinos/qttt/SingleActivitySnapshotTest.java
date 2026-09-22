package com.gmail.smanis.konstantinos.qttt;

import static org.junit.Assert.assertTrue;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import app.cash.paparazzi.Paparazzi;
import com.google.android.material.snackbar.Snackbar;
import org.junit.Rule;
import org.junit.Test;

public class SingleActivitySnapshotTest {
    @Rule public final Paparazzi paparazzi = new Paparazzi();

    @Test
    public void gameOverSnackbar() {
        paparazzi.unsafeUpdateConfig(null, "Theme.AppCompat.Light.NoActionBar", null);
        View view = paparazzi.inflate(R.layout.activity_single);
        GameView gameView = view.findViewById(R.id.gameView);
        State state = new State();
        Move[] moves = {
            new Move(3, 7, CellState.X1),
            new Move(2, 4, CellState.O2),
            new Move(1, 7, CellState.X3),
            new Move(1, 5, CellState.O4),
            new Move(7, 8, CellState.X5),
            new Move(4, 8, CellState.O6),
            new Move(7, 8, CellState.X7),
            new Move(7, CellState.X7),
            new Move(0, 6, CellState.O8),
            new Move(0, 6, CellState.X9),
            new Move(0, CellState.X9)
        };
        for (Move move : moves) {
            state.applyMove(move);
        }
        gameView.render(state);

        assertTrue(state.gameOver());
        Snackbar snackbar =
                Snackbar.make(
                                gameView,
                                view.getResources()
                                        .getString(R.string.result_winner, state.result().winner()),
                                Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_reset, ignored -> {});
        CoordinatorLayout.LayoutParams layoutParams =
                new CoordinatorLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        ((ViewGroup) view).addView(snackbar.getView(), layoutParams);
        paparazzi.snapshot(view);
    }
}
