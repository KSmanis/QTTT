package com.gmail.smanis.konstantinos.qttt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import org.junit.Test;

public class StateTest {
    @Test
    public void startsWithAllOpeningMoves() {
        State state = new State();

        assertEquals(Player.X, state.currentPlayer());
        assertEquals(36, state.availableMoves(false).size());
        assertFalse(state.gameOver());
        assertEquals("()", state.moveHistory());
    }

    @Test
    public void inputMoveCanBeUndone() {
        State state = new State();

        assertTrue(state.applyInput(0));
        assertTrue(state.hasIncompleteInput());
        assertTrue(state.applyInput(1));
        assertFalse(state.hasIncompleteInput());
        assertEquals(1, state.currentTurn());
        assertEquals(CellState.X1, state.quantumBoard().get(0).iterator().next());
        assertTrue(state.quantumBoard().get(1).contains(CellState.X1));

        state.undoLastMove();

        assertEquals(0, state.currentTurn());
        assertEquals("()", state.moveHistory());
        assertTrue(state.quantumBoard().get(0).isEmpty());
        assertTrue(state.quantumBoard().get(1).isEmpty());
    }

    @Test
    public void collapseCanBeUndone() {
        State state = new State();
        state.applyMove(new Move(0, 1, CellState.X1));
        state.applyMove(new Move(1, 2, CellState.O2));
        state.applyMove(new Move(0, 2, CellState.X3));
        List<CellState> classicBeforeCollapse = new ArrayList<>(state.classicBoard());
        List<EnumSet<CellState>> quantumBeforeCollapse = quantumSnapshot(state);
        String historyBeforeCollapse = state.moveHistory();

        assertTrue(state.entangled());
        List<Move> collapseMoves = state.availableMoves(false);
        assertEquals(2, collapseMoves.size());

        state.applyMove(collapseMoves.get(0));

        assertFalse(state.entangled());
        assertTrue(state.classicBoard().subList(0, 3).stream().allMatch(cell -> cell != null));
        assertTrue(state.classicBoard().subList(3, 9).stream().allMatch(cell -> cell == null));

        state.undoLastMove();

        assertTrue(state.entangled());
        assertEquals(classicBeforeCollapse, state.classicBoard());
        assertEquals(quantumBeforeCollapse, state.quantumBoard());
        assertEquals(historyBeforeCollapse, state.moveHistory());
        assertEquals(3, state.currentTurn());
    }

    @Test
    public void rejectsOccupiedInputAndCancelsDuplicateSelection() {
        State state = new State();

        assertTrue(state.applyInput(0));
        assertTrue(state.hasIncompleteInput());
        assertTrue(state.applyInput(0));
        assertFalse(state.hasIncompleteInput());
        assertTrue(state.quantumBoard().get(0).isEmpty());

        state.applyMove(new Move(0, 1, CellState.X1));
        state.applyMove(new Move(1, 2, CellState.O2));
        state.applyMove(new Move(0, 2, CellState.X3));
        state.applyMove(state.availableMoves(false).get(0));

        assertFalse(state.applyInput(0));
    }

    @Test
    public void resetClearsAnInProgressGame() {
        State state = new State();
        state.applyMove(new Move(0, 1, CellState.X1));
        state.applyMove(new Move(2, 3, CellState.O2));

        state.reset();

        assertEquals(0, state.currentTurn());
        assertNull(state.lastMove());
        assertFalse(state.isUndoAvailable());
        assertTrue(state.classicBoard().stream().allMatch(cell -> cell == null));
        assertTrue(state.quantumBoard().stream().allMatch(cell -> cell.isEmpty()));
    }

    @Test
    public void evaluatesACompleteWinFromTheBoard() {
        State state =
                play(
                        new Move(0, 5, CellState.X1),
                        new Move(3, 7, CellState.O2),
                        new Move(2, 8, CellState.X3),
                        new Move(2, 7, CellState.O4),
                        new Move(6, 7, CellState.X5),
                        new Move(0, 7, CellState.O6),
                        new Move(0, 7, CellState.X7),
                        new Move(0, CellState.X7),
                        new Move(1, 4, CellState.O8),
                        new Move(1, 4, CellState.X9),
                        new Move(4, CellState.X9));

        assertTrue(state.gameOver());
        assertEquals(GameResult.PlayerResult.COMPLETE_WIN, state.result().xResult());
        assertEquals(Player.X, state.result().winner());
    }

