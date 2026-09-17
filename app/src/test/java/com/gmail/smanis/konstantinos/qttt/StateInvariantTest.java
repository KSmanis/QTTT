package com.gmail.smanis.konstantinos.qttt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

public class StateInvariantTest {
    @Test
    public void seededLegalSequencesPreserveStateInvariants() {
        boolean sawEntanglement = false;
        boolean sawCollapse = false;
        EnumSet<GameResult> results = EnumSet.noneOf(GameResult.class);
        for (int seed = 0; seed < 128; ++seed) {
            State state = new State();
            String initial = snapshot(state);
            Random random = new Random(seed);

            while (!state.gameOver()) {
                List<Move> moves = state.availableMoves(false);
                assertFalse("seed " + seed, moves.isEmpty());
                assertAvailableMovesAreLegal(state, moves);
                sawEntanglement |= state.entangled();
                Move move = moves.get(random.nextInt(moves.size()));
                sawCollapse |= move.type() == Move.Type.COLLAPSE;
                state.applyMove(move);
                assertStateIsValid(state);
            }
            results.add(state.result());

            while (state.isUndoAvailable()) {
                state.undoLastMove();
            }
            assertEquals("seed " + seed, initial, snapshot(state));
        }

        assertTrue(sawEntanglement);
        assertTrue(sawCollapse);
        assertEquals(EnumSet.complementOf(EnumSet.of(GameResult.INVALID)), results);
    }

    private static void assertAvailableMovesAreLegal(State state, List<Move> moves) {
        Set<String> distinctMoves = new HashSet<>();
        for (Move move : moves) {
            assertTrue(distinctMoves.add(move.type() + ":" + move.toShortString()));
            if (move.type() == Move.Type.REGULAR) {
                assertTrue(move.firstCellIndex() >= 0 && move.firstCellIndex() < 9);
                assertTrue(move.secondCellIndex() >= 0 && move.secondCellIndex() < 9);
                assertTrue(state.classicBoard().get(move.firstCellIndex()) == null);
                assertTrue(state.classicBoard().get(move.secondCellIndex()) == null);
                assertEquals(CellState.values()[state.currentTurn()], move.cellState());
            } else {
                assertTrue(state.entangled());
                assertNotNull(state.lastMove());
                assertEquals(state.lastMove().cellState(), move.cellState());
                assertTrue(
                        move.firstCellIndex() == state.lastMove().firstCellIndex()
                                || move.firstCellIndex() == state.lastMove().secondCellIndex());
            }
        }
    }

    private static void assertStateIsValid(State state) {
        assertTrue(state.currentTurn() >= 0 && state.currentTurn() <= 9);
        for (int i = 0; i < 9; ++i) {
            CellState classicCell = state.classicBoard().get(i);
            EnumSet<CellState> quantumCell = state.quantumBoard().get(i);
            if (classicCell != null) {
                assertTrue(quantumCell.contains(classicCell));
            }
        }

        for (CellState mark : CellState.values()) {
            int occurrences = 0;
            for (EnumSet<CellState> quantumCell : state.quantumBoard()) {
                if (quantumCell.contains(mark)) {
                    ++occurrences;
                }
            }
            assertTrue(
                    occurrences == 0
                            || occurrences == 2
                            || (mark == CellState.X9 && occurrences == 1));
        }

        if (state.gameOver()) {
            assertTrue(state.availableMoves(false).isEmpty());
        }
    }

    private static String snapshot(State state) {
        StringBuilder result = new StringBuilder();
        result.append(state.currentTurn()).append('|').append(state.moveHistory()).append('|');
        for (CellState cell : state.classicBoard()) {
            result.append(cell == null ? '-' : cell.name());
        }
        result.append('|');
        for (EnumSet<CellState> cell : state.quantumBoard()) {
            for (CellState mark : CellState.values()) {
                if (cell.contains(mark)) {
                    result.append(mark.name()).append(',');
                }
            }
            result.append(';');
        }
        return result.toString();
    }
}
