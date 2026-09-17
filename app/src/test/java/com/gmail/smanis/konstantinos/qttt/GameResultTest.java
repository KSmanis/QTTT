package com.gmail.smanis.konstantinos.qttt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GameResultTest {
    @Test
    public void invalidResultIsAnInProgressGame() {
        GameResult result = GameResult.INVALID;

        assertFalse(result.gameOver());
        assertFalse(result.draw());
        assertNull(result.winner());
    }

    @Test
    public void completeWinProducesTheOppositeLoss() {
        GameResult result = GameResult.COMPLETE_WIN;

        assertTrue(result.gameOver());
        assertEquals(Player.X, result.winner());
        assertEquals(GameResult.LOSS, result.oResult());
        assertEquals(1.0f, result.xScore(), 0.0f);
        assertEquals(0.0f, result.oScore(), 0.0f);
    }

    @Test
    public void drawHasNoWinner() {
        GameResult result = GameResult.DRAW;

        assertTrue(result.gameOver());
        assertTrue(result.draw());
        assertNull(result.winner());
        assertEquals(GameResult.DRAW, result.oResult());
    }

    @Test
    public void doubleCompleteWinScoresTwo() {
        GameResult result = GameResult.DOUBLE_COMPLETE_WIN;

        assertEquals(Player.X, result.winner());
        assertEquals(GameResult.LOSS, result.oResult());
        assertEquals(2.0f, result.xScore(), 0.0f);
        assertEquals(0.0f, result.oScore(), 0.0f);
    }

    @Test
    public void narrowWinResultsSwapScoresAndWinner() {
        GameResult result = GameResult.NARROW_WIN_FIRST;

        assertEquals(Player.X, result.winner());
        assertEquals(GameResult.NARROW_WIN_SECOND, result.oResult());
        assertEquals(1.0f, result.xScore(), 0.0f);
        assertEquals(0.5f, result.oScore(), 0.0f);

        result = GameResult.NARROW_WIN_SECOND;

        assertEquals(Player.O, result.winner());
        assertEquals(GameResult.NARROW_WIN_FIRST, result.oResult());
        assertEquals(0.5f, result.xScore(), 0.0f);
        assertEquals(1.0f, result.oScore(), 0.0f);
    }

    @Test
    public void lossMakesOTheWinner() {
        GameResult result = GameResult.LOSS;

        assertEquals(Player.O, result.winner());
        assertEquals(GameResult.COMPLETE_WIN, result.oResult());
        assertEquals(0.0f, result.xScore(), 0.0f);
        assertEquals(1.0f, result.oScore(), 0.0f);
    }
}