    @Test
    public void evaluatesADrawFromTheBoard() {
        State state =
                play(
                        new Move(2, 6, CellState.X1),
                        new Move(2, 8, CellState.O2),
                        new Move(0, 4, CellState.X3),
                        new Move(0, 4, CellState.O4),
                        new Move(4, CellState.O4),
                        new Move(3, 6, CellState.X5),
                        new Move(1, 7, CellState.O6),
                        new Move(5, 8, CellState.X7),
                        new Move(3, 6, CellState.O8),
                        new Move(3, CellState.O8),
                        new Move(1, 7, CellState.X9),
                        new Move(7, CellState.X9));

        assertTrue(state.gameOver());
        assertTrue(state.result().draw());
        assertNull(state.result().winner());
    }

    @Test
    public void evaluatesEveryTerminalResultAndWinningCells() {
        assertTerminalResult(
                GameResult.PlayerResult.LOSS,
                Player.O,
                Arrays.asList(2, 4, 6),
                "3,7",
                "2,4",
                "1,7",
                "1,5",
                "7,8",
                "4,8",
                "7,8",
                "7",
                "0,6",
                "0,6",
                "0");
        assertTerminalResult(
                GameResult.PlayerResult.DOUBLE_COMPLETE_WIN,
                Player.X,
                Arrays.asList(3, 4, 5, 0, 4, 8),
                "4,5",
                "2,4",
                "0,4",
                "1,4",
                "0,4",
                "0",
                "3,7",
                "6,8",
                "3,6",
                "3,7",
                "3");
        assertTerminalResult(
                GameResult.PlayerResult.NARROW_WIN_SECOND,
                Player.O,
                Arrays.asList(0, 1, 2, 3, 4, 5),
                "0,2",
                "4,7",
                "2,4",
                "5,7",
                "7,8",
                "1,3",
                "4,8",
                "8",
                "1,6",
                "1,6",
                "1");
        assertTerminalResult(
                GameResult.PlayerResult.NARROW_WIN_FIRST,
                Player.X,
                Arrays.asList(0, 3, 6, 2, 5, 8),
                "3,4",
                "2,8",
                "1,4",
                "0,4",
                "0,2",
                "5,8",
                "3,6",
                "6,8",
                "8");
    }

    @Test
    public void parsesOpeningBookMovesForTheCurrentPosition() {
        State state = new State();
        String openingBook = "():1,13\n0,8\n2,6\n\n";

        List<Move> moves =
                state.lookupNextMove(
                        new ByteArrayInputStream(openingBook.getBytes(StandardCharsets.UTF_8)));

        assertEquals(2, moves.size());
        assertEquals("0,8", moves.get(0).toShortString());
        assertEquals(CellState.X1, moves.get(0).cellState());
        assertEquals(1, moves.get(0).utility().value());
        assertEquals(13, moves.get(0).utility().depth());
        assertEquals("2,6", moves.get(1).toShortString());
    }

    @Test
    public void minimaxSelectsTheUniqueBestEndgameMove() {
        State state = play("3,7", "2,4", "1,7", "1,5", "7,8", "4,8", "7,8", "7");

        Move move = state.minimaxMoves().get(0);

        assertEquals(Player.O, state.currentPlayer());
        assertEquals(Move.Type.REGULAR, move.type());
        assertEquals("0,6", move.toShortString());
        assertEquals(-2, move.utility().value());
    }

    private static State play(Move... moves) {
        State state = new State();
        for (Move move : moves) {
            state.applyMove(move);
        }
        return state;
    }

    private static State play(String... encodedMoves) {
        State state = new State();
        for (String encodedMove : encodedMoves) {
            Move move = Move.valueOf(encodedMove);
            int markIndex = state.currentTurn() - (move.type() == Move.Type.COLLAPSE ? 1 : 0);
            move.setCellState(CellState.values()[markIndex]);
            state.applyMove(move);
        }
        return state;
    }

    private static void assertTerminalResult(
            GameResult.PlayerResult expectedResult,
            Player expectedWinner,
            List<Integer> expectedWinningCells,
            String... encodedMoves) {
        State state = play(encodedMoves);

        assertTrue(state.gameOver());
        assertEquals(expectedResult, state.result().xResult());
        assertEquals(expectedWinner, state.result().winner());
        assertEquals(expectedWinningCells, state.winningCells());
    }

    private static List<EnumSet<CellState>> quantumSnapshot(State state) {
        List<EnumSet<CellState>> snapshot = new ArrayList<>();
        for (EnumSet<CellState> cell : state.quantumBoard()) {
            EnumSet<CellState> copy = EnumSet.noneOf(CellState.class);
            copy.addAll(cell);
            snapshot.add(copy);
        }
        return snapshot;
    }
}
