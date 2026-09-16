package com.gmail.smanis.konstantinos.qttt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.res.AssetManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OpeningBookAssetTest {
    @Test
    public void packagedOpeningBookProvidesInitialMoves() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();

        List<Move> moves = lookup(context, "0", new State());

        assertEquals(2, moves.size());
        assertEquals("0,8", moves.get(0).toShortString());
        assertEquals("2,6", moves.get(1).toShortString());
    }

    @Test
    public void packagedOpeningBookProvidesMovesForEveryTurn() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();

        State afterFirstMove = stateWith(new Move(0, 1, CellState.X1));
        assertTrue(lookup(context, "1", afterFirstMove).size() > 0);

        State afterSecondMove =
                stateWith(new Move(0, 1, CellState.X1), new Move(0, 1, CellState.O2));
        assertTrue(lookup(context, "2", afterSecondMove).size() > 0);

        State afterThirdMove =
                stateWith(
                        new Move(0, 1, CellState.X1),
                        new Move(0, 1, CellState.O2),
                        new Move(0, CellState.O2),
                        new Move(2, 3, CellState.X3));
        assertTrue(lookup(context, "3", afterThirdMove).size() > 0);

        State afterFourthMove =
                stateWith(
                        new Move(0, 1, CellState.X1),
                        new Move(0, 1, CellState.O2),
                        new Move(0, CellState.O2),
                        new Move(2, 3, CellState.X3),
                        new Move(2, 3, CellState.O4));
        List<Move> lateOpeningMoves = lookup(context, "4/(0,1)", afterFourthMove);
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

        assertTrue(lookup(context, "1", state).isEmpty());
        assertTrue(!state.minimaxMoves().isEmpty());
    }

    @Test
    public void packagedOpeningBookCorpusIsStructurallyValid() throws Exception {
        AssetManager assets = ApplicationProvider.getApplicationContext().getAssets();
        for (int turn = 0; turn < 4; ++turn) {
            assertValidBook(assets, String.valueOf(turn));
        }

        Set<String> expectedLateBooks = new HashSet<>();
        for (int first = 0; first < 8; ++first) {
            for (int second = first + 1; second < 9; ++second) {
                expectedLateBooks.add("(" + first + "," + second + ")");
            }
        }
        String[] lateBooks = assets.list("4");
        assertNotNull(lateBooks);
        assertEquals(expectedLateBooks, new HashSet<>(Arrays.asList(lateBooks)));
        for (String book : lateBooks) {
            assertValidBook(assets, "4/" + book);
        }
    }

    private static List<Move> lookup(Context context, String asset, State state) throws Exception {
        return state.lookupNextMove(context.getAssets().open(asset));
    }

    private static State stateWith(Move... moves) {
        State state = new State();
        for (Move move : moves) {
            state.applyMove(move);
        }
        return state;
    }

    private static void assertValidBook(AssetManager assets, String path) throws Exception {
        boolean sawPosition = false;
        boolean sawMove = false;
        try (BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(assets.open(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith("(")) {
                    assertTrue(
                            path + " contains a position without moves", !sawPosition || sawMove);
                    int separator = line.lastIndexOf(':');
                    assertTrue(path + " contains an invalid position", separator > 0);
                    assertNotNull(Utility.valueOf(line.substring(separator + 1)));
                    sawPosition = true;
                    sawMove = false;
                } else {
                    assertTrue(path + " contains a move before its position", sawPosition);
                    Move move = Move.valueOf(line);
                    assertNotNull(path + " contains an invalid move", move);
                    assertTrue(move.firstCellIndex() >= 0 && move.firstCellIndex() < 9);
                    if (move.type() == Move.Type.REGULAR) {
                        assertTrue(move.secondCellIndex() >= 0 && move.secondCellIndex() < 9);
                    }
                    sawMove = true;
                }
            }
        }
        assertTrue(path + " contains no positions", sawPosition);
        assertTrue(path + " ends with a position without moves", sawMove);
    }
}
