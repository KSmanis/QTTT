package com.gmail.smanis.konstantinos.qttt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TutorialActivityTest {
    @Test
    public void lessonContinuesFromEveryPossibleFirstMove() {
        for (int firstCell = 0; firstCell < 8; ++firstCell) {
            for (int secondCell = firstCell + 1; secondCell < 9; ++secondCell) {
                assertLessonCompletes(firstCell, secondCell, false);
                assertLessonCompletes(firstCell, secondCell, true);
            }
        }
    }

    @Test
    public void everyResultUsesAFixedLegalState() {
        GameResult[] results = {
            GameResult.COMPLETE_WIN,
            GameResult.LOSS,
            GameResult.NARROW_WIN_FIRST,
            GameResult.DOUBLE_COMPLETE_WIN,
            GameResult.DRAW
        };
        for (GameResult result : results) {
            State finalState = TutorialActivity.resultState(result);
            assertEquals(result, finalState.result());
            if (result == GameResult.COMPLETE_WIN || result == GameResult.LOSS) {
                assertOrdinaryWinEndsAtWinningMark(finalState);
            }
        }
    }

    private static void assertOrdinaryWinEndsAtWinningMark(State state) {
        int winningMark = -1;
        for (int cell : state.winningCells()) {
            winningMark = Math.max(winningMark, state.classicBoard().get(cell).ordinal());
        }
        int lastClassicalMark = -1;
        for (CellState mark : state.classicBoard()) {
            if (mark != null) {
                lastClassicalMark = Math.max(lastClassicalMark, mark.ordinal());
            }
        }
        assertEquals(winningMark, lastClassicalMark);
    }

    private static void assertLessonCompletes(
            int firstCell, int secondCell, boolean chooseSecondCollapse) {
        State state = new State();
        state.applyInput(firstCell);
        state.applyInput(secondCell);
        state = TutorialActivity.advance(1, state);
        int thirdCell = firstCellOtherThan(firstCell, secondCell);
        state.applyInput(secondCell);
        state.applyInput(thirdCell);
        assertFalse(state.entangled());
        state = TutorialActivity.advance(2, state);
        state.applyInput(firstCell);
        state.applyInput(thirdCell);
        assertTrue(state.entangled());
        state = TutorialActivity.advance(3, state);
        Move cycle = state.lastMove();
        state.applyInput(chooseSecondCollapse ? cycle.secondCellIndex() : cycle.firstCellIndex());
        String collapsedHistory = state.moveHistory();
        state = TutorialActivity.advance(4, state);

        assertFalse(state.gameOver());
        assertEquals(collapsedHistory, state.moveHistory());
        assertEquals("(" + firstCell + "," + secondCell + ")", state.moveHistory().substring(0, 5));
    }

    private static int firstCellOtherThan(int first, int second) {
        for (int cell = 0; cell < 9; ++cell) {
            if (cell != first && cell != second) {
                return cell;
            }
        }
        throw new AssertionError();
    }
}
