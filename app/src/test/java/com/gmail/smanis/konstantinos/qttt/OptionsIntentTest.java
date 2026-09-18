package com.gmail.smanis.konstantinos.qttt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class OptionsIntentTest {
    @Test
    public void playerIdsRemainStableAndInvalidValuesFail() {
        assertEquals(0, Player.X.id());
        assertEquals(1, Player.O.id());
        assertSame(Player.X, Player.fromId(0));
        assertSame(Player.O, Player.fromId(1));
        assertThrows(IllegalArgumentException.class, () -> Player.fromId(-1));
    }

    @Test
    public void difficultyIdsRemainStableAndInvalidValuesFail() {
        assertEquals(0, Difficulty.RANDOM.id());
        assertEquals(1, Difficulty.EASY.id());
        assertEquals(2, Difficulty.MEDIUM.id());
        assertEquals(3, Difficulty.HARD.id());
        assertEquals(4, Difficulty.OPTIMAL.id());
        assertSame(Difficulty.MEDIUM, Difficulty.fromId(2));
        assertThrows(IllegalArgumentException.class, () -> Difficulty.fromId(-1));
    }

    @Test
    public void radioButtonMappingsDoNotDependOnChildOrder() {
        assertSame(Player.X, OptionsActivity.playerForRadioButton(R.id.radioButton_x));
        assertSame(Player.O, OptionsActivity.playerForRadioButton(R.id.radioButton_o));
        assertSame(
                Difficulty.RANDOM,
                OptionsActivity.difficultyForRadioButton(R.id.radioButton_random));
        assertSame(
                Difficulty.EASY, OptionsActivity.difficultyForRadioButton(R.id.radioButton_easy));
        assertSame(
                Difficulty.MEDIUM,
                OptionsActivity.difficultyForRadioButton(R.id.radioButton_medium));
        assertSame(
                Difficulty.HARD, OptionsActivity.difficultyForRadioButton(R.id.radioButton_hard));
        assertSame(
                Difficulty.OPTIMAL,
                OptionsActivity.difficultyForRadioButton(R.id.radioButton_optimal));
        assertThrows(
                IllegalArgumentException.class, () -> OptionsActivity.playerForRadioButton(-1));
        assertThrows(
                IllegalArgumentException.class, () -> OptionsActivity.difficultyForRadioButton(-1));
    }
}
