package com.gmail.smanis.konstantinos.qttt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeProviderCompat;
import app.cash.paparazzi.Paparazzi;
import org.junit.Rule;
import org.junit.Test;

public class MultiActivitySnapshotTest {
    @Rule public final Paparazzi paparazzi = new Paparazzi();

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
        tap.setAction(MotionEvent.ACTION_UP);
        gameView.onTouchEvent(tap);
        tap.recycle();

        assertEquals(CellState.O2, gameView.state().classicBoard().get(0));
        paparazzi.snapshot(view);
    }

    @Test
    public void boardCellsAcceptAccessibilityClicks() {
        GameView gameView = boardView().findViewById(R.id.gameView);
        gameView.layout(0, 0, 900, 900);

        AccessibilityDelegateCompat delegate = ViewCompat.getAccessibilityDelegate(gameView);
        assertNotNull(delegate);
        AccessibilityNodeProviderCompat provider = delegate.getAccessibilityNodeProvider(gameView);
        assertNotNull(provider);
        AccessibilityNodeInfoCompat firstCell = provider.createAccessibilityNodeInfo(0);
        assertNotNull(firstCell);
        assertEquals(
                gameView.getResources()
                        .getString(
                                R.string.game_cell_description,
                                1,
                                1,
                                gameView.getResources().getString(R.string.game_cell_empty)),
                firstCell.getContentDescription());
        assertTrue(
                provider.performAction(
                        0,
                        AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK.getId(),
                        null));
        assertTrue(
                provider.performAction(
                        1,
                        AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK.getId(),
                        null));
        assertEquals(1, gameView.state().currentTurn());
    }

    private View boardView() {
        paparazzi.unsafeUpdateConfig(null, "Theme.AppCompat.Light.NoActionBar", null);
        return paparazzi.inflate(R.layout.activity_multi);
    }
}
