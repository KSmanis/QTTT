package com.gmail.smanis.konstantinos.qttt;

import static org.junit.Assert.assertTrue;

import android.view.View;
import android.widget.TextView;
import app.cash.paparazzi.Paparazzi;
import org.junit.Rule;
import org.junit.Test;

public class TutorialActivitySnapshotTest {
    @Rule public final Paparazzi paparazzi = new Paparazzi();

    @Test
    public void cyclicEntanglementLesson() {
        paparazzi.unsafeUpdateConfig(null, "Theme.AppCompat.Light.NoActionBar", null);
        View view = paparazzi.inflate(R.layout.activity_tutorial);
        ((TextView) view.findViewById(R.id.tutorial_progress))
                .setText(view.getResources().getString(R.string.tutorial_progress, 3, 10));
        ((TextView) view.findViewById(R.id.tutorial_title)).setText(R.string.tutorial_cycle_title);
        ((TextView) view.findViewById(R.id.tutorial_body)).setText(R.string.tutorial_cycle_body);

        State state = new State();
        state.applyMove(new Move(0, 1, CellState.X1));
        state.applyMove(new Move(1, 4, CellState.O2));
        state.applyMove(new Move(0, 4, CellState.X3));
        assertTrue(state.entangled());
        GameView gameView = view.findViewById(R.id.gameView);
        gameView.setAnimationEnabled(false);
        gameView.render(state);

        paparazzi.snapshot(view);
        assertTrue(gameView.getHeight() > 0);
    }
}
