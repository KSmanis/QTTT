package com.gmail.smanis.konstantinos.qttt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OpeningBookAssetTest {
    @Test
    public void packagedOpeningBookProvidesInitialMoves() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();

        List<Move> moves = OpeningBook.lookup(context, new State());

        assertEquals(2, moves.size());
        assertEquals("0,8", moves.get(0).toShortString());
        assertEquals("2,6", moves.get(1).toShortString());
    }

    @Test
    public void packagedOpeningBookProvidesMovesForEveryTurn() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();

        State afterFirstMove = stateWith(new Move(0, 1, CellState.X1));
        assertTrue(OpeningBook.lookup(context, afterFirstMove).size() > 0);

        State afterSecondMove =
                stateWith(new Move(0, 1, CellState.X1), new Move(0, 1, CellState.O2));
        assertTrue(OpeningBook.lookup(context, afterSecondMove).size() > 0);

        State afterThirdMove =
                stateWith(
                        new Move(0, 1, CellState.X1),
                        new Move(0, 1, CellState.O2),
                        new Move(0, CellState.O2),
                        new Move(2, 3, CellState.X3));
        assertTrue(OpeningBook.lookup(context, afterThirdMove).size() > 0);

        State afterFourthMove =
                stateWith(
                        new Move(0, 1, CellState.X1),
                        new Move(0, 1, CellState.O2),
                        new Move(0, CellState.O2),
                        new Move(2, 3, CellState.X3),
                        new Move(2, 3, CellState.O4));
        List<Move> lateOpeningMoves = OpeningBook.lookup(context, afterFourthMove);
        assertEquals(1, lateOpeningMoves.size());
        assertEquals("2", lateOpeningMoves.get(0).toShortString());
    }

    @Test
    public void unmatchedOpeningBookPositionHasMinimaxFallback() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        State state =
                stateWith(
                        new Move(3, 7, CellState.X1),
                        new Move(2, 4, CellState.O2),
                        new Move(1, 7, CellState.X3),
                        new Move(1, 5, CellState.O4),
                        new Move(7, 8, CellState.X5),
                        new Move(4, 8, CellState.O6),
                        new Move(7, 8, CellState.X7),
                        new Move(7, CellState.X7));

        assertTrue(OpeningBook.lookup(context, state).isEmpty());
        assertTrue(!state.minimaxMoves().isEmpty());
    }

    @Test
    public void packagedOpeningBookIsCompleteAndValid() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        try (Cursor integrity =
                        OpeningBook.database(context).rawQuery("PRAGMA integrity_check", null);
                Cursor count =
                        OpeningBook.database(context)
                                .rawQuery("SELECT count(*) FROM opening_book", null)) {
            assertTrue(integrity.moveToFirst());
            assertEquals("ok", integrity.getString(0));
            assertTrue(count.moveToFirst());
            assertEquals(2_094_013, count.getInt(0));
        }
    }

    @Test
    public void installedOpeningBookReopensAfterProcessRestart() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        OpeningBook.database(context).close();
        OpeningBook.databaseTask = null;

        assertTrue(OpeningBook.database(context).isOpen());
    }

    private static State stateWith(Move... moves) {
        State state = new State();
        for (Move move : moves) {
            state.applyMove(move);
        }
        return state;
    }
}
