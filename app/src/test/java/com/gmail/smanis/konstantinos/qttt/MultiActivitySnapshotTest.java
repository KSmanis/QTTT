package com.gmail.smanis.konstantinos.qttt;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import app.cash.paparazzi.Paparazzi;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MultiActivitySnapshotTest {
    @Rule
    public final Paparazzi paparazzi = new Paparazzi();

    @Test
    public void emptyBoard() {
        paparazzi.snapshot(boardView());
    }

    @Test
    public void firstMove() {
        View view = boardView();
        GameView gameView = view.findViewById(R.id.gameView);
        gameView.state().applyMove(new Move(0, 1, CellState.X1));
        gameView.refresh();

        paparazzi.snapshot(view);
    }

    @Test
    public void entangledMove() {
        View view = boardView();
        GameView gameView = view.findViewById(R.id.gameView);
        gameView.state().applyMove(new Move(0, 1, CellState.X1));
        gameView.state().applyMove(new Move(1, 2, CellState.O2));
        gameView.state().applyMove(new Move(0, 2, CellState.X3));
        gameView.refresh();

        assertTrue(gameView.state().entangled());
        paparazzi.snapshot(view);
    }

    @Test
    public void gameOver() {
        View view = boardView();
        GameView gameView = view.findViewById(R.id.gameView);
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
            gameView.state().applyMove(move);
        }
        gameView.refresh();

        assertTrue(gameView.state().gameOver());
        assertEquals(GameResult.PlayerResult.LOSS, gameView.state().result().xResult());
        paparazzi.snapshot(view);
    }

    @Test
    public void collapsedMoveHistory() {
        View view = boardView();
        GameView gameView = view.findViewById(R.id.gameView);
        gameView.state().applyMove(new Move(0, 1, CellState.X1));
        gameView.state().applyMove(new Move(0, 1, CellState.O2));
        gameView.state().applyMove(new Move(0, CellState.O2));
        gameView.refresh();
        gameView.layout(0, 0, 900, 900);

        long now = SystemClock.uptimeMillis();
        MotionEvent tap = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, 150, 150, 0);
        gameView.onTouchEvent(tap);
        tap.recycle();

        assertEquals(CellState.O2, gameView.state().classicBoard().get(0));
        paparazzi.snapshot(view);
    }

    private View boardView() {
        paparazzi.unsafeUpdateConfig(null, "Theme.AppCompat.Light.NoActionBar", null);
        return paparazzi.inflate(R.layout.activity_multi);
    }
}
